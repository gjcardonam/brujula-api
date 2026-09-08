package co.edu.udea.brujula.dominio.excepcion;

public class NoEncontrado extends ErrorDeNegocio {
    public NoEncontrado(String mensaje) {
        super("NO_ENCONTRADO", mensaje);
    }
}
