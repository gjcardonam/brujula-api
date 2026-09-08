package co.edu.udea.brujula.dominio.modelo;

/**
 * Opción de respuesta. El tipo de error es la clasificación del distractor, que alimenta el motor
 * de reglas; en la opción correcta va en nulo.
 */
public record Opcion(Long id, String texto, String imagen, boolean correcta, String retroalimentacion,
                     TipoError tipoError, int orden) {

    /** Lo que se le muestra al estudiante cuando la opción quedó sin explicación (HU-011 CA-06). */
    public static final String SIN_RETROALIMENTACION = "No hay una explicación disponible para esta respuesta.";

    public String retroalimentacionParaMostrar() {
        return tieneRetroalimentacion() ? retroalimentacion : SIN_RETROALIMENTACION;
    }

    public String letra() {
        return String.valueOf((char) ('A' + Math.max(0, orden - 1)));
    }

    public boolean tieneRetroalimentacion() {
        return retroalimentacion != null && !retroalimentacion.isBlank();
    }

    public boolean tieneContenido() {
        return (texto != null && !texto.isBlank()) || (imagen != null && !imagen.isBlank());
    }

    /** Dos opciones se consideran repetidas si dicen lo mismo o usan la misma imagen (HU-020 CA-08). */
    public String claveDeComparacion() {
        String t = texto == null ? "" : texto.trim().toLowerCase();
        String i = imagen == null ? "" : imagen.trim();
        return t + "|" + i;
    }
}
