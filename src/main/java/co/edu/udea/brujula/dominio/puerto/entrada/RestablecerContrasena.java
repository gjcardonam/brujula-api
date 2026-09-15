package co.edu.udea.brujula.dominio.puerto.entrada;

public interface RestablecerContrasena {

    String restablecer(String token, String password, String confirmacion);
}
