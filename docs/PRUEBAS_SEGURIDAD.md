# Pruebas de seguridad – Backend PetSalud

Este documento explica cómo se configuró el entorno de pruebas, qué valida cada prueba de seguridad y qué hallazgos aparecieron al escribirlas.

## 1. Configuración del entorno de pruebas, paso a paso

1. **Proyecto Maven con la estructura estándar.** El código de la aplicación está en `src/main/java` y las pruebas en `src/test/java`. Las pruebas usan el mismo paquete que el código que prueban, así pueden acceder a sus clases.
2. **JUnit 5 y Mockito.** Ya venían en `spring-boot-starter-test`: incluye JUnit Jupiter, Mockito, AssertJ, Hamcrest, JsonPath y MockMvc. `spring-security-test` agrega utilidades para probar la seguridad, como `httpBasic()`.
3. **Base de datos H2 en memoria (dependencia nueva, solo para pruebas).** La única prueba que traía el proyecto necesitaba un MySQL encendido. Con H2, `mvn test` funciona en cualquier computador y en GitHub sin instalar nada más.
   ```xml
   <dependency>
       <groupId>com.h2database</groupId>
       <artifactId>h2</artifactId>
       <scope>test</scope>
   </dependency>
   ```
4. **Configuración propia para las pruebas** en `src/test/resources/application.properties`. Reemplaza a la configuración principal mientras corren las pruebas:
   - H2 en modo compatible con MySQL y `ddl-auto=create-drop`, para que las tablas se creen y se borren en cada ejecución.
   - Una clave JWT distinta a la de producción, válida solo en las pruebas.
   - Sin administrador inicial: cada prueba crea los usuarios que necesita.
5. **Plugin JaCoCo 0.8.12** en el `pom.xml`. La meta `prepare-agent` registra qué líneas se ejecutan y la meta `report`, enganchada a la fase `test`, genera `target/site/jacoco/index.html`.
6. **Base común para las pruebas de integración** (`support/SeguridadTestBase`):
   - `@SpringBootTest` levanta la aplicación real y `@AutoConfigureMockMvc` permite enviarle solicitudes HTTP sin levantar un servidor.
   - `@Transactional` deshace los cambios al terminar cada prueba, así cada una parte con los mismos datos.
   - Antes de cada prueba crea un ADMIN, un VETERINARIO, dos CLIENTES, una mascota del primer cliente y un producto. A cada usuario le genera un **JWT real** con `JwtUtil`.
7. **Ejecución:** `./mvnw test` (en Windows, `mvnw.cmd test`). También se puede usar *Run tests* en IntelliJ IDEA o VS Code.

## 2. Diseño de las pruebas

| Clase | Tipo | Qué valida |
|---|---|---|
| `JwtUtilTest` | Unitaria (JUnit puro) | El token lleva el email y el rol. Se acepta para su dueño y se rechaza si está **expirado**, **firmado con otra clave**, **alterado**, **malformado** o si pertenece a otro usuario. También rechaza claves de menos de 256 bits. |
| `AuthorizationServiceTest` | Unitaria (Mockito) | Las reglas de propiedad (`esUsuario`, `esDuenoMascota`, `esDuenoNotificacion`) con repositorios simulados. Cubre también los casos límite: sin autenticación, id nulo, usuario eliminado y mascota sin dueño. |
| `AutenticacionTest` | Integración (MockMvc) | Login correcto e incorrecto, con el mismo mensaje para email o contraseña equivocados. El registro público siempre crea un CLIENTE, cifra la contraseña con BCrypt y nunca la devuelve. Solo un ADMIN puede crear cuentas de VETERINARIO o ADMIN. |
| `FiltroJwtTest` | Integración (MockMvc) | Responde 401 sin token, con token falso, expirado, de un usuario eliminado o sin el prefijo `Bearer`. HTTP Basic está desactivado, no se crean sesiones ni se exige CSRF y las respuestas llevan las cabeceras CSP, X-Frame-Options y nosniff. |
| `CitaControllerSeguridadTest`, `ExpedienteMedicoControllerSeguridadTest`, `MascotaControllerSeguridadTest`, `VentaControllerSeguridadTest` | Integración (MockMvc) | El acceso a las cuatro entidades clave del caso según el rol y la propiedad del recurso: 401, 403 o permitido. |

