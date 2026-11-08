# Backend Clínica PetSalud

API REST de la Clínica PetSalud (Spring Boot 3.3 · Java 17 · MySQL 8) con seguridad basada en **Spring Security + JWT**:
contraseñas con BCrypt, sesiones sin estado (stateless) y autorización por roles.

---

## 1. Ejecución rápida con Docker (recomendada)

Solo necesitas **Docker Desktop** (o Docker Engine + Compose v2). No hace falta instalar Java, Maven ni MySQL.

```bash
# 1) Crear el archivo de configuración
cp .env.example .env            # en Windows (PowerShell): copy .env.example .env

# 2) Generar la clave secreta de los tokens y pegarla en .env, en la línea JWT_SECRET=
openssl rand -base64 64         # sin openssl: docker run --rm alpine sh -c "head -c 64 /dev/urandom | base64 | tr -d '\n'"

# 3) Cambiar en .env las contraseñas (DB_PASSWORD, DB_ROOT_PASSWORD, ADMIN_PASSWORD) y levantar todo
docker compose up -d --build
```

La primera vez tarda unos minutos (descarga imágenes y compila). Cuando termine:

- API disponible en **http://localhost:8080**
- MySQL disponible solo desde tu equipo en `localhost:3307`
- Se crea automáticamente el administrador definido en `ADMIN_EMAIL` / `ADMIN_PASSWORD`

Comprobar que todo está arriba y ver los registros:

```bash
docker compose ps                 # ambos servicios deben estar "running" (db en "healthy")
docker compose logs -f app        # buscar "Started BackendpetsaludApplication"
```

### Comandos útiles

| Acción | Comando |
|---|---|
| Detener (conserva los datos) | `docker compose down` |
| Detener y **borrar la base de datos** | `docker compose down -v` |
| Reconstruir tras cambiar código | `docker compose up -d --build` |
| Ver logs de la aplicación | `docker compose logs -f app` |
| Entrar a MySQL | `docker compose exec db mysql -u petsalud -p petsalud_db` |

---

## 2. Reglas simples de los contenedores

1. **Un servicio = un contenedor:** `db` (MySQL) y `app` (la API). Nada más.
2. **Toda la configuración va en `.env`:** puertos, claves y usuarios. No se escribe en el código ni en `docker-compose.yml`.
3. **`.env` nunca se sube a GitHub** (ya está en `.gitignore`). Se sube solo `.env.example`, sin secretos reales.
4. **Los datos viven en un volumen** (`petsalud-db-data`): borrar el contenedor no borra la base; solo `docker compose down -v` lo hace.
5. **La app espera a la base:** `app` arranca únicamente cuando `db` está "healthy".
6. **Puertos mínimos:** la API en `8080`; MySQL solo en `127.0.0.1:3307` (no visible desde la red).
7. **La app no corre como root** dentro del contenedor.
8. **Sin cambios manuales dentro de los contenedores:** si algo cambia, se edita el código o `.env` y se reconstruye.

---

## 3. Ejecución sin Docker para la app (desarrollo)

Requisitos: **JDK 17** y una base MySQL. Lo más simple es dejar solo la base en Docker y correr la app en tu equipo:

```bash
docker compose up -d db          # solo MySQL (puerto 3307)

# Linux / macOS
export DB_PORT=3307 DB_PASSWORD=<valor de DB_PASSWORD en .env> ADMIN_PASSWORD='Admin123!'
./mvnw spring-boot:run

# Windows (PowerShell)
$env:DB_PORT="3307"; $env:DB_PASSWORD="<valor de DB_PASSWORD en .env>"; $env:ADMIN_PASSWORD="Admin123!"
.\mvnw.cmd spring-boot:run
```

Con un MySQL propio ya instalado, basta con definir `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` y `DB_PASSWORD`.

## 4. Variables de entorno

