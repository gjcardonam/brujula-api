package co.edu.udea.brujula.dominio.puerto.salida;

public interface VerificadorDeGoogle {

    record CuentaDeGoogle(String sub, String email, String nombre, String apellido) {
    }

    CuentaDeGoogle verificar(String credencial);

    boolean estaSimulado();
}
