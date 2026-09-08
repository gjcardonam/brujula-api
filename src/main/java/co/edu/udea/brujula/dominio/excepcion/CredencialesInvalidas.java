package co.edu.udea.brujula.dominio.excepcion;

/**
 * Mensaje único para correo inexistente y contraseña equivocada: si fueran distintos se podría
 * averiguar qué correos están registrados (HU-002 CA-04).
 */
public class CredencialesInvalidas extends ErrorDeNegocio {
    public CredencialesInvalidas() {
        super("CREDENCIALES_INVALIDAS", "Las credenciales ingresadas no son válidas.");
    }
}
