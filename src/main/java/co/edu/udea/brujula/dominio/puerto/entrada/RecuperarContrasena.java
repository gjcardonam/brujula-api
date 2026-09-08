package co.edu.udea.brujula.dominio.puerto.entrada;

/** HU-003. */
public interface RecuperarContrasena {

    /** Devuelve siempre el mismo mensaje, exista o no la cuenta (CA-03). */
    String solicitarEnlace(String email);

    void verificarEnlace(String token);

    String restablecer(String token, String password, String confirmacion);
}
