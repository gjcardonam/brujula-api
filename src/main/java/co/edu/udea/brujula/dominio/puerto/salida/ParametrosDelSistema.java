package co.edu.udea.brujula.dominio.puerto.salida;

public interface ParametrosDelSistema {

    String MAX_INTENTOS_LOGIN = "max_intentos_login";
    String MINUTOS_BLOQUEO_LOGIN = "minutos_bloqueo_login";
    String HORAS_EXPIRACION_SESION = "horas_expiracion_sesion";
    String MINUTOS_VIGENCIA_RECUPERACION = "minutos_vigencia_recuperacion";
    String MAX_SOLICITUDES_RECUPERACION_HORA = "max_solicitudes_recuperacion_hora";
    String TAMANO_PAGINA_BANCO = "tamano_pagina_banco";

    int entero(String clave, int valorPorDefecto);
}
