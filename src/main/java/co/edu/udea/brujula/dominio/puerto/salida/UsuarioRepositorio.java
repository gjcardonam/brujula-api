package co.edu.udea.brujula.dominio.puerto.salida;

import co.edu.udea.brujula.dominio.modelo.Usuario;

import java.util.Optional;

public interface UsuarioRepositorio {

    Optional<Usuario> porId(Long id);

    Optional<Usuario> porEmail(String email);

    boolean existeConEmail(String email);

    boolean existeAlgunAdministrador();

    Usuario guardar(Usuario usuario);
}
