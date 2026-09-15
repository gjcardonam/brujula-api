CREATE FUNCTION fn_marcar_actualizado_en() RETURNS trigger
    LANGUAGE plpgsql AS
$$
BEGIN
    NEW.actualizado_en := now();
    RETURN NEW;
END;
$$;

CREATE TABLE roles (
    id_rol     BIGSERIAL,
    nombre_rol VARCHAR(30) NOT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id_rol),
    CONSTRAINT ux_roles_nombre UNIQUE (nombre_rol),
    CONSTRAINT ck_roles_nombre_no_vacio CHECK (btrim(nombre_rol) <> '')
);

CREATE TABLE componentes (
    id_componente     BIGSERIAL,
    nombre_componente VARCHAR(100) NOT NULL,
    estado            VARCHAR(15) NOT NULL DEFAULT 'Activo',
    CONSTRAINT pk_componentes PRIMARY KEY (id_componente),
    CONSTRAINT ux_componentes_nombre UNIQUE (nombre_componente),
    CONSTRAINT ck_componentes_nombre_no_vacio CHECK (btrim(nombre_componente) <> ''),
    CONSTRAINT ck_componentes_estado CHECK (estado IN ('Activo', 'Desactivado'))
);

CREATE TABLE competencias (
    id_competencia     BIGSERIAL,
    nombre_competencia VARCHAR(100) NOT NULL,
    estado             VARCHAR(15) NOT NULL DEFAULT 'Activo',
    CONSTRAINT pk_competencias PRIMARY KEY (id_competencia),
    CONSTRAINT ux_competencias_nombre UNIQUE (nombre_competencia),
    CONSTRAINT ck_competencias_nombre_no_vacio CHECK (btrim(nombre_competencia) <> ''),
    CONSTRAINT ck_competencias_estado CHECK (estado IN ('Activo', 'Desactivado'))
);

CREATE TABLE niveles_dificultad (
    id_nivel_dificultad BIGSERIAL,
    nivel               VARCHAR(15) NOT NULL,
    CONSTRAINT pk_niveles_dificultad PRIMARY KEY (id_nivel_dificultad),
    CONSTRAINT ux_niveles_dificultad_nivel UNIQUE (nivel),
    CONSTRAINT ck_niveles_dificultad_nivel_no_vacio CHECK (btrim(nivel) <> '')
);

CREATE TABLE usuarios (
    id_usuario              BIGSERIAL,
    id_rol                  BIGINT       NOT NULL,
    nombre                  VARCHAR(30)  NOT NULL,
    apellido                VARCHAR(50)  NOT NULL,
    email                   VARCHAR(150) NOT NULL,
    google_sub              VARCHAR(255),
    password_hash           VARCHAR(255) NOT NULL,
    password_actualizado_en TIMESTAMPTZ  NOT NULL DEFAULT now(),
    acepto_terminos         BOOLEAN      NOT NULL,
    terminos_aceptados_en   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    intentos_fallidos_login SMALLINT     NOT NULL DEFAULT 0,
    bloqueado_hasta         TIMESTAMPTZ,
    ultimo_login_en         TIMESTAMPTZ,
    estado                  VARCHAR(10)  NOT NULL DEFAULT 'Activo',
    creado_en               TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_en          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_usuarios PRIMARY KEY (id_usuario),
    CONSTRAINT fk_usuarios_rol FOREIGN KEY (id_rol)
        REFERENCES roles (id_rol) ON DELETE RESTRICT,
    CONSTRAINT ux_usuarios_google_sub UNIQUE (google_sub),
    CONSTRAINT ck_usuarios_nombre_longitud CHECK (char_length(btrim(nombre)) BETWEEN 1 AND 30),
    CONSTRAINT ck_usuarios_apellido_longitud CHECK (char_length(btrim(apellido)) BETWEEN 2 AND 50),
    CONSTRAINT ck_usuarios_email_formato CHECK (email ~ '^[^[:space:]@]+@[^[:space:]@]+\.[^[:space:]@]+$'),
    CONSTRAINT ck_usuarios_password_hash_no_vacio CHECK (btrim(password_hash) <> ''),
    CONSTRAINT ck_usuarios_terminos_aceptados CHECK (acepto_terminos),
    CONSTRAINT ck_usuarios_intentos_fallidos_login CHECK (intentos_fallidos_login >= 0),
    CONSTRAINT ck_usuarios_estado CHECK (estado IN ('Activo', 'Inactivo'))
);

CREATE UNIQUE INDEX ux_usuarios_email ON usuarios (lower(email));

CREATE TRIGGER tg_usuarios_actualizado_en
    BEFORE UPDATE ON usuarios
    FOR EACH ROW EXECUTE FUNCTION fn_marcar_actualizado_en();

