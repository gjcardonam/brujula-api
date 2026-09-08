package co.edu.udea.brujula.aplicacion;

import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.puerto.entrada.GestionarPerfil;
import co.edu.udea.brujula.dominio.puerto.salida.CifradorDeContrasenas;
import co.edu.udea.brujula.dominio.puerto.salida.UsuarioRepositorio;
import co.edu.udea.brujula.dominio.servicio.PoliticaDeContrasena;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PerfilDeUsuario implements GestionarPerfil {

    private final UsuarioRepositorio usuarios;
    private final CifradorDeContrasenas cifrador;

    public PerfilDeUsuario(UsuarioRepositorio usuarios, CifradorDeContrasenas cifrador) {
        this.usuarios = usuarios;
        this.cifrador = cifrador;
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario consultar(Long idUsuario) {
        return buscar(idUsuario);
    }

    @Override
    @Transactional
    public Usuario actualizarDatos(Long idUsuario, String nombre, String apellido) {
        List<String> errores = PoliticaDeContrasena.revisarNombres(nombre, apellido);
        if (!errores.isEmpty()) throw new DatosInvalidos(errores);

        Usuario usuario = buscar(idUsuario);
        usuario.actualizarDatosPersonales(nombre, apellido);   // el correo viene de Google y no se toca (CA-03)
        return usuarios.guardar(usuario);
    }

    @Override
    @Transactional
    public String cambiarContrasena(Long idUsuario, String actual, String nueva, String confirmacion) {
        Usuario usuario = buscar(idUsuario);
        if (actual == null || !cifrador.coincide(actual, usuario.passwordHash())) {
            throw new DatosInvalidos("PASSWORD_ACTUAL_INCORRECTA",
                    "La contraseña actual no coincide con la registrada.");
        }
        List<String> errores = PoliticaDeContrasena.revisar(nueva, confirmacion);
        if (!errores.isEmpty()) throw new DatosInvalidos(errores);

        usuario.cambiarPassword(cifrador.cifrar(nueva));
        usuarios.guardar(usuario);
        return "Tu contraseña fue actualizada correctamente.";
    }

    private Usuario buscar(Long idUsuario) {
        return usuarios.porId(idUsuario).orElseThrow(() -> new NoEncontrado("La cuenta no existe."));
    }
}
