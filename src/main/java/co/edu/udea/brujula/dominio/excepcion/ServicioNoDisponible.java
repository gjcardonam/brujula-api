package co.edu.udea.brujula.dominio.excepcion;

public class ServicioNoDisponible extends ErrorDeNegocio {
    public ServicioNoDisponible(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
