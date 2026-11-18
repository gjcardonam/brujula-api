ALTER TABLE intentos
    ADD COLUMN id_componente       BIGINT,
    ADD COLUMN id_competencia      BIGINT,
    ADD COLUMN id_nivel_dificultad BIGINT,
    ADD COLUMN enunciado           TEXT,
    ADD COLUMN imagen_enunciado    VARCHAR(255),
    ADD COLUMN retroalimentacion   TEXT;

UPDATE intentos i
SET id_componente       = e.id_componente,
    id_competencia      = e.id_competencia,
    id_nivel_dificultad = e.id_nivel_dificultad,
    enunciado           = e.enunciado,
    imagen_enunciado    = e.imagen_enunciado,
    retroalimentacion   = o.retroalimentacion
FROM ejercicios e, opciones_respuesta o
WHERE e.id_ejercicio = i.id_ejercicio
  AND o.id_opcion = i.id_opcion_seleccionada;

ALTER TABLE intentos
    ALTER COLUMN id_componente SET NOT NULL,
    ALTER COLUMN id_competencia SET NOT NULL,
    ALTER COLUMN id_nivel_dificultad SET NOT NULL,
    ALTER COLUMN enunciado SET NOT NULL,
    ADD CONSTRAINT fk_intentos_componente FOREIGN KEY (id_componente)
        REFERENCES componentes (id_componente) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_intentos_competencia FOREIGN KEY (id_competencia)
        REFERENCES competencias (id_competencia) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_intentos_nivel_dificultad FOREIGN KEY (id_nivel_dificultad)
        REFERENCES niveles_dificultad (id_nivel_dificultad) ON DELETE RESTRICT;

CREATE INDEX ix_intentos_usuario_componente ON intentos (id_usuario, id_componente);
CREATE INDEX ix_intentos_usuario_competencia ON intentos (id_usuario, id_competencia);
