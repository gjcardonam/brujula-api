-- Brújula · Catálogos iniciales
-- En PI1 los componentes y competencias se cargan directamente en la base de datos
-- (su administración por interfaz es alcance de PI II, HU-030 a HU-035).

INSERT INTO roles (nombre_rol) VALUES ('Administrador'), ('Estudiante');

-- Categorías de contenido de la prueba Saber 11.º (guía de orientación 2026-2, Calendario A).
INSERT INTO componentes (nombre_componente) VALUES
    ('Álgebra y cálculo'),
    ('Geometría'),
    ('Estadística');

INSERT INTO competencias (nombre_competencia) VALUES
    ('Interpretación y representación'),
    ('Formulación y ejecución'),
    ('Argumentación');

INSERT INTO niveles_dificultad (nivel) VALUES ('Básico'), ('Intermedio'), ('Avanzado');

INSERT INTO tipos_error (nombre_tipo_error) VALUES
    ('Error cognitivo'),
    ('Error de hábito'),
    ('Error de ansiedad');

INSERT INTO duraciones_simulacro (duracion_minutos) VALUES (30), (45), (60), (90), (120);

INSERT INTO parametros_sistema (clave, valor, descripcion) VALUES
    ('ventana_intentos',              '5',  'HU-028 CA-01: últimos N intentos sobre la misma competencia o componente'),
    ('umbral_dominio_pct',            '70', 'HU-028 CA-02: porcentaje de aciertos en la ventana para considerar dominio'),
    ('minimo_intentos_ventana',       '3',  'HU-028 CA-04: intentos previos mínimos para evaluar inconsistencia'),
    ('umbral_recomendacion_pct',      '60', 'HU-018 CA-02: por debajo de este porcentaje se recomienda reforzar'),
    ('max_intentos_login',            '5',  'HU-002 CA-08: intentos fallidos consecutivos antes del bloqueo'),
    ('minutos_bloqueo_login',         '10', 'HU-002 CA-08: minutos de bloqueo temporal'),
    ('horas_expiracion_sesion',       '2',  'HU-004 CA-05: horas de inactividad para expirar la sesión'),
    ('minutos_vigencia_recuperacion', '30', 'HU-003 CA-02: vigencia del enlace de restablecimiento'),
    ('max_solicitudes_recuperacion_hora', '3', 'HU-003 CA-09: solicitudes de restablecimiento por correo y hora'),
    ('tamano_pagina_banco',           '20', 'HU-008 CA-01: tarjetas por página en el banco de ejercicios');
