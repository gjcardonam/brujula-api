# Brújula · API y despliegue

Repositorios: [`brujula-api`](https://github.com/gjcardonam/brujula-api) (este, con la base de datos y el `docker-compose.yml`) y [`brujula-web`](https://github.com/gjcardonam/brujula-web) (front). Deben clonarse **uno al lado del otro** para que Compose construya el front desde `../brujula-web`.

Plataforma web gratuita para preparar la prueba Saber 11 en matemáticas, con retroalimentación por
tipo de error mediante un motor de reglas. Proyecto Integrador I · Ingeniería de Sistemas · UdeA · 2026-2.

Implementa las **29 historias de usuario del alcance de PI1 (HU-001 a HU-029)** y las **14 pantallas
M-01 a M-14** de los mockups del Sprint 0. Las historias HU-030 a HU-036 (administración de catálogos
y roles) son alcance de PI II: sus tablas existen en la base de datos pero no tienen API ni pantallas.

| Capa | Tecnología | Carpeta |
| :-- | :-- | :-- |
| Base de datos | PostgreSQL 16 · migraciones Flyway (18 tablas del modelo ER) | `src/main/resources/db/migration/` (este repo) |
| API REST | Java 21 · Spring Boot 3.3 · Spring Security (JWT) · JPA/Hibernate | este repo |
| Front | React 18 · TypeScript · Vite · React Router | repo `brujula-web` |
| Despliegue | Docker Compose (db + api + web + Mailpit) | `docker-compose.yml` (este repo) |

## Arranque rápido con Docker

```bash
git clone https://github.com/gjcardonam/brujula-api.git
git clone https://github.com/gjcardonam/brujula-web.git
cd brujula-api
cp .env.example .env        # opcional: ajustar contraseñas, client id de Google, etc.
docker compose up --build
```

| Servicio | URL |
| :-- | :-- |
| Aplicación web | http://localhost:3000 |
| API | http://localhost:8080/api (salud: `/api/salud`) |
| Mailpit (bandeja de correo de prueba, para los enlaces de recuperación) | http://localhost:8025 |
| PostgreSQL | `localhost:5432`, base `brujula`, usuario `brujula` |

Si algún puerto está ocupado: `API_PORT=8081 WEB_PORT=3001 DB_PORT=5433 docker compose up --build`.

**Usuarios de prueba** (se crean al arrancar con `DATOS_EJEMPLO=true`, valor por defecto):

| Rol | Correo | Contraseña |
| :-- | :-- | :-- |
| Administrador | `admin@brujula.local` (configurable con `ADMIN_EMAIL`) | `Admin.2026` (`ADMIN_PASSWORD`) |
| Estudiante | `estudiante@brujula.local` | `Estudiante.2026` |

También se cargan **16 ejercicios de ejemplo** (`src/main/resources/datos-ejemplo/ejercicios.json`)
que cubren los tres componentes y las tres competencias, con retroalimentación por opción y tipo de error
en cada distractor. Se pueden editar o ampliar en ese archivo; solo se cargan si el banco está vacío.

## Desarrollo sin Docker para la API y el front

Requisitos: JDK 21, Maven 3.9, Node 20+, y una base PostgreSQL (`docker compose up db mailpit` sirve).

```bash
# API en http://localhost:8080 (desde brujula-api)
MAIL_HOST=localhost mvn spring-boot:run

# Front en http://localhost:5173 (desde brujula-web; proxy /api → API)
npm install
npm run dev                      # API_TARGET=http://localhost:8081 npm run dev si la API está en otro puerto
```

Pruebas unitarias de la API: `mvn test` (política de contraseñas y motor de clasificación).

## Variables de entorno de la API

| Variable | Por defecto | Uso |
| :-- | :-- | :-- |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | `jdbc:postgresql://localhost:5432/brujula`, `brujula`, `brujula` | Conexión |
| `JWT_SECRET` | secreto de desarrollo | Firma de los tokens de sesión (mínimo 32 caracteres) |
| `FRONTEND_URL` | `http://localhost:5173` | CORS y enlace del correo de recuperación |
| `GOOGLE_CLIENT_ID` | vacío | Client ID de Google Identity Services. Vacío = Google **simulado** (ver abajo) |
| `MODO_DESARROLLO` | `true` | Habilita el Google simulado. **Poner en `false` en producción** |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USER`, `MAIL_PASSWORD`, `MAIL_FROM` | sin SMTP | Envío del enlace de recuperación. Sin SMTP el enlace se escribe en el log |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | `admin@brujula.local` / `Admin.2026` | Primer administrador (solo si no existe ninguno) |
| `DATOS_EJEMPLO` | `true` | Carga estudiante de prueba y ejercicios de ejemplo si el banco está vacío |
| `UPLOADS_DIR` | `./uploads` | Carpeta de imágenes de enunciados y opciones |
| `PORT` | `8080` | Puerto HTTP |

### Inicio con Google (HU-001)

Con `GOOGLE_CLIENT_ID` definido (y `VITE_GOOGLE_CLIENT_ID` en el front, que Compose toma de la misma
variable), el botón "Continuar con Google" usa Google Identity Services y la API verifica el ID token
contra las llaves públicas de Google (emisor y audiencia). Nunca se pide ni se guarda la contraseña de Google.

Sin client id y con `MODO_DESARROLLO=true`, el botón abre un formulario simulado donde se escribe el
correo que "verificaría" Google. Sirve para probar el flujo completo de registro sin crear credenciales.
Para obtener un client id real: Google Cloud Console → APIs y servicios → Credenciales → Crear ID de
cliente OAuth → Aplicación web → orígenes autorizados `http://localhost:3000` y `http://localhost:5173`.

## Endpoints principales

| Método y ruta | Historia |
| :-- | :-- |
| `POST /api/auth/google`, `POST /api/auth/registro` | HU-001 |
| `POST /api/auth/login` (bloqueo tras 5 fallos), `POST /api/auth/logout`, `POST /api/auth/refresh`, `GET /api/auth/me` | HU-002, HU-004 |
| `POST /api/auth/recuperar`, `GET /api/auth/restablecer/validar`, `POST /api/auth/restablecer` | HU-003 |
| `GET/PUT /api/perfil`, `PUT /api/perfil/password` | HU-005 |
| `GET /api/ejercicios?componente=&pagina=`, `GET /api/ejercicios/componentes` | HU-006, HU-007, HU-008 |
| `GET /api/ejercicios/{id}` (vista por rol), `GET /api/ejercicios/{id}/siguiente` | HU-009, HU-010, HU-022 |
| `POST /api/ejercicios`, `PUT /api/ejercicios/{id}`, `PATCH /api/ejercicios/{id}/estado`, `POST /api/archivos` | HU-020 a HU-024 |
| `POST /api/intentos` (con `tokenIdempotencia` y `idSimulacro` opcionales) | HU-010 a HU-012, HU-014, HU-016, HU-027, HU-028 |
| `GET /api/intentos/mios`, `GET /api/intentos/{id}` | HU-025 |
| `POST /api/simulacros`, `GET /api/simulacros/en-curso`, `GET /api/simulacros/{id}`, `GET /api/simulacros/{id}/siguiente-ejercicio`, `POST /api/simulacros/{id}/finalizar`, `GET /api/simulacros/{id}/resultado`, `GET /api/simulacros/mios` | HU-013 a HU-018, HU-026 |
| `GET /api/estadisticas/mias?componente=` | HU-019, HU-029 |
| `GET /api/catalogos` | catálogos (componentes, competencias, niveles, tipos de error, duraciones) |

Los errores responden `{ estado, codigo, mensaje, detalles[] }`. Códigos útiles para el front:
`CREDENCIALES_INVALIDAS`, `CUENTA_BLOQUEADA`, `CUENTA_EXISTENTE`, `ENLACE_INVALIDO`, `NO_DISPONIBLE` (410,
ejercicio desactivado), `SIMULACRO_EN_CURSO`, `SIMULACRO_FINALIZADO`, `EJERCICIO_YA_RESPONDIDO`,
`ENUNCIADO_DUPLICADO`, `OPCION_CON_INTENTOS`.

## Decisiones de implementación que conviene conocer

- **Reglas configurables en `parametros_sistema`** (HU-028 y otras): ventana de intentos, umbrales,
  intentos de login, minutos de bloqueo, horas de sesión, vigencia del enlace, tamaño de página. Se leen
  de la base en cada uso; cambiarlos con SQL aplica de inmediato.
- **Motor de clasificación (HU-027/HU-028)**, en `ClasificadorErrorService`: 1) confianza 4-5 → ansiedad;
  2) dominio previo (≥ 70 % en los últimos 5 intentos de la misma competencia o componente, mínimo 3) → hábito;
  3) si no, el tipo de error registrado en el distractor elegido, y si no tiene, cognitivo.
