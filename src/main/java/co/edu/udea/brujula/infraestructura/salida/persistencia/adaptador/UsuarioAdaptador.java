package co.edu.udea.brujula.infraestructura.salida.persistencia.adaptador;

import co.edu.udea.brujula.dominio.modelo.Rol;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.puerto.salida.UsuarioRepositorio;
import co.edu.udea.brujula.infraestructura.salida.persistencia.Mapeador;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.UsuarioEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.RolJpa;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.UsuarioJpa;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsuarioAdaptador implements UsuarioRepositorio {

    private final UsuarioJpa usuarios;
    private final RolJpa roles;

    public UsuarioAdaptador(UsuarioJpa usuarios, RolJpa roles) {
        this.usuarios = usuarios;
        this.roles = roles;
    }

    @Override
    public Optional<Usuario> porId(Long id) {
        return usuarios.findById(id).map(Mapeador::aDominio);
    }

    @Override
    public Optional<Usuario> porEmail(String email) {
        return usuarios.findByEmailIgnoreCase(email).map(Mapeador::aDominio);
    }

    @Override
    public boolean existeConEmail(String email) {
        return usuarios.existsByEmailIgnoreCase(email);
    }

    @Override
    public boolean existeAlgunAdministrador() {
        return usuarios.existsByRol_Nombre(Rol.ADMINISTRADOR);
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        UsuarioEntidad entidad = usuario.id() == null
                ? new UsuarioEntidad()
                : usuarios.findById(usuario.id()).orElseGet(UsuarioEntidad::new);

        entidad.setNombre(usuario.nombre());
        entidad.setApellido(usuario.apellido());
        entidad.setEmail(usuario.email());
        entidad.setGoogleSub(usuario.googleSub());
        entidad.setPasswordHash(usuario.passwordHash());
        entidad.setAceptoTerminos(usuario.aceptoTerminos());
        entidad.setFechaAceptacionTerminos(usuario.fechaAceptacionTerminos());
        entidad.setCreadoEn(usuario.creadoEn());
        entidad.setIntentosFallidos(usuario.intentosFallidos());
        entidad.setUltimoLogin(usuario.ultimoLogin());
        entidad.setFechaBloqueo(usuario.fechaBloqueo());
        entidad.setEstado(usuario.estado());
        entidad.setPasswordActualizadoEn(usuario.passwordActualizadoEn());
        // Se carga el rol de verdad y no una referencia perezosa: guardar() se llama también desde
        // el arranque, fuera de una transacción, y allí un proxy sin sesión falla al leerse.
        entidad.setRol(roles.findById(usuario.rol().id()).orElseThrow());

        UsuarioEntidad guardado = usuarios.save(entidad);
        usuario.asignarId(guardado.getId());
        return Mapeador.aDominio(guardado);
    }
}
