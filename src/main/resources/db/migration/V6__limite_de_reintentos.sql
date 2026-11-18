INSERT INTO parametros_sistema (clave, valor, descripcion) VALUES
    ('max_intentos_practica', '2',
     'Intentos de práctica libre permitidos por estudiante en cada ejercicio (los de simulacro no cuentan)'),
    ('horas_espera_reintento', '24',
     'Horas que deben pasar desde el último intento de práctica para volver a intentar el mismo ejercicio')
ON CONFLICT (clave) DO NOTHING;