| Variable | Para qué sirve | Valor por defecto |
|---|---|---|
| `DB_HOST` / `DB_PORT` | Dónde está MySQL | `localhost` / `3306` |
| `DB_NAME` | Nombre de la base | `petsalud_db` |
| `DB_USER` / `DB_PASSWORD` | Credenciales de la base | `petsalud` / `petsalud123` (solo desarrollo) |
| `DB_ROOT_PASSWORD` | Clave root de MySQL (solo Docker) | sin valor: obligatoria |
| `DB_HOST_PORT` | Puerto de tu equipo hacia MySQL (solo Docker) | `3307` |
| `APP_PORT` | Puerto de tu equipo hacia la API (solo Docker) | `8080` |
| `JWT_SECRET` | Clave de firma de tokens (Base64, ≥ 32 bytes) | clave de desarrollo; **en Docker es obligatoria** |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | Primer administrador; si `ADMIN_PASSWORD` está vacío no se crea | `admin@petsalud.cl` / vacío |

---

## 5. Roles

| Rol en el caso | Rol en el código | Alcance |
|---|---|---|
| Cliente | `CLIENTE` | Solo sus propios datos, mascotas, citas, notificaciones, recompensas y compras; consulta del catálogo |
| Empleado | `VETERINARIO` | Gestión clínica (citas, expedientes), inventario, ventas y notificaciones |
| Gerente | `ADMIN` | Acceso total, incluida la gestión de usuarios |

Respuestas de seguridad: **401** sin token o token inválido/expirado · **403** token válido pero sin permiso.

## 6. Probar la API con Postman

Esta guía cubre **todos los endpoints** (44 en total). Se ejecutan en orden: primero se preparan usuarios y tokens, luego se prueba cada módulo (casos que deben funcionar y casos que deben ser rechazados) y al final se limpian los datos.

### 6.1 Preparación de Postman

1. Crea una **Collection** llamada `PetSalud` y, en su pestaña **Variables**, agrega estas variables (déjalas vacías, salvo `baseUrl`):

| Variable | Valor inicial | Se llena con |
|---|---|---|
| `baseUrl` | `http://localhost:8080` (en el servidor Ubuntu: `http://<IP-del-servidor>:8080`) | manual |
| `adminPassword` | el valor de `ADMIN_PASSWORD` de tu `.env` | manual |
| `tokenAdmin`, `tokenVet`, `tokenCliente`, `tokenCliente2` | (vacío) | los logins (6.2) |
| `idCliente`, `idCliente2`, `idVet` | (vacío) | los registros (6.2) |
| `idMascota`, `idCita`, `idExpediente`, `idProducto`, `idProducto2`, `idVenta`, `idNotificacion`, `idRecompensa` | (vacío) | las creaciones de cada módulo |

2. En cada request, pestaña **Authorization** → tipo **Bearer Token** → valor `{{tokenXxx}}` según la columna "Token" de las tablas (si dice "—", usa **No Auth**).
3. Para los bodies, elige **Body → raw → JSON**. Postman agrega `Content-Type: application/json` solo.
4. **Guardado automático de tokens e ids:** en la pestaña **Scripts → Post-response** (o **Tests** en versiones antiguas) de cada request que lo indique, pega una línea como estas:

```javascript
// En un login (cambia el nombre de la variable según el usuario)
pm.collectionVariables.set("tokenAdmin", pm.response.json().token);

// En una creación (cambia el nombre de la variable según el recurso)
pm.collectionVariables.set("idMascota", pm.response.json().id);
```

Notas generales:
- El token dura **1 hora**; si empiezas a recibir 401, repite los logins.
- No se necesita token CSRF (la API es stateless y el token va en el header `Authorization`).
- **Códigos esperados:** `401` = sin token o token inválido · `403` = token válido pero sin permiso · `404` = recurso inexistente consultado con permiso suficiente · `204` = eliminado / sin contenido.
- Los errores de negocio (email repetido, id inexistente al crear algo) devuelven **500**, porque el proyecto no tiene un manejador global de excepciones. Es el comportamiento esperado del código base.

### 6.2 Paso A: preparar usuarios y tokens (ejecutar en este orden)

