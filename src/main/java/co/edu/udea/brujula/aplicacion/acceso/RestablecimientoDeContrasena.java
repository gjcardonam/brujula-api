package co.edu.udea.brujula.aplicacion.acceso;

import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.modelo.TokenRecuperacion;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.puerto.entrada.RestablecerContrasena;
import co.edu.udea.brujula.dominio.puerto.salida.CifradorDeContrasenas;
import co.edu.udea.brujula.dominio.puerto.salida.Reloj;
import co.edu.udea.brujula.dominio.puerto.salida.TokenRecuperacionRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.UsuarioRepositorio;
import co.edu.udea.brujula.dominio.servicio.PoliticaDeContrasena;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class RestablecimientoDeContrasena implements RestablecerContrasena {

    private final UsuarioRepositorio usuarios;
    private final TokenRecuperacionRepositorio tokensDeRecuperacion;
    private final CifradorDeContrasenas cifrador;
    private final Reloj reloj;

    public RestablecimientoDeContrasena(UsuarioRepositorio usuarios,
                                        TokenRecuperacionRepositorio tokensDeRecuperacion,
                                        CifradorDeContrasenas cifrador, Reloj reloj) {
        this.usuarios = usuarios;
        this.tokensDeRecuperacion = tokensDeRecuperacion;
        this.cifrador = cifrador;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public String restablecer(String token, String password, String confirmacion) {
        TokenRecuperacion vigente = buscarVigente(token);
        List<String> errores = PoliticaDeContrasena.revisar(password, confirmacion);
        if (!errores.isEmpty()) throw new DatosInvalidos(errores);

        Usuario usuario = usuarios.porId(vigente.idUsuario())
                .orElseThrow(() -> new NoEncontrado("La cuenta no existe."));
        Instant ahora = reloj.ahora();
        usuario.restablecerPassword(cifrador.cifrar(password), ahora);
        usuarios.guardar(usuario);
        tokensDeRecuperacion.guardar(vigente.marcarUsado(ahora));
        return "Tu contraseña fue actualizada. Ya puedes iniciar sesión.";
    }

    private TokenRecuperacion buscarVigente(String token) {
        if (token == null || token.isBlank()) {
            throw new DatosInvalidos("ENLACE_INVALIDO", TokenRecuperacion.ENLACE_INVALIDO);
        }
        return tokensDeRecuperacion.porHash(cifrador.resumen(token))
                .filter(guardado -> guardado.vigente(reloj.ahora()))
                .orElseThrow(() -> new DatosInvalidos("ENLACE_INVALIDO",
                        TokenRecuperacion.ENLACE_INVALIDO));
    }
}