CREATE TABLE tokens_recuperacion (
    id_token   BIGSERIAL,
    id_usuario BIGINT       NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    creado_en  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    expira_en  TIMESTAMPTZ  NOT NULL,
    usado_en   TIMESTAMPTZ,
    CONSTRAINT pk_tokens_recuperacion PRIMARY KEY (id_token),
    CONSTRAINT fk_tokens_recuperacion_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuarios (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT ux_tokens_recuperacion_hash UNIQUE (token_hash),
    CONSTRAINT ck_tokens_recuperacion_hash_no_vacio CHECK (btrim(token_hash) <> ''),
    CONSTRAINT ck_tokens_recuperacion_vigencia CHECK (expira_en > creado_en),
    CONSTRAINT ck_tokens_recuperacion_usado_despues_de_creado CHECK (usado_en IS NULL OR usado_en >= creado_en)
);

CREATE INDEX ix_tokens_recuperacion_usuario_creado ON tokens_recuperacion (id_usuario, creado_en DESC);

CREATE TABLE tokens_sesion_revocados (
    jti         VARCHAR(64),
    id_usuario  BIGINT      NOT NULL,
    revocado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    expira_en   TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_tokens_sesion_revocados PRIMARY KEY (jti),
    CONSTRAINT fk_tokens_sesion_revocados_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuarios (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT ck_tokens_sesion_revocados_jti_no_vacio CHECK (btrim(jti) <> '')
);

CREATE INDEX ix_tokens_sesion_revocados_expira ON tokens_sesion_revocados (expira_en);

CREATE SEQUENCE ejercicios_numero_seq AS INTEGER START WITH 1;

CREATE TABLE ejercicios (
    id_ejercicio        BIGSERIAL,
    numero              INTEGER      NOT NULL DEFAULT nextval('ejercicios_numero_seq'),
    enunciado           TEXT         NOT NULL,
    imagen_enunciado    VARCHAR(255),
    id_componente       BIGINT       NOT NULL,
    id_competencia      BIGINT       NOT NULL,
    id_nivel_dificultad BIGINT       NOT NULL,
    id_usuario_creador  BIGINT       NOT NULL,
    estado              VARCHAR(15)  NOT NULL DEFAULT 'Activo',
    creado_en           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_en      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_ejercicios PRIMARY KEY (id_ejercicio),
    CONSTRAINT fk_ejercicios_componente FOREIGN KEY (id_componente)
        REFERENCES componentes (id_componente) ON DELETE RESTRICT,
    CONSTRAINT fk_ejercicios_competencia FOREIGN KEY (id_competencia)
        REFERENCES competencias (id_competencia) ON DELETE RESTRICT,
    CONSTRAINT fk_ejercicios_nivel_dificultad FOREIGN KEY (id_nivel_dificultad)
        REFERENCES niveles_dificultad (id_nivel_dificultad) ON DELETE RESTRICT,
    CONSTRAINT fk_ejercicios_usuario_creador FOREIGN KEY (id_usuario_creador)
        REFERENCES usuarios (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT ux_ejercicios_numero UNIQUE (numero),
    CONSTRAINT ck_ejercicios_numero_positivo CHECK (numero > 0),
    CONSTRAINT ck_ejercicios_enunciado_no_vacio CHECK (btrim(enunciado) <> ''),
    CONSTRAINT ck_ejercicios_imagen_enunciado_no_vacia CHECK (imagen_enunciado IS NULL OR btrim(imagen_enunciado) <> ''),
    CONSTRAINT ck_ejercicios_estado CHECK (estado IN ('Activo', 'Desactivado'))
);

ALTER SEQUENCE ejercicios_numero_seq OWNED BY ejercicios.numero;

CREATE UNIQUE INDEX ux_ejercicios_enunciado ON ejercicios (md5(lower(btrim(enunciado))));
CREATE INDEX ix_ejercicios_estado_numero ON ejercicios (estado, numero);
CREATE INDEX ix_ejercicios_componente_estado_numero ON ejercicios (id_componente, estado, numero);

CREATE TRIGGER tg_ejercicios_actualizado_en
    BEFORE UPDATE ON ejercicios
    FOR EACH ROW EXECUTE FUNCTION fn_marcar_actualizado_en();

CREATE TABLE opciones_respuesta (
    id_opcion          BIGSERIAL,
    id_ejercicio       BIGINT      NOT NULL,
    orden_opcion       SMALLINT    NOT NULL,
    descripcion_opcion TEXT,
    imagen_opcion      VARCHAR(255),
    es_correcta        BOOLEAN     NOT NULL DEFAULT FALSE,
    retroalimentacion  TEXT        NOT NULL,
    creado_en          TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_opciones_respuesta PRIMARY KEY (id_opcion),
    CONSTRAINT fk_opciones_respuesta_ejercicio FOREIGN KEY (id_ejercicio)
        REFERENCES ejercicios (id_ejercicio) ON DELETE CASCADE,
    CONSTRAINT ux_opciones_respuesta_opcion_ejercicio UNIQUE (id_opcion, id_ejercicio),
    CONSTRAINT ux_opciones_respuesta_orden UNIQUE (id_ejercicio, orden_opcion),
    CONSTRAINT ck_opciones_respuesta_orden CHECK (orden_opcion BETWEEN 1 AND 6),
    CONSTRAINT ck_opciones_respuesta_con_contenido CHECK (
        btrim(coalesce(descripcion_opcion, '')) <> '' OR btrim(coalesce(imagen_opcion, '')) <> ''),
    CONSTRAINT ck_opciones_respuesta_retroalimentacion_no_vacia CHECK (btrim(retroalimentacion) <> '')
);

CREATE UNIQUE INDEX ux_opciones_respuesta_correcta ON opciones_respuesta (id_ejercicio) WHERE es_correcta;
CREATE UNIQUE INDEX ux_opciones_respuesta_descripcion ON opciones_respuesta (id_ejercicio, md5(lower(btrim(descripcion_opcion))))
    WHERE descripcion_opcion IS NOT NULL;

CREATE TRIGGER tg_opciones_respuesta_actualizado_en
    BEFORE UPDATE ON opciones_respuesta
    FOR EACH ROW EXECUTE FUNCTION fn_marcar_actualizado_en();

CREATE FUNCTION fn_verificar_unica_opcion_correcta() RETURNS trigger
    LANGUAGE plpgsql AS
$$
DECLARE
    v_id_ejercicio BIGINT;
    v_correctas    INTEGER;
BEGIN
    IF TG_OP = 'DELETE' THEN
        v_id_ejercicio := OLD.id_ejercicio;
    ELSE
        v_id_ejercicio := NEW.id_ejercicio;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM ejercicios WHERE id_ejercicio = v_id_ejercicio) THEN
        RETURN NULL;
    END IF;

    SELECT count(*) INTO v_correctas
    FROM opciones_respuesta
    WHERE id_ejercicio = v_id_ejercicio AND es_correcta;

    IF v_correctas <> 1 THEN
        RAISE EXCEPTION 'ck_ejercicios_una_opcion_correcta: el ejercicio % tiene % opciones correctas', v_id_ejercicio, v_correctas
            USING ERRCODE = 'check_violation';
    END IF;

    RETURN NULL;
END;
$$;

CREATE CONSTRAINT TRIGGER tg_ejercicios_una_opcion_correcta
    AFTER INSERT ON ejercicios
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW EXECUTE FUNCTION fn_verificar_unica_opcion_correcta();

CREATE CONSTRAINT TRIGGER tg_opciones_respuesta_una_opcion_correcta
    AFTER INSERT OR UPDATE OR DELETE ON opciones_respuesta
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW EXECUTE FUNCTION fn_verificar_unica_opcion_correcta();

CREATE TABLE intentos (
    id_intento             BIGSERIAL,
    id_usuario             BIGINT      NOT NULL,
    id_ejercicio           BIGINT      NOT NULL,
    id_opcion_seleccionada BIGINT      NOT NULL,
    es_correcto            BOOLEAN     NOT NULL,
    nivel_confianza        SMALLINT    NOT NULL,
    token_idempotencia     UUID        NOT NULL,
    respondido_en          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_intentos PRIMARY KEY (id_intento),
    CONSTRAINT fk_intentos_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuarios (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT fk_intentos_opcion_del_ejercicio FOREIGN KEY (id_opcion_seleccionada, id_ejercicio)
        REFERENCES opciones_respuesta (id_opcion, id_ejercicio) ON DELETE RESTRICT,
    CONSTRAINT ux_intentos_token_idempotencia UNIQUE (token_idempotencia),
    CONSTRAINT ck_intentos_nivel_confianza CHECK (nivel_confianza BETWEEN 1 AND 5)
);

CREATE INDEX ix_intentos_usuario_respondido ON intentos (id_usuario, respondido_en DESC);

CREATE TABLE parametros_sistema (
    clave          VARCHAR(50),
    valor          VARCHAR(50)  NOT NULL,
    descripcion    VARCHAR(255) NOT NULL,
    creado_en      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_en TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_parametros_sistema PRIMARY KEY (clave),
    CONSTRAINT ck_parametros_sistema_clave_formato CHECK (clave ~ '^[a-z][a-z0-9_]*$'),
    CONSTRAINT ck_parametros_sistema_valor_no_vacio CHECK (btrim(valor) <> ''),
    CONSTRAINT ck_parametros_sistema_descripcion_no_vacia CHECK (btrim(descripcion) <> '')
);

CREATE TRIGGER tg_parametros_sistema_actualizado_en
    BEFORE UPDATE ON parametros_sistema
    FOR EACH ROW EXECUTE FUNCTION fn_marcar_actualizado_en();
