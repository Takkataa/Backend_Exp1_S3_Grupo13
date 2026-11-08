# Resumen del proyecto (contexto para otra IA)

## 1. Qué es y de dónde viene
- **Origen:** evaluación sumativa del curso *Desarrollo Backend II (PBY2202)*, Duoc UC, Semana 3 "Integrando seguridad en aplicaciones Backend". Caso: **Clínica PetSalud** (veterinaria chilena).
- **Punto de partida:** un backend Spring Boot entregado por el profesor (`backendpetsalud`), con entidades, repositorios, servicios y controladores CRUD, pero con seguridad incompleta (Basic Auth, CSRF por cookie, reglas para rutas que no existían, contraseñas en texto plano, registro público que permitía elegir rol ADMIN).
- **Alcance pedido por el usuario:** solo el **Paso 2** del "Formato de respuesta" (configuración del framework de seguridad: UserDetailsService, BCrypt, JwtUtil, filtro JWT, `@PreAuthorize`, pruebas funcionales). LDAP/LDAPS/IDaaS **no** están implementados (no aparecen en la pauta de evaluación). Los pasos 1 (análisis), 3 (informe), 4 (GitHub) y las pruebas XSS/CSRF/SQLi (criterio 5 de la pauta) **no se han hecho**.

## 2. Stack
Java 17 · Spring Boot 3.3.6 · Spring Security 6 · Spring Data JPA/Hibernate · MySQL 8 · Lombok · jjwt 0.12.6 · Maven (wrapper) · Docker / Docker Compose. Destino de despliegue: servidor Ubuntu en red propia del usuario.

## 3. Estructura (paquete `com.petsalud.backendpetsalud`)
```
controller/  Usuario, Mascota, Cita, ExpedienteMedico, Producto, Venta, Notificacion, Recompensa, AuthController
service/     interfaz + Impl por módulo
repository/  JpaRepository por entidad (consultas derivadas)
entity/      Usuario, Mascota, Cita, ExpedienteMedico, Producto, Venta, Notificacion, Recompensa, enum Rol
dto/         LoginRequest, LoginResponse (records)
security/    SecurityConfig, JwtUtil, JwtAuthenticationFilter, UsuarioDetailsService, UsuarioDetails,
             AuthorizationService (bean "authz"), DataInitializer
raíz         Dockerfile, docker-compose.yml, .env.example, .dockerignore, README.md, RESUMEN_PROYECTO.md
```

## 4. Seguridad implementada
- **Autenticación:** `POST /api/login` (público) valida email+contraseña con `AuthenticationManager`; devuelve `{token, tipo:"Bearer", email, rol, expiraEnMs}`. Error → 401 con mensaje genérico `Credenciales invalidas`.
- **JWT (`JwtUtil`):** HMAC-SHA, subject = email, claim `rol`, expira a 1 h (`jwt.expiration-ms`). Secreto en Base64 (≥32 bytes) desde `JWT_SECRET`.
- **Filtro (`JwtAuthenticationFilter`, OncePerRequestFilter):** lee `Authorization: Bearer`, valida token, carga el usuario desde BD y puebla el `SecurityContext`. Token inválido → sigue sin autenticar → 401.
- **`SecurityConfig`:** stateless, CSRF desactivado (token en header, no cookie), httpBasic y formLogin desactivados, `@EnableMethodSecurity`, CSP `default-src 'none'; frame-ancestors 'none'`, entry point 401. Públicos: `/api/login` y `/api/usuarios/registro`; todo lo demás autenticado.
- **Contraseñas:** BCrypt (`UsuarioServiceImpl.guardarUsuario`). `Usuario.password` es `WRITE_ONLY` en JSON.
- **Roles (enum `Rol`):** `CLIENTE` (cliente), `VETERINARIO` (empleado), `ADMIN` (gerente). `UsuarioDetails` los expone como `ROLE_<ROL>` (por eso `hasRole('ADMIN')` funciona).
- **Autorización:** `@PreAuthorize` en **todos** los endpoints. Además de rol, el bean `authz` (`AuthorizationService`) verifica propiedad: `esUsuario(id)`, `esDuenoMascota(id)`, `esDuenoNotificacion(id)`, de modo que un CLIENTE solo accede a sus propios recursos.
- **Registro seguro:** `/api/usuarios/registro` siempre crea `CLIENTE` (ignora el rol del body). `POST /api/usuarios/interno` (solo ADMIN) crea VETERINARIO/ADMIN.
- **Primer admin:** `DataInitializer` (CommandLineRunner) crea un ADMIN al arrancar con `ADMIN_EMAIL`/`ADMIN_PASSWORD`; si la contraseña está vacía no crea nada.
- **Respuestas:** 401 sin token/token inválido; 403 sin permiso. Errores de negocio (`IllegalArgumentException`) → 500 (no hay `@ControllerAdvice`).

### Matriz de permisos
| Recurso | CLIENTE | VETERINARIO | ADMIN |
|---|---|---|---|
| Usuarios | ver/editar solo su perfil | sin acceso | todo (listar, crear internos, eliminar) |
| Mascotas | registrar/ver/editar las propias | ver/editar todas | todo (eliminar solo ADMIN) |
| Citas | agendar y ver las de sus mascotas | ver, cambiar estado, cancelar, agendar | igual que VETERINARIO |
| Expedientes | ver los de sus mascotas | crear y consultar | todo (eliminar solo ADMIN) |
| Productos | consultar catálogo | consultar, crear, editar, stock bajo | todo (eliminar solo ADMIN) |
| Ventas | realizar y ver las propias | registrar y consultar | todo (listado general solo ADMIN) |
| Notificaciones | ver/marcar leídas las propias | enviar y consultar | todo (eliminar solo ADMIN) |
| Recompensas | ver las propias | agregar y consultar | todo (eliminar solo ADMIN) |

