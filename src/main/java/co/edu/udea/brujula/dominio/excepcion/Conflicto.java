package co.edu.udea.brujula.dominio.excepcion;

import java.util.List;
import java.util.Map;

/** El estado actual no permite la operación: ya existe, ya finalizó, ya fue respondido. */
public class Conflicto extends ErrorDeNegocio {

    public Conflicto(String codigo, String mensaje) {
        super(codigo, mensaje);
    }

    public Conflicto(String codigo, String mensaje, Map<String, Object> datos) {
        super(codigo, mensaje, List.of(), datos);
    }
}