- **Expiración por inactividad (HU-004)**: el token dura 2 horas; el front lo renueva cada 15 minutos
  mientras el usuario haga peticiones. Cerrar sesión revoca el `jti` (tabla `tokens_sesion_revocados`).
- **Restablecer contraseña invalida sesiones (HU-003 CA-07)**: se agregó la columna
  `usuarios.password_actualizado_en`; los tokens emitidos antes de esa fecha se rechazan. Es el único
  agregado frente al modelo ER del Sprint 0 (los FK se crearon como BIGINT para coincidir con los PK).
- **Simulacro sin repetir ejercicios (HU-014 CA-07)**: el orden de los ejercicios es pseudoaleatorio
  pero fijo por simulacro (hash de `id_simulacro` + `id_ejercicio`), y se sirve el primero sin intento;
  una recarga de página muestra el mismo ejercicio y no hace falta una tabla adicional.
- **Finalización automática (HU-015/HU-016)**: la comprueba el servidor en cada petición del simulacro
  usando `fecha_inicio + duración`; el front calcula el temporizador con la hora del servidor, así que
  una desconexión no regala tiempo.
- **Idempotencia (HU-016)**: cada intento lleva un UUID; si se reenvía, la API devuelve el intento ya
  registrado con `repetido: true`.
- **Estudiantes nunca reciben `esCorrecta` ni `retroalimentacion` antes de responder (HU-020 CA-15)**:
  hay DTO separados para estudiante y administrador.
- **Edición con intentos (HU-021 CA-09)**: las opciones ya usadas no pueden eliminarse ni cambiar de
  texto, imagen o corrección; sí su retroalimentación y tipo de error.
- **Imágenes (HU-020 CA-09)**: se validan por los bytes iniciales (JPG/PNG/WEBP) y por tamaño (5 MB).
