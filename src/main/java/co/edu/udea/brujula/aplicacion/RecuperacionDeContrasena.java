package co.edu.udea.brujula.aplicacion;

import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.modelo.TokenRecuperacion;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.puerto.entrada.RecuperarContrasena;
import co.edu.udea.brujula.dominio.puerto.salida.*;
import co.edu.udea.brujula.dominio.servicio.PoliticaDeContrasena;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Service
public class RecuperacionDeContrasena implements RecuperarContrasena {

    private static final Logger log = LoggerFactory.getLogger(RecuperacionDeContrasena.class);

    /** El mismo texto exista o no la cuenta, para no revelar qué correos están registrados (CA-03). */
    private static final String RESPUESTA_NEUTRA =
            "Si el correo existe, te enviamos un enlace para restablecer tu contraseña.";

    private final UsuarioRepositorio usuarios;
    private final TokenRecuperacionRepositorio tokensDeRecuperacion;
    private final CifradorDeContrasenas cifrador;
    private final NotificadorDeCorreo correo;
    private final ParametrosDelSistema parametros;
    private final Reloj reloj;
    private final SecureRandom aleatorio = new SecureRandom();

    public RecuperacionDeContrasena(UsuarioRepositorio usuarios, TokenRecuperacionRepositorio tokensDeRecuperacion,
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
    public String solicitarEnlace(String email) {
        if (email == null || email.isBlank()) {
            throw new DatosInvalidos(List.of("El correo electrónico es obligatorio."));
        }
        usuarios.porEmail(email.trim())
                .filter(Usuario::estaActivo)
                .ifPresent(this::enviarEnlaceSiNoAbusa);
        return RESPUESTA_NEUTRA;
    }

    private void enviarEnlaceSiNoAbusa(Usuario usuario) {
        int maximoPorHora = parametros.entero(ParametrosDelSistema.MAX_SOLICITUDES_RECUPERACION_HORA, 3);
        Instant ahora = reloj.ahora();
        if (tokensDeRecuperacion.solicitudesDesde(usuario.id(), ahora.minus(Duration.ofHours(1))) >= maximoPorHora) {
            log.warn("Se alcanzó el límite de solicitudes de restablecimiento para {}", usuario.email());
            return;   // la respuesta al usuario sigue siendo la misma (CA-09)
        }
        int minutos = parametros.entero(ParametrosDelSistema.MINUTOS_VIGENCIA_RECUPERACION, 30);
        String tokenPlano = generarToken();
        tokensDeRecuperacion.guardar(TokenRecuperacion.nuevo(usuario.id(), cifrador.resumen(tokenPlano), ahora, minutos));
        correo.enviarEnlaceDeRecuperacion(usuario.email(), tokenPlano, minutos);
    }

    private String generarToken() {
        byte[] bytes = new byte[32];
        aleatorio.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Override
    @Transactional(readOnly = true)
    public void verificarEnlace(String token) {
        buscarVigente(token);
    }

    @Override
    @Transactional
    public String restablecer(String token, String password, String confirmacion) {
        TokenRecuperacion vigente = buscarVigente(token);
        List<String> errores = PoliticaDeContrasena.revisar(password, confirmacion);
        if (!errores.isEmpty()) throw new DatosInvalidos(errores);

        Usuario usuario = usuarios.porId(vigente.idUsuario()).orElseThrow();
        Instant ahora = reloj.ahora();
        usuario.restablecerPassword(cifrador.cifrar(password), ahora);
        usuarios.guardar(usuario);
        tokensDeRecuperacion.guardar(vigente.marcarUsado(ahora));   // el enlace no sirve dos veces (CA-06)
        return "Tu contraseña fue actualizada. Ya puedes iniciar sesión.";
    }

    private TokenRecuperacion buscarVigente(String token) {
        if (token == null || token.isBlank()) throw enlaceInvalido();
        return tokensDeRecuperacion.porHash(cifrador.resumen(token))
                .filter(t -> t.vigente(reloj.ahora()))
                .orElseThrow(this::enlaceInvalido);
    }

    private DatosInvalidos enlaceInvalido() {
        return new DatosInvalidos("ENLACE_INVALIDO", "Este enlace expiró o ya fue utilizado. Solicita uno nuevo.");
    }
}
