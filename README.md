# Backend Clínica PetSalud

API REST de la Clínica PetSalud con seguridad basada en **Spring Security + JWT**.
Proyecto de la asignatura Desarrollo Backend II (PBY2202), actividad "Integrando seguridad en aplicaciones Backend".

## Qué incluye

- Autenticación con **JWT** (sin sesiones): el cliente hace login y envía el token en cada solicitud.
- Contraseñas cifradas con **BCrypt**.
- Autorización por **roles** (`@PreAuthorize`) y por propiedad del recurso (un cliente solo ve lo suyo).
- 44 endpoints: usuarios, mascotas, citas, expedientes médicos, productos, ventas, notificaciones y recompensas.
- Ejecución con **Docker Compose** (API + MySQL).

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
| `JWT_SECRET` | Clave para firmar tokens: Base64, mínimo 32 bytes (obligatoria) |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Primer administrador, se crea solo al arrancar |
| `APP_PORT`, `DB_HOST_PORT` | Puertos del equipo: API (8080) y MySQL (3307, solo local) |

El archivo `.env` **no se sube a Git** (está en `.gitignore`); solo se sube `.env.example`.

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

- JWT firmado (HMAC-SHA) con expiración de 1 hora; el secreto sale de la variable `JWT_SECRET`, no del código.
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
docs/             Guía completa, resumen técnico y colección de Postman
Dockerfile · docker-compose.yml · .env.example
```

## Más documentación

- `docs/GUIA_COMPLETA.md`: despliegue en Ubuntu, respaldos, ejecución sin Docker y guía de pruebas de los 44 endpoints.
- `docs/RESUMEN_PROYECTO.md`: resumen técnico del proyecto.
