package co.edu.udea.brujula.dominio.puerto.salida;

/** Ninguna contraseña se guarda en texto plano (HU-001 CA-13). */
public interface CifradorDeContrasenas {

    String cifrar(String contrasenaPlana);

    boolean coincide(String contrasenaPlana, String hash);

    /** Hash de un solo sentido para el token de recuperación, que tampoco se guarda en claro. */
    String resumen(String valor);
}
