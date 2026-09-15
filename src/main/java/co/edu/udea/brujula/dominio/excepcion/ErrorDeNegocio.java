package co.edu.udea.brujula.dominio.excepcion;

import java.util.List;
import java.util.Map;

public class ErrorDeNegocio extends RuntimeException {

    private final String codigo;
    private final List<String> detalles;
    private final Map<String, Object> datos;

    public ErrorDeNegocio(String codigo, String mensaje) {
        this(codigo, mensaje, List.of(), Map.of());
    }

    public ErrorDeNegocio(String codigo, String mensaje, List<String> detalles) {
        this(codigo, mensaje, detalles, Map.of());
    }

    public ErrorDeNegocio(String codigo, String mensaje, List<String> detalles, Map<String, Object> datos) {
        super(mensaje);
        this.codigo = codigo;
        this.detalles = detalles;
        this.datos = datos;
    }

    public String codigo() { return codigo; }
    public List<String> detalles() { return detalles; }
    public Map<String, Object> datos() { return datos; }
}
