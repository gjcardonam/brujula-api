# Brújula · API

Plataforma de preparación para la prueba Saber 11 en el área de matemáticas. El estudiante
practica ejercicios, declara qué tan seguro está de su respuesta y recibe una explicación
**de la opción que eligió**, no una explicación genérica.

Proyecto Integrador I · Universidad de Antioquia · 2026-2.

Este repositorio tiene la API, la base de datos y el despliegue. El front está en
[brujula-web](https://github.com/gjcardonam/brujula-web).

## Qué hay en esta rama

`main` contiene el **Sprint 1**:

| | |
| :-- | :-- |
| Acceso y cuenta | Registro con Google y contraseña, inicio de sesión con bloqueo temporal, recuperación por correo, cierre y expiración de sesión, edición del perfil y cambio de contraseña |
| Banco de ejercicios | Consulta, filtro por componente, paginación y apertura de un ejercicio |
| Práctica | Responder registrando el nivel de confianza y recibir la retroalimentación de la opción elegida |
| Administración | Crear ejercicios con sus opciones, su retroalimentación y sus imágenes |

Los simulacros, las estadísticas, el historial, la edición del banco y la clasificación del
tipo de error están construidos y esperan en la rama
[`sprint-2`](https://github.com/gjcardonam/brujula-api/tree/sprint-2). Entran a `main`
cuando llegue su sprint, con su propia migración de base de datos.

## Arrancar todo

Los dos repositorios se clonan **uno al lado del otro**, porque Compose construye el front
desde `../brujula-web`.

```bash
git clone https://github.com/gjcardonam/brujula-api.git
git clone https://github.com/gjcardonam/brujula-web.git
cd brujula-api
cp .env.example .env
docker compose up --build
```

| | |
| :-- | :-- |
| Aplicación | http://localhost:3000 |
| API | http://localhost:8080/api |
| Correo de prueba | http://localhost:8025 |

Los puertos se cambian en el `.env` con `WEB_PORT`, `API_PORT` y `DB_PORT`.

Con `DATOS_EJEMPLO=true` arranca con dieciséis ejercicios de matemáticas y estas dos
cuentas:

| Rol | Correo | Contraseña |
| :-- | :-- | :-- |
| Estudiante | estudiante@brujula.local | Estudiante.2026 |
| Administrador | admin@brujula.local | Admin.2026 |

## Desarrollar sin Docker

Hace falta Java 21 y un PostgreSQL 16 escuchando en el 5432. Maven no: lo baja el wrapper.

```bash
docker compose up -d db mailpit
./mvnw spring-boot:run
```

```bash
./mvnw test
```

Las 110 pruebas corren sin base de datos y sin levantar Spring: tardan segundos.

## Configuración

Todo entra por variables de entorno y `.env.example` las lista con sus valores de
desarrollo. Las que importan en producción:

| Variable | Para qué |
| :-- | :-- |
| `JWT_SECRET` | Firma de los tokens de sesión. Mínimo 32 caracteres, y hay que cambiarla |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Conexión a PostgreSQL |
| `FRONTEND_URL` | Base del enlace que viaja en el correo de recuperación |
| `GOOGLE_CLIENT_ID` | ID de cliente OAuth. Si está vacío, "Continuar con Google" queda simulado |
| `MODO_DESARROLLO` | En `false` desaparece el acceso simulado con Google |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_FROM` | Servidor de correo real |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Administrador que se crea en el primer arranque |
| `DATOS_EJEMPLO` | En `false` no carga los ejercicios de ejemplo |

Los umbrales del negocio no son variables de entorno: viven en la tabla
`parametros_sistema` y se cambian con un `UPDATE`, sin reiniciar.

## Cómo está hecho

Spring Boot 3 y Java 21 sobre PostgreSQL 16, en arquitectura hexagonal: 21 casos de uso,
uno por acción del usuario, y un dominio que no importa ni una clase de Spring.

```
src/main/java/co/edu/udea/brujula/
├── dominio/          modelo, reglas puras, puertos y errores de negocio
├── aplicacion/       los casos de uso, agrupados por contexto
└── infraestructura/  controladores, JPA, seguridad, correo, Google, disco y reloj

src/main/resources/db/migration/   el esquema, en dos migraciones de Flyway
src/test/java/                     110 pruebas, ninguna necesita base de datos
docs/                              el documento de arquitectura y sus diagramas
```

El detalle, con diagramas y las decisiones que hay detrás, está en
**[docs/arquitectura.md](docs/arquitectura.md)**.

## La API

Veintitrés rutas bajo `/api`. Todas responden JSON y los errores llevan siempre la misma
forma: `{ estado, codigo, mensaje }`, con un código estable que el front usa para decidir
qué mostrar.

| Área | Rutas |
| :-- | :-- |
| Acceso | `POST /auth/google` · `/auth/registro` · `/auth/login` · `/auth/logout` · `/auth/refresh` · `GET /auth/me` · `GET /auth/google/config` |
| Recuperación | `POST /auth/recuperar` · `GET /auth/restablecer/validar` · `POST /auth/restablecer` |
| Perfil | `PUT /perfil` · `PUT /perfil/password` |
| Banco | `GET /ejercicios` · `/ejercicios/componentes` · `/ejercicios/{id}` · `/ejercicios/{id}/siguiente` |
| Práctica | `POST /intentos` |
| Administración | `POST /ejercicios` · `POST /archivos` · `GET /catalogos` |
| Público | `GET /catalogos/publicos` · `GET /archivos/{nombre}` · `GET /salud` |

La sesión viaja en `Authorization: Bearer`. Un token deja de valer al cerrar sesión, al
expirar y al cambiar la contraseña.
