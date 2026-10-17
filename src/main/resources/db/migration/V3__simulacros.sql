CREATE TABLE duraciones_simulacro (
    id_duracion      BIGSERIAL,
    duracion_minutos SMALLINT NOT NULL,
    CONSTRAINT pk_duraciones_simulacro PRIMARY KEY (id_duracion),
    CONSTRAINT ux_duraciones_simulacro_minutos UNIQUE (duracion_minutos),
    CONSTRAINT ck_duraciones_simulacro_minutos_positivos CHECK (duracion_minutos > 0)
);

INSERT INTO duraciones_simulacro (duracion_minutos) VALUES
    (30),
    (45),
    (60),
    (90),
    (120);

CREATE TABLE simulacros (
    id_simulacro         BIGSERIAL,
    id_usuario           BIGINT      NOT NULL,
    id_duracion          BIGINT      NOT NULL,
    fecha_inicio         TIMESTAMPTZ NOT NULL DEFAULT now(),
    fecha_fin            TIMESTAMPTZ,
    estado               VARCHAR(15) NOT NULL DEFAULT 'En curso',
    tiempo_utilizado_seg INTEGER,
    CONSTRAINT pk_simulacros PRIMARY KEY (id_simulacro),
    CONSTRAINT fk_simulacros_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuarios (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT fk_simulacros_duracion FOREIGN KEY (id_duracion)
        REFERENCES duraciones_simulacro (id_duracion) ON DELETE RESTRICT,
    CONSTRAINT ux_simulacros_simulacro_usuario UNIQUE (id_simulacro, id_usuario),
    CONSTRAINT ck_simulacros_estado CHECK (estado IN ('En curso', 'Finalizado')),
    CONSTRAINT ck_simulacros_cierre_completo CHECK (
        (estado = 'En curso' AND fecha_fin IS NULL AND tiempo_utilizado_seg IS NULL)
        OR (estado = 'Finalizado' AND fecha_fin IS NOT NULL AND tiempo_utilizado_seg IS NOT NULL)),
    CONSTRAINT ck_simulacros_fin_despues_de_inicio CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio),
    CONSTRAINT ck_simulacros_tiempo_utilizado CHECK (tiempo_utilizado_seg IS NULL OR tiempo_utilizado_seg >= 0)
);

CREATE INDEX ix_simulacros_usuario_inicio ON simulacros (id_usuario, fecha_inicio DESC);
CREATE UNIQUE INDEX ux_simulacros_uno_en_curso_por_usuario ON simulacros (id_usuario) WHERE estado = 'En curso';

ALTER TABLE intentos ADD COLUMN id_simulacro BIGINT;

ALTER TABLE intentos ADD CONSTRAINT fk_intentos_simulacro_del_usuario FOREIGN KEY (id_simulacro, id_usuario)
    REFERENCES simulacros (id_simulacro, id_usuario) ON DELETE RESTRICT;

CREATE UNIQUE INDEX ux_intentos_simulacro_ejercicio ON intentos (id_simulacro, id_ejercicio)
    WHERE id_simulacro IS NOT NULL;

CREATE FUNCTION fn_verificar_simulacro_en_curso() RETURNS trigger
    LANGUAGE plpgsql AS
$$
BEGIN
    IF NEW.id_simulacro IS NULL THEN
        RETURN NEW;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM simulacros WHERE id_simulacro = NEW.id_simulacro AND estado = 'En curso') THEN
        RAISE EXCEPTION 'ck_intentos_simulacro_en_curso: el simulacro % ya finalizó', NEW.id_simulacro
            USING ERRCODE = 'check_violation';
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER tg_intentos_simulacro_en_curso
    BEFORE INSERT OR UPDATE OF id_simulacro ON intentos
    FOR EACH ROW EXECUTE FUNCTION fn_verificar_simulacro_en_curso();

CREATE TABLE recomendaciones_estudio (
    id_recomendacion      BIGSERIAL,
    id_simulacro          BIGINT       NOT NULL,
    orden_prioridad       SMALLINT     NOT NULL,
    id_componente         BIGINT,
    id_competencia        BIGINT,
    porcentaje_aciertos   NUMERIC(5,2) NOT NULL,
    mensaje_recomendacion TEXT         NOT NULL,
    creado_en             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_recomendaciones_estudio PRIMARY KEY (id_recomendacion),
    CONSTRAINT fk_recomendaciones_estudio_simulacro FOREIGN KEY (id_simulacro)
        REFERENCES simulacros (id_simulacro) ON DELETE RESTRICT,
    CONSTRAINT fk_recomendaciones_estudio_componente FOREIGN KEY (id_componente)
        REFERENCES componentes (id_componente) ON DELETE RESTRICT,
    CONSTRAINT fk_recomendaciones_estudio_competencia FOREIGN KEY (id_competencia)
        REFERENCES competencias (id_competencia) ON DELETE RESTRICT,
    CONSTRAINT ux_recomendaciones_estudio_orden UNIQUE (id_simulacro, orden_prioridad),
    CONSTRAINT ck_recomendaciones_estudio_una_sola_area CHECK (num_nonnulls(id_componente, id_competencia) = 1),
    CONSTRAINT ck_recomendaciones_estudio_orden_positivo CHECK (orden_prioridad > 0),
    CONSTRAINT ck_recomendaciones_estudio_porcentaje CHECK (porcentaje_aciertos BETWEEN 0 AND 100),
    CONSTRAINT ck_recomendaciones_estudio_mensaje_no_vacio CHECK (btrim(mensaje_recomendacion) <> '')
);

INSERT INTO parametros_sistema (clave, valor, descripcion) VALUES
    ('umbral_recomendacion_pct', '60',
     'Porcentaje de aciertos por debajo del cual un componente o una competencia se recomienda reforzar')
ON CONFLICT (clave) DO NOTHING;
