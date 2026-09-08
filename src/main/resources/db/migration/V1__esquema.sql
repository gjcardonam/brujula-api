-- Brújula · Esquema inicial
-- Corresponde al Modelo Entidad-Relación final del Sprint 0 (18 tablas, 24 relaciones).
-- Convenciones: nombres de tablas y columnas en snake_case, tal como en el modelo.
-- Nota: los timestamps se guardan con zona horaria (TIMESTAMPTZ) para evitar ambigüedades;
-- la API los expone en ISO-8601 y el front los muestra en hora local.

-- =========================
-- Tablas de catálogo
-- =========================
CREATE TABLE roles (
    id_rol      BIGSERIAL PRIMARY KEY,
    nombre_rol  VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE componentes (
    id_componente       BIGSERIAL PRIMARY KEY,
    nombre_componente   VARCHAR(100) NOT NULL UNIQUE,
    estado              VARCHAR(15) NOT NULL DEFAULT 'Activo'
                        CHECK (estado IN ('Activo', 'Desactivado'))
);

CREATE TABLE competencias (
    id_competencia      BIGSERIAL PRIMARY KEY,
    nombre_competencia  VARCHAR(100) NOT NULL UNIQUE,
    estado              VARCHAR(15) NOT NULL DEFAULT 'Activo'
                        CHECK (estado IN ('Activo', 'Desactivado'))
);

CREATE TABLE niveles_dificultad (
    id_nivel_dificultad BIGSERIAL PRIMARY KEY,
    nivel               VARCHAR(15) NOT NULL UNIQUE
);

CREATE TABLE tipos_error (
    id_tipo_error       BIGSERIAL PRIMARY KEY,
    nombre_tipo_error   VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE duraciones_simulacro (
    id_duracion         BIGSERIAL PRIMARY KEY,
    duracion_minutos    INT NOT NULL UNIQUE CHECK (duracion_minutos > 0)
);

-- =========================
-- Usuarios y seguridad
-- =========================
CREATE TABLE usuarios (
    id_usuario                  BIGSERIAL PRIMARY KEY,
    nombre                      VARCHAR(30) NOT NULL,
    apellido                    VARCHAR(50) NOT NULL,
    email                       VARCHAR(150) NOT NULL UNIQUE,                 -- HU-001 CA-03
    google_sub                  VARCHAR(255) UNIQUE,                          -- HU-001 CA-02/CA-12
    password_hash               VARCHAR(255) NOT NULL,                        -- HU-001 CA-13
    acepto_terminos             BOOLEAN NOT NULL DEFAULT FALSE,               -- HU-001 CA-07
    fecha_aceptacion_terminos   TIMESTAMPTZ,
    creado_en                   TIMESTAMPTZ NOT NULL DEFAULT now(),
    intentos_fallidos_login     INT NOT NULL DEFAULT 0,                       -- HU-002 CA-08
    ultimo_login                TIMESTAMPTZ,                                  -- HU-002 CA-06
    fecha_bloqueo               TIMESTAMPTZ,                                  -- HU-002 CA-08
    estado                      VARCHAR(10) NOT NULL DEFAULT 'Activo'
                                CHECK (estado IN ('Activo', 'Inactivo')),
    id_rol                      BIGINT NOT NULL REFERENCES roles (id_rol),
    -- Complemento al modelo: marca desde cuándo dejan de valer las sesiones emitidas
    -- antes de un restablecimiento de contraseña (HU-003 CA-07).
    password_actualizado_en     TIMESTAMPTZ
);

CREATE TABLE tokens_recuperacion (                                            -- HU-003
    id_token    BIGSERIAL PRIMARY KEY,
    id_usuario  BIGINT NOT NULL REFERENCES usuarios (id_usuario),
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    creado_en   TIMESTAMPTZ NOT NULL DEFAULT now(),
    expira_en   TIMESTAMPTZ NOT NULL,                                         -- HU-003 CA-02 (30 min)
    usado_en    TIMESTAMPTZ                                                   -- HU-003 CA-05/CA-06
);
CREATE INDEX ix_tokens_recuperacion_usuario ON tokens_recuperacion (id_usuario, creado_en);

CREATE TABLE tokens_sesion_revocados (                                        -- HU-004 CA-06
    jti         VARCHAR(64) PRIMARY KEY,
    id_usuario  BIGINT NOT NULL REFERENCES usuarios (id_usuario),
    revocado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    expira_en   TIMESTAMPTZ NOT NULL
);
CREATE INDEX ix_tokens_sesion_revocados_expira ON tokens_sesion_revocados (expira_en);

-- =========================
-- Banco de ejercicios
-- =========================
CREATE SEQUENCE ejercicios_numero_seq START WITH 1;                           -- HU-020 CA-11

CREATE TABLE ejercicios (
    id_ejercicio        BIGSERIAL PRIMARY KEY,
    numero              INT NOT NULL UNIQUE DEFAULT nextval('ejercicios_numero_seq'),  -- HU-020 CA-13
    enunciado           TEXT NOT NULL,
    imagen_enunciado    VARCHAR(255),
    id_nivel_dificultad BIGINT NOT NULL REFERENCES niveles_dificultad (id_nivel_dificultad),
    estado              VARCHAR(15) NOT NULL DEFAULT 'Activo'
                        CHECK (estado IN ('Activo', 'Desactivado')),
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT now(),
    id_competencia      BIGINT NOT NULL REFERENCES competencias (id_competencia),
    id_componente       BIGINT NOT NULL REFERENCES componentes (id_componente),
    id_usuario_creador  BIGINT NOT NULL REFERENCES usuarios (id_usuario)
);
-- Enunciado único (HU-020 CA-16): se indexa el hash normalizado para tolerar textos largos.
CREATE UNIQUE INDEX ux_ejercicios_enunciado ON ejercicios (md5(lower(btrim(enunciado))));
CREATE INDEX ix_ejercicios_componente ON ejercicios (id_componente, estado);
CREATE INDEX ix_ejercicios_competencia ON ejercicios (id_competencia, estado);

CREATE TABLE opciones_respuesta (
    id_opcion           BIGSERIAL PRIMARY KEY,
    descripcion_opcion  TEXT,
    imagen_opcion       VARCHAR(255),
    es_correcta         BOOLEAN NOT NULL DEFAULT FALSE,
    retroalimentacion   TEXT NOT NULL,
    id_tipo_error       BIGINT REFERENCES tipos_error (id_tipo_error),        -- distractores clasificados (objetivo 1, HU-027)
    orden_opcion        INT NOT NULL,
    id_ejercicio        BIGINT NOT NULL REFERENCES ejercicios (id_ejercicio),
    CONSTRAINT ck_opcion_con_contenido CHECK (descripcion_opcion IS NOT NULL OR imagen_opcion IS NOT NULL),
    -- Diferibles: al editar un ejercicio se reordenan opciones y se cambia la correcta dentro de una misma transacción.
    CONSTRAINT uq_opcion_orden UNIQUE (id_ejercicio, orden_opcion) DEFERRABLE INITIALLY DEFERRED,
    -- Exactamente una opción correcta por ejercicio (HU-020 CA-07): a lo sumo una en BD, el mínimo lo valida la API.
    CONSTRAINT ux_opcion_correcta_por_ejercicio EXCLUDE USING btree (id_ejercicio WITH =) WHERE (es_correcta) DEFERRABLE INITIALLY DEFERRED
);

-- =========================
-- Práctica y simulacros
-- =========================
CREATE TABLE simulacros (
    id_simulacro        BIGSERIAL PRIMARY KEY,
    id_usuario          BIGINT NOT NULL REFERENCES usuarios (id_usuario),
    id_duracion         BIGINT NOT NULL REFERENCES duraciones_simulacro (id_duracion),
    fecha_inicio        TIMESTAMPTZ NOT NULL DEFAULT now(),
    fecha_fin           TIMESTAMPTZ,
    estado              VARCHAR(15) NOT NULL DEFAULT 'En curso'
                        CHECK (estado IN ('En curso', 'Finalizado')),         -- HU-015
    tiempo_utilizado_seg INT                                                  -- HU-017 CA-02
);
CREATE INDEX ix_simulacros_usuario ON simulacros (id_usuario, fecha_inicio DESC);

CREATE TABLE intentos (
    id_intento              BIGSERIAL PRIMARY KEY,
    fecha_hora              TIMESTAMPTZ NOT NULL DEFAULT now(),
    es_correcto             BOOLEAN NOT NULL,
    nivel_confianza         INT NOT NULL CHECK (nivel_confianza BETWEEN 1 AND 5),   -- HU-012
    id_tipo_error           BIGINT REFERENCES tipos_error (id_tipo_error),    -- HU-027 (nulo si es correcto)
    id_usuario              BIGINT NOT NULL REFERENCES usuarios (id_usuario),
    id_ejercicio            BIGINT NOT NULL REFERENCES ejercicios (id_ejercicio),
    id_opcion_seleccionada  BIGINT NOT NULL REFERENCES opciones_respuesta (id_opcion),
    id_simulacro            BIGINT REFERENCES simulacros (id_simulacro),      -- nulo en práctica libre
    token_idempotencia      UUID UNIQUE                                       -- HU-016
);
CREATE INDEX ix_intentos_usuario_fecha ON intentos (id_usuario, fecha_hora DESC);
CREATE INDEX ix_intentos_simulacro ON intentos (id_simulacro);
CREATE INDEX ix_intentos_ejercicio ON intentos (id_ejercicio);
-- Dentro de un mismo simulacro no se repite un ejercicio (HU-014 CA-07/CA-08).
CREATE UNIQUE INDEX ux_intento_simulacro_ejercicio ON intentos (id_simulacro, id_ejercicio) WHERE id_simulacro IS NOT NULL;

CREATE TABLE recomendaciones_estudio (                                        -- HU-018
    id_recomendacion        BIGSERIAL PRIMARY KEY,
    mensaje_recomendacion   TEXT NOT NULL,
    porcentaje_aciertos     NUMERIC(5,2) NOT NULL,
    orden_prioridad         INT NOT NULL,
    id_simulacro            BIGINT NOT NULL REFERENCES simulacros (id_simulacro),
    id_competencia          BIGINT REFERENCES competencias (id_competencia),
    id_componente           BIGINT REFERENCES componentes (id_componente)
);
CREATE INDEX ix_recomendaciones_simulacro ON recomendaciones_estudio (id_simulacro, orden_prioridad);

-- =========================
-- Auditoría y configuración
-- =========================
CREATE TABLE auditoria_ejercicios (
    id_auditoria        BIGSERIAL PRIMARY KEY,
    tipo_accion         VARCHAR(15) NOT NULL
                        CHECK (tipo_accion IN ('Creación', 'Edición', 'Activación', 'Desactivación')),
    fecha_accion        TIMESTAMPTZ NOT NULL DEFAULT now(),
    id_ejercicio        BIGINT NOT NULL REFERENCES ejercicios (id_ejercicio),
    id_usuario_actor    BIGINT NOT NULL REFERENCES usuarios (id_usuario)
);

-- Preparada para PI II (HU-030 a HU-035). En PI1 no se escribe.
CREATE TABLE auditoria_catalogos (
    id_auditoria        BIGSERIAL PRIMARY KEY,
    tipo_catalogo       VARCHAR(15) NOT NULL CHECK (tipo_catalogo IN ('Componente', 'Competencia')),
    id_elemento         BIGINT NOT NULL,
    tipo_accion         VARCHAR(15) NOT NULL CHECK (tipo_accion IN ('Creación', 'Activación', 'Desactivación')),
    fecha_accion        TIMESTAMPTZ NOT NULL DEFAULT now(),
    id_usuario_actor    BIGINT NOT NULL REFERENCES usuarios (id_usuario)
);

-- Preparada para PI II (HU-036). En PI1 no se escribe.
CREATE TABLE auditoria_roles (
    id_auditoria        BIGSERIAL PRIMARY KEY,
    id_usuario_afectado BIGINT NOT NULL REFERENCES usuarios (id_usuario),
    id_usuario_actor    BIGINT NOT NULL REFERENCES usuarios (id_usuario),
    rol_anterior        VARCHAR(30) NOT NULL,
    rol_nuevo           VARCHAR(30) NOT NULL,
    fecha_accion        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE parametros_sistema (                                             -- HU-028 y otros
    clave       VARCHAR(50) PRIMARY KEY,
    valor       VARCHAR(50) NOT NULL,
    descripcion VARCHAR(255)
);
