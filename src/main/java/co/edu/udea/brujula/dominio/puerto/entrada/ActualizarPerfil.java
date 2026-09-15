package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.Usuario;

public interface ActualizarPerfil {

    Usuario actualizar(Long idUsuario, String nombre, String apellido);
}
