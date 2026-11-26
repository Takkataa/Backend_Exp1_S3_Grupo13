# Backend Clínica PetSalud

API REST de la Clínica PetSalud con seguridad basada en **Spring Security + JWT**.
Proyecto de la asignatura Desarrollo Backend II (PBY2202), actividad "Integrando seguridad en aplicaciones Backend".

## Qué incluye

- Autenticación con **JWT** (sin sesiones): el cliente hace login y envía el token en cada solicitud.
- Contraseñas cifradas con **BCrypt**.
- Autorización por **roles** (`@PreAuthorize`) y por propiedad del recurso (un cliente solo ve lo suyo).
- 44 endpoints: usuarios, mascotas, citas, expedientes médicos, productos, ventas, notificaciones y recompensas.
- Ejecución con **Docker Compose** (API + MySQL).
- **Monitoreo de seguridad**: logins fallidos, accesos denegados (403) y solicitudes sin token (401) quedan en el log.
- **94 pruebas automáticas** (JUnit 5 + Mockito) con reporte de cobertura JaCoCo.

## Requisitos

- Docker con Compose v2 (`docker compose version`) y Git.
- No hace falta instalar Java, Maven ni MySQL.

## Inicio rápido

```bash
git clone <URL-del-repositorio> petsalud && cd petsalud
cp .env.example .env              # Windows: copy .env.example .env
chmod 600 .env                    # solo Linux/macOS
openssl rand -base64 64           # copia el resultado y pégalo en JWT_SECRET
nano .env                         # cambia las contraseñas y pega JWT_SECRET
docker compose up -d --build      # la primera vez tarda unos minutos
docker compose logs -f app        # espera: "Started BackendpetsaludApplication"
```

Comprobar que funciona (debe responder **401**, porque la API está viva y protegida):

```bash
curl -i http://localhost:8080/api/productos
```

Para entrar desde otro equipo usa `http://<IP-del-servidor>:8080` (IP con `hostname -I`) y permite el puerto 8080 en el firewall.

## Configuración (archivo `.env`)

| Variable | Qué es |
|---|---|
| `DB_PASSWORD`, `DB_ROOT_PASSWORD` | Contraseñas de MySQL (obligatorias) |
| `DB_NAME`, `DB_USER` | Base y usuario de MySQL (por defecto `petsalud_db`, `petsalud`) |
| `JWT_SECRET` | Clave para firmar tokens: Base64, mínimo 32 bytes. **Obligatoria: sin ella la API no arranca** (no hay clave de respaldo) |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Primer administrador, se crea solo al arrancar |
| `APP_PORT`, `DB_HOST_PORT` | Puertos del equipo: API (8080) y MySQL (3307, solo local) |

El archivo `.env` **no se sube a Git** (está en `.gitignore`); solo se sube `.env.example`.

### Ejecutar sin Docker (desde el IDE o con Maven)

La aplicación **no arranca si falta `JWT_SECRET`**. Si no usas Docker, defínela antes de iniciarla:

```bash
# Linux / macOS
export JWT_SECRET="$(openssl rand -base64 64 | tr -d '\n')"
./mvnw spring-boot:run
```

```powershell
# Windows PowerShell (la clave queda solo para esa ventana)
$bytes = New-Object byte[] 64
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
.\mvnw.cmd spring-boot:run
```

En IntelliJ IDEA o VS Code agrégala en *Run Configuration → Environment variables*. Si falta, el arranque se detiene con el mensaje
`Falta la variable de entorno JWT_SECRET...`. Las pruebas (`mvnw test`) no la necesitan: usan su propia clave de prueba.

## Roles

| Rol | Equivale a | Puede |
|---|---|---|
| `CLIENTE` | Cliente | Sus propios datos, mascotas, citas, compras, notificaciones y recompensas; ver el catálogo |
| `VETERINARIO` | Empleado | Citas, expedientes, inventario, ventas y notificaciones |
| `ADMIN` | Gerente | Todo, incluida la gestión de usuarios |

- `POST /api/usuarios/registro` (público) siempre crea un **CLIENTE**, aunque se envíe otro rol.
- Solo un ADMIN crea empleados u otros administradores con `POST /api/usuarios/interno`.
- Respuestas: **401** sin token o token inválido · **403** sin permiso para esa operación.

## Usar la API

```bash
# 1) Login: devuelve {"token":"...","tipo":"Bearer",...}
curl -X POST http://localhost:8080/api/login -H "Content-Type: application/json" \
  -d '{"email":"admin@petsalud.cl","password":"<ADMIN_PASSWORD>"}'

# 2) Usar el token en las demás solicitudes
curl http://localhost:8080/api/usuarios -H "Authorization: Bearer <TOKEN>"
```

Rutas públicas: solo `/api/login` y `/api/usuarios/registro`. El token dura 1 hora.

## Pruebas con Postman

- Colección lista para importar: `docs/postman/PetSalud_Informe.postman_collection.json` (10 pruebas: JWT, roles, CSRF, XSS y SQL Injection).
  Ajusta las variables `baseUrl` y `adminPassword` y ejecuta los requests **en orden**.
- Guía con los 44 endpoints: `docs/GUIA_COMPLETA.md`, sección 6.

## Pruebas automáticas (JUnit 5 + Mockito + JaCoCo)

Las pruebas usan una base **H2 en memoria**, así que **no necesitan MySQL ni Docker**: solo Java 17+.

```bash
./mvnw test            # Windows: mvnw.cmd test
```

