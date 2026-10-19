CREATE TABLE tipos_error (
    id_tipo_error     BIGSERIAL,
    nombre_tipo_error VARCHAR(30) NOT NULL,
    CONSTRAINT pk_tipos_error PRIMARY KEY (id_tipo_error),
    CONSTRAINT ux_tipos_error_nombre UNIQUE (nombre_tipo_error),
    CONSTRAINT ck_tipos_error_nombre_no_vacio CHECK (btrim(nombre_tipo_error) <> '')
);

INSERT INTO tipos_error (nombre_tipo_error) VALUES
    ('Error cognitivo'),
    ('Error de hábito'),
    ('Error de ansiedad');

ALTER TABLE opciones_respuesta ADD COLUMN id_tipo_error BIGINT;

ALTER TABLE opciones_respuesta ADD CONSTRAINT fk_opciones_respuesta_tipo_error
    FOREIGN KEY (id_tipo_error) REFERENCES tipos_error (id_tipo_error) ON DELETE RESTRICT;

ALTER TABLE intentos ADD COLUMN id_tipo_error BIGINT;

ALTER TABLE intentos ADD CONSTRAINT fk_intentos_tipo_error
    FOREIGN KEY (id_tipo_error) REFERENCES tipos_error (id_tipo_error) ON DELETE RESTRICT;

CREATE INDEX ix_intentos_tipo_error ON intentos (id_tipo_error);

CREATE TABLE auditoria_ejercicios (
    id_auditoria     BIGSERIAL,
    tipo_accion      VARCHAR(15) NOT NULL,
    fecha_accion     TIMESTAMPTZ NOT NULL DEFAULT now(),
    id_ejercicio     BIGINT      NOT NULL,
    id_usuario_actor BIGINT      NOT NULL,
    CONSTRAINT pk_auditoria_ejercicios PRIMARY KEY (id_auditoria),
    CONSTRAINT fk_auditoria_ejercicios_ejercicio FOREIGN KEY (id_ejercicio)
        REFERENCES ejercicios (id_ejercicio) ON DELETE RESTRICT,
    CONSTRAINT fk_auditoria_ejercicios_usuario FOREIGN KEY (id_usuario_actor)
        REFERENCES usuarios (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT ck_auditoria_ejercicios_accion CHECK (
        tipo_accion IN ('Creación', 'Edición', 'Activación', 'Desactivación'))
);

CREATE INDEX ix_auditoria_ejercicios_ejercicio ON auditoria_ejercicios (id_ejercicio, fecha_accion DESC);

INSERT INTO parametros_sistema (clave, valor, descripcion) VALUES
    ('ventana_intentos', '5',
     'Últimos intentos considerados sobre la misma competencia o componente para clasificar el error'),
    ('umbral_dominio_pct', '70',
     'Porcentaje de aciertos en la ventana para considerar que el estudiante domina el área'),
    ('minimo_intentos_ventana', '3',
     'Intentos previos mínimos para evaluar si el estudiante domina el área')
ON CONFLICT (clave) DO NOTHING;
