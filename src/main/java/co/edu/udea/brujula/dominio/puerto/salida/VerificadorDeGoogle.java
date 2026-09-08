package co.edu.udea.brujula.dominio.puerto.salida;

public interface VerificadorDeGoogle {

    record CuentaDeGoogle(String sub, String email, String nombre, String apellido) {
    }

    /** Valida el ID token que entrega el botón de Google y devuelve el correo ya verificado. */
    CuentaDeGoogle verificar(String credencial);

    /** En desarrollo, sin credenciales de Google, el inicio se simula para poder probar el registro. */
    boolean estaSimulado();
}