| Reporte | Dónde queda |
|---|---|
| Resultado de cada prueba (XML/TXT) | `target/surefire-reports/` |
| Cobertura de código (HTML) | `target/site/jacoco/index.html` (abrir en el navegador) |

Organización:

```
src/test/java/com/petsalud/backendpetsalud/
├── support/SeguridadTestBase.java      Datos de prueba (un usuario por rol) y atajos para MockMvc
├── security/                           Pruebas de seguridad
│   ├── JwtUtilTest.java                Unitarias: generar/validar JWT (expirado, falsificado, alterado)
│   ├── AuthorizationServiceTest.java   Unitarias con Mockito: "un cliente solo accede a lo suyo"
│   ├── AutenticacionTest.java          Login, registro público y cuentas internas
│   ├── FiltroJwtTest.java              Filtro JWT, 401, cabeceras de seguridad, sin sesión ni Basic Auth
│   ├── ConfiguracionJwtSecretTest.java Sin JWT_SECRET (o inválido) la aplicación no arranca
│   └── AuditoriaSeguridadTest.java     Monitoreo: logins fallidos, 401 y 403 quedan en el log
├── controller/*SeguridadTest.java      Acceso por rol a Cita, ExpedienteMedico, Mascota y Venta (401 / 403 / permitido)
└── BackendpetsaludApplicationTests     Arranque del contexto
src/test/resources/application.properties   Configuración de pruebas (H2 y clave JWT de prueba)
```

Detalle de la configuración, matriz de permisos probada y hallazgos: `docs/PRUEBAS_SEGURIDAD.md`.

## Monitoreo de seguridad

La clase `SecurityAuditLogger` registra en el log los eventos de seguridad, siempre con el prefijo `SEGURIDAD`:

| Evento | Nivel | Ejemplo de línea |
|---|---|---|
| Login correcto | INFO | `SEGURIDAD LOGIN_EXITOSO email=carla@mail.cl ip=192.168.1.20` |
| Login fallido | WARN | `SEGURIDAD LOGIN_FALLIDO email=carla@mail.cl ip=192.168.1.20` |
| Sin token o token inválido (401) | WARN | `SEGURIDAD NO_AUTENTICADO GET /api/mascotas/5 ip=192.168.1.20` |
| Sin permiso (403) | WARN | `SEGURIDAD ACCESO_DENEGADO usuario=carla@mail.cl DELETE /api/mascotas/5 ip=192.168.1.20` |

- **Nunca** se registran contraseñas ni tokens.
- Los saltos de línea que envía el cliente se reemplazan por `_`, para que nadie pueda escribir líneas falsas en el log (*log injection*).
- Muchos `LOGIN_FALLIDO` seguidos para el mismo email o IP indican un posible ataque de fuerza bruta.

```bash
docker compose logs app | grep SEGURIDAD                 # todos los eventos
docker compose logs app | grep LOGIN_FALLIDO             # solo logins fallidos
docker compose logs -f app | grep --line-buffered SEGURIDAD   # en vivo
```

## Operación

| Acción | Comando |
|---|---|
| Ver estado | `docker compose ps` |
| Ver logs de la API | `docker compose logs -f app` |
| Detener (conserva los datos) | `docker compose stop` |
| Volver a iniciar | `docker compose start` |
| Actualizar tras `git pull` | `docker compose up -d --build` |
| Borrar todo, **incluida la base de datos** | `docker compose down -v` |

Los datos de MySQL viven en el volumen `petsalud-db-data`: reconstruir la API no los borra.

## Seguridad en resumen

- JWT firmado (HMAC-SHA) con expiración de 1 hora; el secreto sale **solo** de la variable `JWT_SECRET`: no hay clave de respaldo en el código y, si falta, la API no arranca.
- Monitoreo: logins fallidos, 401 y 403 quedan registrados en el log (ver "Monitoreo de seguridad").
- Filtro que valida el token en cada solicitud y carga al usuario desde la base de datos.
- Sesiones desactivadas (stateless) y CSRF deshabilitado: el token va en el header `Authorization`, que el navegador no envía solo.
- Consultas parametrizadas (Spring Data JPA) contra inyección SQL.
- Respuestas con `Content-Security-Policy`, `X-Content-Type-Options: nosniff` y `X-Frame-Options: DENY`.
- Contenedor de la API con usuario sin privilegios; MySQL solo visible desde `127.0.0.1`.

## Estructura

```
src/main/java/com/petsalud/backendpetsalud/
├── controller/   Endpoints REST (cada uno con su @PreAuthorize)
├── service/      Lógica de negocio
├── repository/   Acceso a datos (Spring Data JPA)
├── entity/       Entidades JPA y enum Rol
├── dto/          Objetos del login
└── security/     SecurityConfig, JwtUtil, JwtAuthenticationFilter, UsuarioDetailsService,
                  UsuarioDetails, AuthorizationService, DataInitializer
src/test/          Pruebas JUnit (ver sección "Pruebas automáticas")
docs/             Guía completa, resumen técnico, pruebas de seguridad y colección de Postman
Dockerfile · docker-compose.yml · .env.example
```

## Más documentación

- `docs/GUIA_COMPLETA.md`: despliegue en Ubuntu, respaldos, ejecución sin Docker y guía de pruebas de los 44 endpoints.
- `docs/PRUEBAS_SEGURIDAD.md`: configuración del entorno de pruebas, permisos probados por rol y hallazgos.