## 5. Endpoints (44)
`/api/login`; `/api/usuarios` (registro, interno, GET id, PUT id, DELETE id, GET lista); `/api/mascotas` (POST propietario/{id}, GET/PUT/DELETE {id}, GET propietario/{id}); `/api/citas` (POST con query params mascotaId, veterinarioId, fechaHora ISO, motivo; GET {id}; PUT {id}/estado?estado=; DELETE {id}; GET mascota/{id}; GET veterinario/{id}; GET entre-fechas?inicio&fin); `/api/expedientes` (POST ?mascotaId&veterinarioId + body; GET {id}; GET mascota/{id}; DELETE {id}); `/api/productos` (CRUD, GET categoria?categoria=, GET stock-bajo?cantidad=); `/api/ventas` (POST ?usuarioId + body lista de ids; GET {id}; GET usuario/{id}; GET lista); `/api/notificaciones` (POST usuario/{id}; GET {id}; GET usuario/{id}; GET usuario/{id}/no-leidas; PUT {id}/leida; DELETE {id}); `/api/recompensas` (POST usuario/{id}; GET {id}; GET usuario/{id}; DELETE {id}).
La guía completa de pruebas con Postman (orden, bodies, token por request y resultado esperado, casos 200/403/401) está en la sección 6 del `README.md`.

## 6. Configuración y despliegue
- `application.properties` lee todo de variables de entorno con valores por defecto: `DB_HOST, DB_PORT, DB_NAME (petsalud_db), DB_USER, DB_PASSWORD, JWT_SECRET, ADMIN_EMAIL, ADMIN_PASSWORD`. `ddl-auto=update`.
- `docker-compose.yml`: servicios `db` (mysql:8.0, volumen `petsalud-db-data`, healthcheck, publicado solo en `127.0.0.1:3307`) y `app` (build del `Dockerfile` multi-etapa, usuario no root, espera a `db` healthy, puerto 8080). Variables desde `.env` (copiar de `.env.example`; `.env` está en `.gitignore`). `JWT_SECRET`, `DB_PASSWORD` y `DB_ROOT_PASSWORD` son obligatorias en compose.
- Arranque: `cp .env.example .env` → completar → `docker compose up -d --build`.
- README incluye despliegue en Ubuntu (instalación de Docker, IP fija, ufw, respaldos con mysqldump, actualización) y tabla de problemas frecuentes.

## 7. Cambios hechos sobre el código del profesor
SecurityConfig reescrito; `UsuarioDetails` con prefijo `ROLE_`; BCrypt y registro forzado a CLIENTE en `UsuarioServiceImpl` (+ `crearUsuarioInterno`); `@JsonProperty(WRITE_ONLY)` en password; `@PreAuthorize` en los 8 controladores; nuevos `AuthController`, DTOs, `JwtUtil`, filtro, `AuthorizationService`, `DataInitializer`; dependencias jjwt en el pom; `application.properties` parametrizado (la base original `letrasypapeles_db` parecía copiada de otro proyecto y se cambió a `petsalud_db`; se quitó el dialecto `MySQL8Dialect` obsoleto); comentarios explicativos en español en todas las clases.

## 8. Estado y limitaciones conocidas (importante)
- **Nada ha sido compilado ni ejecutado.** El entorno donde se escribió no tenía Maven, Docker ni acceso a Maven Central. Solo se verificó balance de llaves/comentarios y validez YAML del compose. Hay que compilar y probar en el equipo del usuario y corregir errores si aparecen.
- **Riesgo probable:** las entidades tienen relaciones bidireccionales (`Usuario.mascotas` ↔ `Mascota.propietario`, etc.) y los controladores devuelven entidades directamente → posible **recursión infinita al serializar JSON** (500). Corrección sugerida: `@JsonIgnore`/`@JsonIgnoreProperties` en los lados inversos o DTOs de respuesta.
- Sin `@ControllerAdvice`: errores de negocio devuelven 500 en vez de 400/404.
- `VentaServiceImpl.realizarVenta` descuenta 1 unidad por producto y no es `@Transactional`; los ids duplicados en la lista no se contemplan. `eliminar*` no valida existencia.
- HTTP sin TLS: se recomienda proxy inverso (Nginx/Caddy) antes de exponer fuera de la red local. `ufw` no filtra puertos publicados por Docker.
- No hay pruebas automáticas más allá del `contextLoads` (requiere MySQL).

## 9. Pendiente respecto a la rúbrica (7 criterios)
1 Análisis de amenazas (Paso 1) · 5 **Pruebas XSS/CSRF/SQL Injection documentadas (15 pts, no hecho)** · 6 Subir a GitHub con documentación · 7 Informe técnico (Paso 3). Entregado: criterios 2, 3 y 4 en código, más un documento Word `Paso2_Configuracion_Seguridad_PetSalud.docx` con la explicación de la configuración, la matriz de permisos y 17 casos de prueba funcionales (el README ahora tiene una guía de pruebas más completa, con 44 endpoints).
