package co.edu.udea.brujula.dominio.excepcion;

public class CredencialesInvalidas extends ErrorDeNegocio {
    public CredencialesInvalidas() {
        super("CREDENCIALES_INVALIDAS", "Las credenciales ingresadas no son válidas.");
    }
}