| # | Método y URL | Token | Body | Esperado | Guardar |
|---|---|---|---|---|---|
| A1 | `POST {{baseUrl}}/api/login` | — | `{"email":"admin@petsalud.cl","password":"{{adminPassword}}"}` | 200, devuelve `token` y `"rol":"ROLE_ADMIN"` | `tokenAdmin` |
| A2 | `POST {{baseUrl}}/api/usuarios/registro` | — | `{"email":"cliente1@petsalud.cl","password":"Cliente123!","nombre":"Ana","apellido":"Rojas","telefono":"+56911111111","direccion":"Santiago","rol":"ADMIN"}` | 200. **Verifica que `rol` venga como `CLIENTE`** aunque se envió `ADMIN`, y que no aparezca `password` | `idCliente` |
| A3 | `POST {{baseUrl}}/api/usuarios/registro` | — | `{"email":"cliente2@petsalud.cl","password":"Cliente123!","nombre":"Luis","apellido":"Soto"}` | 200 | `idCliente2` |
| A4 | `POST {{baseUrl}}/api/login` | — | `{"email":"cliente1@petsalud.cl","password":"Cliente123!"}` | 200 | `tokenCliente` |
| A5 | `POST {{baseUrl}}/api/login` | — | `{"email":"cliente2@petsalud.cl","password":"Cliente123!"}` | 200 | `tokenCliente2` |
| A6 | `POST {{baseUrl}}/api/usuarios/interno` | `tokenAdmin` | `{"email":"vet@petsalud.cl","password":"Vet12345!","nombre":"Pía","apellido":"Mena","rol":"VETERINARIO"}` | 200, `rol` = `VETERINARIO` | `idVet` |
| A7 | `POST {{baseUrl}}/api/login` | — | `{"email":"vet@petsalud.cl","password":"Vet12345!"}` | 200 | `tokenVet` |

En A1 el campo `rol` de la respuesta muestra el nombre de la autoridad (`ROLE_ADMIN`); es normal.

### 6.3 Paso B: pruebas de autenticación

| # | Método y URL | Token | Esperado |
|---|---|---|---|
| B1 | `POST {{baseUrl}}/api/login` con `{"email":"cliente1@petsalud.cl","password":"incorrecta"}` | — | 401, mensaje genérico `Credenciales invalidas` |
| B2 | `GET {{baseUrl}}/api/productos` | — (sin Authorization) | 401 |
| B3 | `GET {{baseUrl}}/api/productos` | Bearer con texto inventado (`abc.def.ghi`) | 401 |
| B4 | `GET {{baseUrl}}/api/productos` | Bearer con un token real **al que le cambiaste una letra** | 401 (firma inválida) |

### 6.4 Paso C: pruebas por módulo

Ejecuta cada tabla de arriba hacia abajo. Las filas marcadas **403** son pruebas de que el rol no tiene permiso.

#### Usuarios (`/api/usuarios`)

| # | Método y URL | Token | Body | Esperado |
|---|---|---|---|---|
| C1 | `GET {{baseUrl}}/api/usuarios` | `tokenAdmin` | — | 200, lista de usuarios sin `password` |
| C2 | `GET {{baseUrl}}/api/usuarios` | `tokenCliente` | — | **403** |
| C3 | `GET {{baseUrl}}/api/usuarios` | `tokenVet` | — | **403** |
| C4 | `GET {{baseUrl}}/api/usuarios/{{idCliente}}` | `tokenCliente` | — | 200 (su propio perfil) |
| C5 | `GET {{baseUrl}}/api/usuarios/{{idCliente2}}` | `tokenCliente` | — | **403** (perfil ajeno) |
| C6 | `GET {{baseUrl}}/api/usuarios/{{idCliente}}` | `tokenAdmin` | — | 200 |
| C7 | `PUT {{baseUrl}}/api/usuarios/{{idCliente}}` | `tokenCliente` | `{"nombre":"Ana María","apellido":"Rojas","telefono":"+56922222222","direccion":"Providencia"}` | 200, datos actualizados |
| C8 | `PUT {{baseUrl}}/api/usuarios/{{idCliente2}}` | `tokenCliente` | igual que C7 | **403** |
| C9 | `POST {{baseUrl}}/api/usuarios/interno` | `tokenCliente` | igual que A6 con otro email | **403** |

