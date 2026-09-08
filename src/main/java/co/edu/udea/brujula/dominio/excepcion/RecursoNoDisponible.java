package co.edu.udea.brujula.dominio.excepcion;

/** El recurso existe pero ya no se puede usar, como un ejercicio que acaban de desactivar. */
public class RecursoNoDisponible extends ErrorDeNegocio {
    public RecursoNoDisponible(String mensaje) {
        super("NO_DISPONIBLE", mensaje);
    }
}
