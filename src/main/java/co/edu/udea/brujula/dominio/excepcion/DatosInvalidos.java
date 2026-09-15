package co.edu.udea.brujula.dominio.excepcion;

import java.util.List;

public class DatosInvalidos extends ErrorDeNegocio {

    public DatosInvalidos(List<String> detalles) {
        super("VALIDACION", "Revisa los datos ingresados.", detalles);
    }

    public DatosInvalidos(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