#### Mascotas (`/api/mascotas`)

| # | Método y URL | Token | Body | Esperado | Guardar |
|---|---|---|---|---|---|
| C10 | `POST {{baseUrl}}/api/mascotas/propietario/{{idCliente}}` | `tokenCliente` | `{"nombre":"Firulais","especie":"Perro","raza":"Labrador","fechaNacimiento":"2020-05-10","sexo":"Macho"}` | 200 | `idMascota` |
| C11 | `POST {{baseUrl}}/api/mascotas/propietario/{{idCliente}}` | `tokenCliente2` | igual que C10 | **403** (no es su cuenta) | |
| C12 | `GET {{baseUrl}}/api/mascotas/{{idMascota}}` | `tokenCliente` | — | 200 | |
| C13 | `GET {{baseUrl}}/api/mascotas/{{idMascota}}` | `tokenCliente2` | — | **403** (mascota ajena) | |
| C14 | `GET {{baseUrl}}/api/mascotas/{{idMascota}}` | `tokenVet` | — | 200 | |
| C15 | `PUT {{baseUrl}}/api/mascotas/{{idMascota}}` | `tokenCliente` | `{"nombre":"Firulais","especie":"Perro","raza":"Golden","fechaNacimiento":"2020-05-10","sexo":"Macho"}` | 200, raza actualizada | |
| C16 | `GET {{baseUrl}}/api/mascotas/propietario/{{idCliente}}` | `tokenCliente` | — | 200, lista con Firulais | |
| C17 | `GET {{baseUrl}}/api/mascotas/propietario/{{idCliente}}` | `tokenCliente2` | — | **403** | |

#### Citas (`/api/citas`)

Los datos van como **parámetros de URL** (pestaña **Params**), sin body. La fecha usa formato ISO 8601.

| # | Método y URL | Token | Esperado | Guardar |
|---|---|---|---|---|
| C18 | `POST {{baseUrl}}/api/citas?mascotaId={{idMascota}}&veterinarioId={{idVet}}&fechaHora=2026-12-01T10:30:00&motivo=Control` | `tokenCliente` | 200, estado `Agendada` | `idCita` |
| C19 | misma URL que C18 | `tokenCliente2` | **403** (mascota ajena) | |
| C20 | `GET {{baseUrl}}/api/citas/{{idCita}}` | `tokenVet` | 200 | |
| C21 | `GET {{baseUrl}}/api/citas/{{idCita}}` | `tokenCliente` | **403** | |
| C22 | `PUT {{baseUrl}}/api/citas/{{idCita}}/estado?estado=Completada` | `tokenVet` | 200, estado `Completada` | |
| C23 | `PUT {{baseUrl}}/api/citas/{{idCita}}/estado?estado=Cancelada` | `tokenCliente` | **403** | |
| C24 | `GET {{baseUrl}}/api/citas/mascota/{{idMascota}}` | `tokenCliente` | 200 | |
| C25 | `GET {{baseUrl}}/api/citas/mascota/{{idMascota}}` | `tokenCliente2` | **403** | |
| C26 | `GET {{baseUrl}}/api/citas/veterinario/{{idVet}}` | `tokenVet` | 200 | |
| C27 | `GET {{baseUrl}}/api/citas/veterinario/{{idVet}}` | `tokenCliente` | **403** | |
| C28 | `GET {{baseUrl}}/api/citas/entre-fechas?inicio=2026-01-01T00:00:00&fin=2026-12-31T23:59:59` | `tokenVet` | 200, incluye la cita | |
| C29 | `GET {{baseUrl}}/api/citas/entre-fechas?inicio=2026-01-01T00:00:00&fin=2026-12-31T23:59:59` | `tokenCliente` | **403** | |

