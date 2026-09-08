package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.Usuario;

/** HU-005. */
public interface GestionarPerfil {

    Usuario consultar(Long idUsuario);

    Usuario actualizarDatos(Long idUsuario, String nombre, String apellido);

    String cambiarContrasena(Long idUsuario, String actual, String nueva, String confirmacion);
}
