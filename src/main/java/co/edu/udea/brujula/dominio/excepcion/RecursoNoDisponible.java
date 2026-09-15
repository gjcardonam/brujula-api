package co.edu.udea.brujula.dominio.excepcion;

public class RecursoNoDisponible extends ErrorDeNegocio {
    public RecursoNoDisponible(String mensaje) {
        super("NO_DISPONIBLE", mensaje);
    }
}