#### Expedientes médicos (`/api/expedientes`)

| # | Método y URL | Token | Body | Esperado | Guardar |
|---|---|---|---|---|---|
| C30 | `POST {{baseUrl}}/api/expedientes?mascotaId={{idMascota}}&veterinarioId={{idVet}}` | `tokenVet` | `{"descripcion":"Control anual","diagnostico":"Sano","tratamiento":"Vacuna antirrábica"}` | 200, la `fecha` la pone el servidor | `idExpediente` |
| C31 | misma URL que C30 | `tokenCliente` | igual que C30 | **403** | |
| C32 | `GET {{baseUrl}}/api/expedientes/{{idExpediente}}` | `tokenVet` | — | 200 | |
| C33 | `GET {{baseUrl}}/api/expedientes/{{idExpediente}}` | `tokenCliente` | — | **403** | |
| C34 | `GET {{baseUrl}}/api/expedientes/mascota/{{idMascota}}` | `tokenCliente` | — | 200 (dueño) | |
| C35 | `GET {{baseUrl}}/api/expedientes/mascota/{{idMascota}}` | `tokenCliente2` | — | **403** | |

#### Productos (`/api/productos`)

| # | Método y URL | Token | Body | Esperado | Guardar |
|---|---|---|---|---|---|
| C36 | `POST {{baseUrl}}/api/productos` | `tokenVet` | `{"nombre":"Alimento Premium 10kg","descripcion":"Perros adultos","precio":29990,"stock":10,"categoria":"Alimentos"}` | 200 | `idProducto` |
| C37 | `POST {{baseUrl}}/api/productos` | `tokenVet` | `{"nombre":"Collar Ajustable","descripcion":"Talla M","precio":5990,"stock":3,"categoria":"Accesorios"}` | 200 | `idProducto2` |
| C38 | `POST {{baseUrl}}/api/productos` | `tokenCliente` | igual que C36 con otro nombre | **403** | |
| C39 | `GET {{baseUrl}}/api/productos` | `tokenCliente` | — | 200, lista de 2 productos | |
| C40 | `GET {{baseUrl}}/api/productos/{{idProducto}}` | `tokenCliente` | — | 200 | |
| C41 | `GET {{baseUrl}}/api/productos/categoria?categoria=Alimentos` | `tokenCliente` | — | 200, solo el alimento | |
| C42 | `GET {{baseUrl}}/api/productos/stock-bajo?cantidad=5` | `tokenVet` | — | 200, solo el collar (stock 3) | |
| C43 | `GET {{baseUrl}}/api/productos/stock-bajo?cantidad=5` | `tokenCliente` | — | **403** | |
| C44 | `PUT {{baseUrl}}/api/productos/{{idProducto}}` | `tokenVet` | `{"nombre":"Alimento Premium 10kg","descripcion":"Perros adultos","precio":27990,"stock":10,"categoria":"Alimentos"}` | 200, precio actualizado | |
| C45 | `PUT {{baseUrl}}/api/productos/{{idProducto}}` | `tokenCliente` | igual que C44 | **403** | |

#### Ventas (`/api/ventas`)

El body es una **lista de ids de productos** y el comprador va como parámetro `usuarioId`.

| # | Método y URL | Token | Body | Esperado | Guardar |
|---|---|---|---|---|---|
| C46 | `POST {{baseUrl}}/api/ventas?usuarioId={{idCliente}}` | `tokenCliente` | `[{{idProducto}}, {{idProducto2}}]` | 200, `total` = suma de precios; el stock baja en 1 | `idVenta` |
| C47 | `POST {{baseUrl}}/api/ventas?usuarioId={{idCliente2}}` | `tokenCliente` | `[{{idProducto}}]` | **403** (compra a nombre de otro) | |
| C48 | `GET {{baseUrl}}/api/ventas/{{idVenta}}` | `tokenVet` | — | 200 | |
| C49 | `GET {{baseUrl}}/api/ventas/{{idVenta}}` | `tokenCliente` | — | **403** | |
| C50 | `GET {{baseUrl}}/api/ventas/usuario/{{idCliente}}` | `tokenCliente` | — | 200 | |
| C51 | `GET {{baseUrl}}/api/ventas/usuario/{{idCliente}}` | `tokenCliente2` | — | **403** | |
| C52 | `GET {{baseUrl}}/api/ventas` | `tokenAdmin` | — | 200 | |
| C53 | `GET {{baseUrl}}/api/ventas` | `tokenVet` | — | **403** | |

