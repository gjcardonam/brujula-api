INSERT INTO roles (nombre_rol) VALUES
    ('Administrador'),
    ('Estudiante');

INSERT INTO componentes (nombre_componente) VALUES
    ('Álgebra y cálculo'),
    ('Geometría'),
    ('Estadística');

INSERT INTO competencias (nombre_competencia) VALUES
    ('Interpretación y representación'),
    ('Formulación y ejecución'),
    ('Argumentación');

INSERT INTO niveles_dificultad (nivel) VALUES
    ('Básico'),
    ('Intermedio'),
    ('Avanzado');

INSERT INTO parametros_sistema (clave, valor, descripcion) VALUES
    ('max_intentos_login', '5', 'Intentos fallidos consecutivos admitidos para un mismo correo antes del bloqueo'),
    ('minutos_bloqueo_login', '10', 'Minutos que permanece bloqueada la autenticación tras agotar los intentos'),
    ('horas_expiracion_sesion', '2', 'Horas de inactividad tras las cuales expira la sesión'),
    ('minutos_vigencia_recuperacion', '30', 'Minutos de vigencia del enlace de restablecimiento de contraseña'),
    ('max_solicitudes_recuperacion_hora', '3', 'Solicitudes de restablecimiento admitidas por correo en una hora'),
    ('tamano_pagina_banco', '20', 'Tarjetas de ejercicio mostradas por página en el banco');
