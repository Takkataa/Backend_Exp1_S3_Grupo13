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

## 6. Probar la API (flujo básico)

```bash
# 1) Registrar un cliente (público)
curl -X POST localhost:8080/api/usuarios/registro -H "Content-Type: application/json" \
  -d '{"email":"cliente@petsalud.cl","password":"Cliente123!","nombre":"Ana","apellido":"Rojas"}'

# 2) Iniciar sesión -> devuelve {"token":"...","tipo":"Bearer",...}
curl -X POST localhost:8080/api/login -H "Content-Type: application/json" \
  -d '{"email":"cliente@petsalud.cl","password":"Cliente123!"}'

# 3) Usar el token en las demás solicitudes
curl localhost:8080/api/productos -H "Authorization: Bearer <TOKEN>"

# 4) Como ADMIN (login con ADMIN_EMAIL / ADMIN_PASSWORD), crear un empleado
curl -X POST localhost:8080/api/usuarios/interno -H "Authorization: Bearer <TOKEN_ADMIN>" \
  -H "Content-Type: application/json" \
  -d '{"email":"vet@petsalud.cl","password":"Vet12345!","nombre":"Pía","apellido":"Mena","rol":"VETERINARIO"}'
```

En Windows, estos comandos funcionan en Git Bash; en PowerShell o CMD usa Postman o reemplaza `curl` por `curl.exe`
(y las comillas simples por dobles escapadas).

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