Comprobación extra: repite C39 y verifica que el stock de ambos productos bajó en 1.

#### Notificaciones (`/api/notificaciones`)

| # | Método y URL | Token | Body | Esperado | Guardar |
|---|---|---|---|---|---|
| C54 | `POST {{baseUrl}}/api/notificaciones/usuario/{{idCliente}}` | `tokenVet` | `{"mensaje":"Recuerda la vacuna de Firulais","tipo":"Recordatorio"}` | 200, `leida` = `false` | `idNotificacion` |
| C55 | misma URL que C54 | `tokenCliente` | igual que C54 | **403** | |
| C56 | `GET {{baseUrl}}/api/notificaciones/{{idNotificacion}}` | `tokenCliente` | — | 200 (destinatario) | |
| C57 | `GET {{baseUrl}}/api/notificaciones/{{idNotificacion}}` | `tokenCliente2` | — | **403** | |
| C58 | `GET {{baseUrl}}/api/notificaciones/usuario/{{idCliente}}` | `tokenCliente` | — | 200 | |
| C59 | `GET {{baseUrl}}/api/notificaciones/usuario/{{idCliente}}` | `tokenCliente2` | — | **403** | |
| C60 | `GET {{baseUrl}}/api/notificaciones/usuario/{{idCliente}}/no-leidas` | `tokenCliente` | — | 200, incluye la notificación | |
| C61 | `PUT {{baseUrl}}/api/notificaciones/{{idNotificacion}}/leida` | `tokenCliente` | — | 204 | |
| C62 | repetir C60 | `tokenCliente` | — | 200, lista vacía | |

#### Recompensas (`/api/recompensas`)

| # | Método y URL | Token | Body | Esperado | Guardar |
|---|---|---|---|---|---|
| C63 | `POST {{baseUrl}}/api/recompensas/usuario/{{idCliente}}` | `tokenVet` | `{"puntos":100,"descripcion":"Puntos por compra"}` | 200 | `idRecompensa` |
| C64 | misma URL que C63 | `tokenCliente` | igual que C63 | **403** | |
| C65 | `GET {{baseUrl}}/api/recompensas/{{idRecompensa}}` | `tokenVet` | — | 200 | |
| C66 | `GET {{baseUrl}}/api/recompensas/{{idRecompensa}}` | `tokenCliente` | — | **403** | |
| C67 | `GET {{baseUrl}}/api/recompensas/usuario/{{idCliente}}` | `tokenCliente` | — | 200 | |
| C68 | `GET {{baseUrl}}/api/recompensas/usuario/{{idCliente}}` | `tokenCliente2` | — | **403** | |

### 6.5 Paso D: eliminaciones y limpieza

Primero comprueba que **solo el ADMIN** puede borrar (filas D1 a D3), luego borra con `tokenAdmin`. Todos devuelven **204**.

