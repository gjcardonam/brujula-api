package co.edu.udea.brujula.aplicacion.perfil;

import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.puerto.entrada.CambiarContrasena;
import co.edu.udea.brujula.dominio.puerto.salida.CifradorDeContrasenas;
import co.edu.udea.brujula.dominio.puerto.salida.Reloj;
import co.edu.udea.brujula.dominio.puerto.salida.UsuarioRepositorio;
import co.edu.udea.brujula.dominio.servicio.PoliticaDeContrasena;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CambioDeContrasena implements CambiarContrasena {

    private final UsuarioRepositorio usuarios;
    private final CifradorDeContrasenas cifrador;
    private final Reloj reloj;

    public CambioDeContrasena(UsuarioRepositorio usuarios, CifradorDeContrasenas cifrador, Reloj reloj) {
        this.usuarios = usuarios;
        this.cifrador = cifrador;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public String cambiar(Long idUsuario, String actual, String nueva, String confirmacion) {
        Usuario usuario = usuarios.porId(idUsuario).orElseThrow(() -> new NoEncontrado("La cuenta no existe."));
        if (actual == null || !cifrador.coincide(actual, usuario.passwordHash())) {
            throw new DatosInvalidos("PASSWORD_ACTUAL_INCORRECTA",
                    "La contraseña actual no coincide con la registrada.");
        }
        List<String> errores = PoliticaDeContrasena.revisar(nueva, confirmacion);
        if (!errores.isEmpty()) throw new DatosInvalidos(errores);

        usuario.cambiarPassword(cifrador.cifrar(nueva), reloj.ahora());
        usuarios.guardar(usuario);
        return "Tu contraseña fue actualizada correctamente.";
    }
}
