package co.edu.udea.brujula.dominio.puerto.salida;

public interface ParametrosDelSistema {

    String VENTANA_INTENTOS = "ventana_intentos";
    String UMBRAL_DOMINIO_PCT = "umbral_dominio_pct";
    String MINIMO_INTENTOS_VENTANA = "minimo_intentos_ventana";
    String MAX_INTENTOS_LOGIN = "max_intentos_login";
    String MINUTOS_BLOQUEO_LOGIN = "minutos_bloqueo_login";
    String HORAS_EXPIRACION_SESION = "horas_expiracion_sesion";
    String MINUTOS_VIGENCIA_RECUPERACION = "minutos_vigencia_recuperacion";
    String MAX_SOLICITUDES_RECUPERACION_HORA = "max_solicitudes_recuperacion_hora";
    String TAMANO_PAGINA_BANCO = "tamano_pagina_banco";

    int entero(String clave, int valorPorDefecto);
}