| # | Método y URL | Token | Esperado |
|---|---|---|---|
| D1 | `DELETE {{baseUrl}}/api/productos/{{idProducto}}` | `tokenVet` | **403** |
| D2 | `DELETE {{baseUrl}}/api/mascotas/{{idMascota}}` | `tokenCliente` | **403** |
| D3 | `DELETE {{baseUrl}}/api/usuarios/{{idCliente2}}` | `tokenVet` | **403** |
| D4 | `DELETE {{baseUrl}}/api/citas/{{idCita}}` | `tokenVet` | 204 (las citas las cancela VETERINARIO o ADMIN) |
| D5 | `DELETE {{baseUrl}}/api/expedientes/{{idExpediente}}` | `tokenAdmin` | 204 |
| D6 | `DELETE {{baseUrl}}/api/notificaciones/{{idNotificacion}}` | `tokenAdmin` | 204 |
| D7 | `DELETE {{baseUrl}}/api/recompensas/{{idRecompensa}}` | `tokenAdmin` | 204 |
| D8 | `DELETE {{baseUrl}}/api/productos/{{idProducto2}}` | `tokenAdmin` | 204 |
| D9 | `DELETE {{baseUrl}}/api/mascotas/{{idMascota}}` | `tokenAdmin` | 204 |
| D10 | `DELETE {{baseUrl}}/api/usuarios/{{idCliente2}}` | `tokenAdmin` | 204 |

Las ventas y los productos con ventas asociadas pueden fallar al borrarse por las claves foráneas de la base (500); es normal. Si quieres empezar de cero, lo más simple es `docker compose down -v` y volver a levantar.

### 6.6 Resumen de cobertura

| Módulo | Endpoints | Casos permitidos | Casos rechazados |
|---|---|---|---|
| Autenticación | `POST /api/login` | A1, A4, A5, A7 | B1 a B4 |
| Usuarios | 6 (incluye `/registro` e `/interno`) | A2, A3, A6, C1, C4, C6, C7 | C2, C3, C5, C8, C9 |
| Mascotas | 5 | C10, C12, C14 a C16 | C11, C13, C17 |
| Citas | 7 | C18, C20, C22, C24, C26, C28 | C19, C21, C23, C25, C27, C29 |
| Expedientes | 4 | C30, C32, C34 | C31, C33, C35 |
| Productos | 7 | C36 a C37, C39 a C42, C44 | C38, C43, C45 |
| Ventas | 4 | C46, C48, C50, C52 | C47, C49, C51, C53 |
| Notificaciones | 6 | C54, C56, C58, C60, C61 | C55, C57, C59 |
| Recompensas | 4 | C63, C65, C67 | C64, C66, C68 |

> Para documentar el criterio de pruebas de la rúbrica, toma una captura de Postman por cada fila importante (código de estado visible) y pégala en el informe.

## 7. Pruebas automáticas

`./mvnw test` necesita una base MySQL accesible (levántala con `docker compose up -d db` y define `DB_PORT=3307` y `DB_PASSWORD`).

## 8. Estructura

```
src/main/java/com/petsalud/backendpetsalud/
├── controller/   Endpoints REST (cada uno con su regla @PreAuthorize)
├── service/      Lógica de negocio
├── repository/   Acceso a datos (Spring Data JPA)
├── entity/       Entidades JPA y enum Rol
├── dto/          Objetos de login (request/response)
└── security/     SecurityConfig, JwtUtil, JwtAuthenticationFilter,
                  UsuarioDetailsService, UsuarioDetails, AuthorizationService, DataInitializer
Dockerfile · docker-compose.yml · .env.example
```

## 9. Despliegue en un servidor Ubuntu (red propia)

Pensado para Ubuntu Server 22.04/24.04 con acceso por SSH. Orden recomendado:

**1) Instalar Docker Engine y Compose** (repositorio oficial; si cambia, ver https://docs.docker.com/engine/install/ubuntu/):

```bash
sudo apt-get update && sudo apt-get install -y ca-certificates curl git
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo usermod -aG docker $USER      # cerrar sesión y volver a entrar para que aplique
sudo systemctl enable --now docker # Docker inicia solo al reiniciar el servidor
```

**2) Obtener el proyecto y configurarlo**

```bash
git clone <URL-de-tu-repositorio> petsalud && cd petsalud
cp .env.example .env
chmod 600 .env                     # solo tu usuario puede leer las claves
nano .env                          # completar claves y JWT_SECRET (openssl rand -base64 64)
docker compose up -d --build
```

Como los contenedores tienen `restart: unless-stopped` y Docker inicia con el sistema, la aplicación vuelve a levantar sola tras un reinicio.

