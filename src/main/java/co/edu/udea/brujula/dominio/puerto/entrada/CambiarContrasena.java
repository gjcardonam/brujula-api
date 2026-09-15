package co.edu.udea.brujula.dominio.puerto.entrada;

public interface CambiarContrasena {

    String cambiar(Long idUsuario, String actual, String nueva, String confirmacion);
}
