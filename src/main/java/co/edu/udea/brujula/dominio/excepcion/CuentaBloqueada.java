package co.edu.udea.brujula.dominio.excepcion;

public class CuentaBloqueada extends ErrorDeNegocio {
    public CuentaBloqueada(String mensaje) {
        super("CUENTA_BLOQUEADA", mensaje);
    }
}
