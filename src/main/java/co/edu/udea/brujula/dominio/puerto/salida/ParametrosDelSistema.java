package co.edu.udea.brujula.dominio.puerto.salida;

/**
 * Umbrales y límites que las historias piden que sean configurables y no queden fijos en el código
 * (HU-028 sobre todo). Viven en la tabla parametros_sistema.
 */
public interface ParametrosDelSistema {

    String VENTANA_INTENTOS = "ventana_intentos";
    String UMBRAL_DOMINIO_PCT = "umbral_dominio_pct";
    String MINIMO_INTENTOS_VENTANA = "minimo_intentos_ventana";
    String UMBRAL_RECOMENDACION_PCT = "umbral_recomendacion_pct";
    String MAX_INTENTOS_LOGIN = "max_intentos_login";
    String MINUTOS_BLOQUEO_LOGIN = "minutos_bloqueo_login";
    String HORAS_EXPIRACION_SESION = "horas_expiracion_sesion";
    String MINUTOS_VIGENCIA_RECUPERACION = "minutos_vigencia_recuperacion";
    String MAX_SOLICITUDES_RECUPERACION_HORA = "max_solicitudes_recuperacion_hora";
    String TAMANO_PAGINA_BANCO = "tamano_pagina_banco";

    int entero(String clave, int valorPorDefecto);
}
