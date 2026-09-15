package co.edu.udea.brujula.dominio.modelo;

public record Opcion(Long id, String texto, String imagen, boolean correcta, String retroalimentacion, int orden) {

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

    public String claveDeComparacion() {
        String descripcion = texto == null ? "" : texto.trim().toLowerCase();
        String archivo = imagen == null ? "" : imagen.trim();
        return descripcion + "|" + archivo;
    }
}