**3) Acceder desde otros equipos de la red**

```bash
hostname -I                        # IP del servidor, por ejemplo 192.168.1.50
curl -i http://192.168.1.50:8080/api/productos   # desde otro equipo: debe responder 401 (la API está viva y protegida)
```

- Conviene darle al servidor una **IP fija** (reserva DHCP en tu router o configuración estática con netplan), para que la dirección no cambie.
- Si tienes firewall en el servidor, permite el puerto de la API: `sudo ufw allow from 192.168.1.0/24 to any port 8080 proto tcp` (ajusta tu subred) y `sudo ufw allow OpenSSH` antes de activarlo con `sudo ufw enable`.
- **Ojo:** Docker publica sus puertos saltándose las reglas de `ufw`. Por eso MySQL ya está publicado solo en `127.0.0.1` (no es alcanzable desde la red). Si quieres que la API tampoco quede abierta a toda la red, cambia en `docker-compose.yml` el puerto de `app` a `"192.168.1.50:${APP_PORT:-8080}:8080"` (la IP de tu servidor).
- **No expongas el servidor a Internet** (apertura de puertos en el router) sin antes poner HTTPS.

**4) HTTPS (recomendado)**

La API recibe contraseñas y tokens JWT. Con HTTP simple viajan sin cifrar, lo que en una red de confianza es un riesgo bajo, pero cualquier equipo comprometido en esa red podría leerlos. Si los usuarios se conectan por Wi-Fi compartido o si más adelante habrá un frontend, pon un proxy inverso con TLS (Nginx o Caddy) delante de la API y publica `app` solo en `127.0.0.1`.

**5) Respaldos de la base de datos**

```bash
# Crear respaldo
docker compose exec -T db sh -c 'mysqldump -u root -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE"' > respaldo_$(date +%F).sql

# Restaurar
docker compose exec -T db sh -c 'mysql -u root -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE"' < respaldo_AAAA-MM-DD.sql
```

Guarda los respaldos **fuera** del servidor (otro equipo o disco externo). Se pueden automatizar con `crontab -e`.

**6) Actualizar la aplicación**

```bash
cd petsalud && git pull && docker compose up -d --build
```

Los datos se conservan porque viven en el volumen `petsalud-db-data`.

## 10. Problemas frecuentes

| Síntoma | Causa y solución |
|---|---|
| `Defina JWT_SECRET en el archivo .env` | Falta crear `.env` o completar esa variable (paso 1.2). |
| `port is already allocated` (3307 u 8080) | Otro programa usa el puerto. Cambia `DB_HOST_PORT` o `APP_PORT` en `.env` y repite `docker compose up -d`. |
| La app se reinicia o muestra `Communications link failure` | La base aún arrancaba o `DB_PASSWORD` cambió después de crear el volumen. Si cambiaste credenciales: `docker compose down -v` y volver a levantar (borra los datos). |
| `Access denied for user` | Mismo caso: MySQL conserva las credenciales con las que se creó el volumen. |
| Login devuelve 401 con el admin | Revisa `ADMIN_EMAIL` / `ADMIN_PASSWORD` en `.env`. El admin solo se crea si ese email no existía; para recrearlo, `docker compose down -v`. |
| Cualquier solicitud devuelve 401 | Falta el header `Authorization: Bearer <token>` o el token expiró (dura 1 hora). Vuelve a hacer login. |
| Error de `Weak key` / clave JWT | `JWT_SECRET` debe estar en Base64 y tener al menos 32 bytes (usa el comando del paso 1.2). |
| Una respuesta da 500 con `Infinite recursion` o `StackOverflowError` | Las entidades JPA tienen relaciones en ambos sentidos (usuario ↔ mascotas, etc.) y al convertirlas a JSON pueden repetirse sin fin. Es una limitación del código base, no de la seguridad: se corrige agregando `@JsonIgnore` o `@JsonIgnoreProperties` en esas relaciones, o devolviendo DTOs. |
