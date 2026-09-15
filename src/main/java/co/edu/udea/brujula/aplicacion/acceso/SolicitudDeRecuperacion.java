package co.edu.udea.brujula.aplicacion.acceso;

import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.modelo.TokenRecuperacion;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.puerto.entrada.SolicitarRecuperacion;
import co.edu.udea.brujula.dominio.puerto.salida.CifradorDeContrasenas;
import co.edu.udea.brujula.dominio.puerto.salida.NotificadorDeCorreo;
import co.edu.udea.brujula.dominio.puerto.salida.ParametrosDelSistema;
import co.edu.udea.brujula.dominio.puerto.salida.Reloj;
import co.edu.udea.brujula.dominio.puerto.salida.TokenRecuperacionRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.UsuarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Service
public class SolicitudDeRecuperacion implements SolicitarRecuperacion {

    public static final String RESPUESTA_NEUTRA =
            "Si el correo existe, te enviamos un enlace para restablecer tu contraseña.";

    private final UsuarioRepositorio usuarios;
    private final TokenRecuperacionRepositorio tokensDeRecuperacion;
    private final CifradorDeContrasenas cifrador;
    private final NotificadorDeCorreo correo;
    private final ParametrosDelSistema parametros;
    private final Reloj reloj;
    private final SecureRandom aleatorio = new SecureRandom();

    public SolicitudDeRecuperacion(UsuarioRepositorio usuarios, TokenRecuperacionRepositorio tokensDeRecuperacion,
                                   CifradorDeContrasenas cifrador, NotificadorDeCorreo correo,
                                   ParametrosDelSistema parametros, Reloj reloj) {
        this.usuarios = usuarios;
        this.tokensDeRecuperacion = tokensDeRecuperacion;
        this.cifrador = cifrador;
        this.correo = correo;
        this.parametros = parametros;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public String solicitar(String email) {
        if (email == null || email.isBlank()) {
            throw new DatosInvalidos(List.of("El correo electrónico es obligatorio."));
        }
        usuarios.porEmail(email.trim()).filter(Usuario::estaActivo).ifPresent(this::enviarEnlace);
        return RESPUESTA_NEUTRA;
    }

    private void enviarEnlace(Usuario usuario) {
        Instant ahora = reloj.ahora();
        if (alcanzoElLimite(usuario, ahora)) return;

        int minutos = parametros.entero(ParametrosDelSistema.MINUTOS_VIGENCIA_RECUPERACION, 30);
        String tokenPlano = generarToken();
        tokensDeRecuperacion.guardar(
                TokenRecuperacion.nuevo(usuario.id(), cifrador.resumen(tokenPlano), ahora, minutos));
        correo.enviarEnlaceDeRecuperacion(usuario.email(), tokenPlano, minutos);
    }

    private boolean alcanzoElLimite(Usuario usuario, Instant ahora) {
        int maximoPorHora = parametros.entero(ParametrosDelSistema.MAX_SOLICITUDES_RECUPERACION_HORA, 3);
        return tokensDeRecuperacion.solicitudesDesde(usuario.id(), ahora.minus(Duration.ofHours(1))) >= maximoPorHora;
    }

    private String generarToken() {
        byte[] bytes = new byte[32];
        aleatorio.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
