package co.edu.udea.brujula.dominio.excepcion;

import java.util.List;

/** Lo que envió el usuario no cumple las validaciones. Trae la lista de lo que falta o está mal. */
public class DatosInvalidos extends ErrorDeNegocio {

    public DatosInvalidos(List<String> detalles) {
        super("VALIDACION", "Revisa los datos ingresados.", detalles);
    }

    public DatosInvalidos(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
