package co.edu.udea.brujula.dominio.excepcion;

public class AccesoDenegado extends ErrorDeNegocio {
    public AccesoDenegado(String mensaje) {
        super("ACCESO_DENEGADO", mensaje);
    }
}
