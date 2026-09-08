package co.edu.udea.brujula.apoyo.dobles;

import co.edu.udea.brujula.dominio.modelo.Rol;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.puerto.salida.UsuarioRepositorio;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class UsuariosEnMemoria implements UsuarioRepositorio {

    private final Map<Long, Usuario> porId = new LinkedHashMap<>();
    private long siguienteId = 1;

    public Usuario agregar(Usuario usuario) {
        if (usuario.id() == null) usuario.asignarId(siguienteId++);
        porId.put(usuario.id(), usuario);
        return usuario;
    }

    @Override
    public Optional<Usuario> porId(Long id) {
        return Optional.ofNullable(porId.get(id));
    }

    @Override
    public Optional<Usuario> porEmail(String email) {
        return porId.values().stream().filter(u -> u.email().equalsIgnoreCase(email)).findFirst();
    }

    @Override
    public boolean existeConEmail(String email) {
        return porEmail(email).isPresent();
    }

    @Override
    public boolean existeAlgunAdministrador() {
        return porId.values().stream().anyMatch(u -> Rol.ADMINISTRADOR.equals(u.rol().nombre()));
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        return agregar(usuario);
    }
}
