package co.edu.udea.brujula.catalogo;

import org.springframework.stereotype.Service;

/**
 * Lee los valores de parametros_sistema. Las HU exigen que estas reglas sean configurables
 * y no queden fijas en el código (HU-028, HU-018, HU-002, HU-003, HU-004, HU-008).
 * Se consultan en cada uso: son pocas filas y así un cambio en BD aplica de inmediato.
 */
@Service
public class ParametrosService {
    public static final String VENTANA_INTENTOS = "ventana_intentos";
    public static final String UMBRAL_DOMINIO_PCT = "umbral_dominio_pct";
    public static final String MINIMO_INTENTOS_VENTANA = "minimo_intentos_ventana";
    public static final String UMBRAL_RECOMENDACION_PCT = "umbral_recomendacion_pct";
    public static final String MAX_INTENTOS_LOGIN = "max_intentos_login";
    public static final String MINUTOS_BLOQUEO_LOGIN = "minutos_bloqueo_login";
    public static final String HORAS_EXPIRACION_SESION = "horas_expiracion_sesion";
    public static final String MINUTOS_VIGENCIA_RECUPERACION = "minutos_vigencia_recuperacion";
    public static final String MAX_SOLICITUDES_RECUPERACION_HORA = "max_solicitudes_recuperacion_hora";
    public static final String TAMANO_PAGINA_BANCO = "tamano_pagina_banco";

    private final ParametroSistemaRepository repo;

    public ParametrosService(ParametroSistemaRepository repo) {
        this.repo = repo;
    }

    public int entero(String clave, int porDefecto) {
        return repo.findById(clave).map(p -> {
            try {
                return Integer.parseInt(p.getValor().trim());
            } catch (NumberFormatException e) {
                return porDefecto;
            }
        }).orElse(porDefecto);
    }
}
