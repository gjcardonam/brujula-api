ALTER TABLE intentos ADD COLUMN tiempo_respuesta_ms INTEGER;

ALTER TABLE intentos ADD CONSTRAINT ck_intentos_tiempo_respuesta_positivo
    CHECK (tiempo_respuesta_ms IS NULL OR tiempo_respuesta_ms > 0);
