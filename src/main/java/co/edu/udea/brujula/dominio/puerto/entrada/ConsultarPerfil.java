package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.Usuario;

public interface ConsultarPerfil {

    Usuario consultar(Long idUsuario);
}
