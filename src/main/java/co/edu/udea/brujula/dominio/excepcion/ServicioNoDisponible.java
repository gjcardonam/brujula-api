package co.edu.udea.brujula.dominio.excepcion;

/** Falló algo de afuera (Google, por ejemplo) y no es culpa del usuario. */
public class ServicioNoDisponible extends ErrorDeNegocio {
    public ServicioNoDisponible(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