Cada regla se prueba en las dos direcciones: quien **debe** entrar, entra, y quien **no debe**, recibe 401 o 403. En total son 87 pruebas: 43 de autenticación y JWT, 43 de acceso por rol a las entidades y 1 de arranque del contexto.

## 3. Matriz de permisos

La matriz resume las reglas `@PreAuthorize` de cada controlador. Las pruebas cubren al menos un caso permitido y uno denegado de cada fila.

✅ = permitido · 🔒 = 403 · "Dueño" = el cliente al que pertenece el recurso. Sin token, todos los endpoints responden **401**, salvo `/api/login` y `/api/usuarios/registro`.

| Endpoint | Cliente dueño | Otro cliente | Veterinario | Admin |
|---|---|---|---|---|
| `POST /api/usuarios/interno` (crear empleados) | 🔒 | 🔒 | 🔒 | ✅ |
| `GET`, `PUT /api/mascotas/{id}` y `POST /api/mascotas/propietario/{id}` | ✅ | 🔒 | ✅ | ✅ |
| `GET /api/mascotas/propietario/{id}` | ✅ | 🔒 | ✅ | ✅ |
| `DELETE /api/mascotas/{id}` | 🔒 | 🔒 | 🔒 | ✅ |
| `POST /api/citas` y `GET /api/citas/mascota/{id}` | ✅ | 🔒 | ✅ | ✅ |
| `GET /api/citas/{id}`, `PUT .../estado` y `DELETE /api/citas/{id}` | 🔒 | 🔒 | ✅ | ✅ |
| `GET /api/citas/veterinario/{id}` y `GET /api/citas/entre-fechas` | 🔒 | 🔒 | ✅ | ✅ |
| `POST /api/expedientes` y `GET /api/expedientes/{id}` | 🔒 | 🔒 | ✅ | ✅ |
| `GET /api/expedientes/mascota/{id}` | ✅ | 🔒 | ✅ | ✅ |
| `DELETE /api/expedientes/{id}` | 🔒 | 🔒 | 🔒 | ✅ |
| `POST /api/ventas` y `GET /api/ventas/usuario/{id}` | ✅ | 🔒 | ✅ | ✅ |
| `GET /api/ventas/{id}` | 🔒 | 🔒 | ✅ | ✅ |
| `GET /api/ventas` (todas) | 🔒 | 🔒 | 🔒 | ✅ |

## 4. Hallazgos y mejoras propuestas

1. **Los errores de negocio no se transforman en respuestas HTTP claras.** Los servicios lanzan `IllegalArgumentException`, por ejemplo con "El email ya está registrado", pero no existe un `@RestControllerAdvice`. Por eso el cliente recibe un **500** en lugar de un **400** con un mensaje. La prueba `registro_emailDuplicado_esRechazado` deja este comportamiento documentado.
   *Mejora:* agregar un manejador global que convierta `IllegalArgumentException` en un 400 y "no encontrado" en un 404.
2. **Recursión infinita al convertir entidades a JSON** (ya mencionada en `GUIA_COMPLETA.md`). Las relaciones en ambos sentidos, como usuario ↔ mascotas o mascota ↔ citas, pueden producir un 500 con `Infinite recursion`. Por eso las pruebas de los casos permitidos solo comprueban que la seguridad deje pasar la solicitud (ni 401 ni 403), sin revisar el JSON.
   *Mejora:* usar `@JsonIgnore` o `@JsonIgnoreProperties` en esas relaciones, o devolver DTOs.
3. **Reglas de negocio para discutir.** Un cliente no puede cancelar su propia cita ni verla por id, aunque sí puede listar las citas de su mascota. Tampoco puede ver por id una de sus propias compras, aunque sí puede listarlas.
   *Mejora:* evaluar con el negocio si el dueño debería poder cancelar sus citas. Si es así, la regla sería `@authz.esDuenoCita(#id)`.
4. **No hay forma de cerrar sesión ni de revocar un token.** El JWT sigue siendo válido hasta que expira, una hora después. El riesgo baja porque el filtro vuelve a cargar al usuario desde la base de datos: si se elimina la cuenta, su token deja de funcionar, como comprueba `tokenDeUsuarioEliminado_401`.
   *Mejora:* agregar tokens de refresco de corta duración o una lista de tokens revocados.
