package co.edu.udea.brujula.usuario;

import co.edu.udea.brujula.catalogo.ParametrosService;
import co.edu.udea.brujula.catalogo.Rol;
import co.edu.udea.brujula.catalogo.RolRepository;
import co.edu.udea.brujula.common.ApiException;
import co.edu.udea.brujula.config.BrujulaProperties;
import co.edu.udea.brujula.config.UsuarioPrincipal;
import co.edu.udea.brujula.usuario.AuthDtos.*;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final String MSG_CREDENCIALES = "Las credenciales ingresadas no son válidas.";
    private static final String MSG_RECUPERACION = "Si el correo existe, te enviamos un enlace para restablecer tu contraseña.";

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final TokenRecuperacionRepository tokensRecuperacion;
    private final TokenSesionRevocadoRepository revocados;
    private final JwtService jwt;
    private final GoogleTokenVerifier google;
    private final PasswordEncoder encoder;
    private final ParametrosService parametros;
    private final MailService mail;
    private final BrujulaProperties props;
    private final SecureRandom random = new SecureRandom();

    public AuthService(UsuarioRepository usuarios, RolRepository roles, TokenRecuperacionRepository tokensRecuperacion,
                       TokenSesionRevocadoRepository revocados, JwtService jwt, GoogleTokenVerifier google,
                       PasswordEncoder encoder, ParametrosService parametros, MailService mail, BrujulaProperties props) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.tokensRecuperacion = tokensRecuperacion;
        this.revocados = revocados;
        this.jwt = jwt;
        this.google = google;
        this.encoder = encoder;
        this.parametros = parametros;
        this.mail = mail;
        this.props = props;
    }

    // ---------- HU-001 Registro mediante Google ----------

    @Transactional(readOnly = true)
    public GoogleRegistroPendiente iniciarConGoogle(String credential) {
        GoogleTokenVerifier.IdentidadGoogle id = google.verificar(credential);
        if (usuarios.existsByEmailIgnoreCase(id.email())) {
            // CA-03: no se crea otra cuenta; el front redirige a iniciar sesión.
            throw new ApiException(HttpStatus.CONFLICT, "CUENTA_EXISTENTE",
                    "Ya existe una cuenta asociada al correo " + id.email() + ". Inicia sesión con tu contraseña.",
                    List.of(), Map.of("email", id.email()));
        }
        String token = jwt.emitirRegistro(id.sub(), id.email(), id.nombre(), id.apellido());
        return new GoogleRegistroPendiente(token, id.email(), id.nombre(), id.apellido(), google.simulado());
    }

    @Transactional
    public UsuarioDto registrar(RegistroRequest req) {
        Claims claims = jwt.validarRegistro(req.registroToken())
                .orElseThrow(() -> ApiException.badRequest("REGISTRO_EXPIRADO",
                        "La verificación con Google expiró. Vuelve a seleccionar \"Continuar con Google\"."));
        String email = claims.get("email", String.class);
        String googleSub = claims.getSubject();

        List<String> errores = new ArrayList<>(PasswordPolicy.validarNombres(req.nombre(), req.apellido()));
        errores.addAll(PasswordPolicy.validar(req.password(), req.confirmacionPassword()));
        if (req.aceptoTerminos() == null || !req.aceptoTerminos()) {
            errores.add("Debes aceptar los Términos y Condiciones y la Política de Tratamiento de Datos Personales.");
        }
        if (!errores.isEmpty()) throw ApiException.validacion(errores);

        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflicto("CUENTA_EXISTENTE", "Ya existe una cuenta asociada al correo " + email + ".");
        }
        Rol estudiante = roles.findByNombreRol("Estudiante").orElseThrow();
        Instant ahora = Instant.now();
        Usuario u = new Usuario();
        u.setNombre(req.nombre().trim());
        u.setApellido(req.apellido().trim());
        u.setEmail(email);
        u.setGoogleSub(googleSub);
        u.setPasswordHash(encoder.encode(req.password()));
        u.setAceptoTerminos(true);
        u.setFechaAceptacionTerminos(ahora);
        u.setCreadoEn(ahora);
        u.setRol(estudiante);
        u.setEstado("Activo");
        usuarios.save(u);
        return UsuarioDto.de(u);
    }

    // ---------- HU-002 Autenticación ----------

    @Transactional(noRollbackFor = ApiException.class)
    public SesionResponse login(LoginRequest req) {
        List<String> faltantes = new ArrayList<>();
        if (req.email() == null || req.email().isBlank()) faltantes.add("El correo electrónico es obligatorio.");
        if (req.password() == null || req.password().isEmpty()) faltantes.add("La contraseña es obligatoria.");
        if (!faltantes.isEmpty()) throw ApiException.validacion(faltantes);

        Usuario u = usuarios.findByEmailIgnoreCase(req.email().trim()).orElse(null);
        if (u == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS", MSG_CREDENCIALES);   // CA-04: mensaje genérico
        }
        int maxIntentos = parametros.entero(ParametrosService.MAX_INTENTOS_LOGIN, 5);
        int minutosBloqueo = parametros.entero(ParametrosService.MINUTOS_BLOQUEO_LOGIN, 10);
        Instant ahora = Instant.now();

        if (u.getFechaBloqueo() != null) {
            Instant fin = u.getFechaBloqueo().plus(Duration.ofMinutes(minutosBloqueo));
            if (ahora.isBefore(fin)) {
                long restantes = Math.max(1, Duration.between(ahora, fin).toMinutes() + 1);
                throw new ApiException(HttpStatus.LOCKED, "CUENTA_BLOQUEADA",
                        "Demasiados intentos fallidos. Intenta de nuevo en " + restantes + " minuto" + (restantes == 1 ? "" : "s") + ".");
            }
            // CA-08: transcurrido el bloqueo se restablece el contador.
            u.setFechaBloqueo(null);
            u.setIntentosFallidosLogin(0);
        }
        if (!"Activo".equals(u.getEstado())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS", MSG_CREDENCIALES);
        }
        if (!encoder.matches(req.password(), u.getPasswordHash())) {
            int fallidos = u.getIntentosFallidosLogin() + 1;
            u.setIntentosFallidosLogin(fallidos);
            if (fallidos >= maxIntentos) {
                u.setFechaBloqueo(ahora);
                usuarios.save(u);
                throw new ApiException(HttpStatus.LOCKED, "CUENTA_BLOQUEADA",
                        "Alcanzaste el máximo de " + maxIntentos + " intentos fallidos. La autenticación queda bloqueada durante " + minutosBloqueo + " minutos.");
            }
            usuarios.save(u);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS", MSG_CREDENCIALES);
        }
        u.setIntentosFallidosLogin(0);
        u.setFechaBloqueo(null);
        u.setUltimoLogin(ahora);                                                  // CA-06
        usuarios.save(u);
        JwtService.TokenEmitido t = jwt.emitirSesion(u);
        return new SesionResponse(t.token(), t.expiraEn(), UsuarioDto.de(u));
    }

    // ---------- HU-004 Cierre y expiración de sesión ----------

    @Transactional
    public void cerrarSesion(UsuarioPrincipal principal, Instant expiraEn) {
        revocar(principal.jti(), principal.id(), expiraEn);
    }

    @Transactional
    public SesionResponse refrescar(UsuarioPrincipal principal, Instant expiraAnterior) {
        Usuario u = usuarios.findById(principal.id()).orElseThrow();
        revocar(principal.jti(), principal.id(), expiraAnterior);
        JwtService.TokenEmitido t = jwt.emitirSesion(u);
        return new SesionResponse(t.token(), t.expiraEn(), UsuarioDto.de(u));
    }

    private void revocar(String jti, Long idUsuario, Instant expiraEn) {
        if (revocados.existsById(jti)) return;
        TokenSesionRevocado r = new TokenSesionRevocado();
        r.setJti(jti);
        r.setIdUsuario(idUsuario);
        r.setRevocadoEn(Instant.now());
        r.setExpiraEn(expiraEn != null ? expiraEn : Instant.now().plus(Duration.ofHours(3)));
        revocados.save(r);
    }

    @Scheduled(fixedDelayString = "PT1H")
    @Transactional
    public void limpiarRevocadosExpirados() {
        int n = revocados.eliminarExpirados(Instant.now());
        if (n > 0) log.info("Se eliminaron {} tokens revocados ya expirados", n);
    }

    // ---------- HU-003 Recuperar contraseña ----------

    @Transactional
    public Mensaje solicitarRecuperacion(String email) {
        if (email == null || email.isBlank()) throw ApiException.validacion(List.of("El correo electrónico es obligatorio."));
        Usuario u = usuarios.findByEmailIgnoreCase(email.trim()).orElse(null);
        if (u != null && "Activo".equals(u.getEstado())) {
            int maxHora = parametros.entero(ParametrosService.MAX_SOLICITUDES_RECUPERACION_HORA, 3);
            long recientes = tokensRecuperacion.countByUsuario_IdAndCreadoEnAfter(u.getId(), Instant.now().minus(Duration.ofHours(1)));
            if (recientes >= maxHora) {
                log.warn("Límite de solicitudes de restablecimiento alcanzado para {}", u.getEmail());   // CA-09, respuesta sigue siendo neutra
            } else {
                int minutos = parametros.entero(ParametrosService.MINUTOS_VIGENCIA_RECUPERACION, 30);
                byte[] bytes = new byte[32];
                random.nextBytes(bytes);
                String tokenPlano = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                TokenRecuperacion t = new TokenRecuperacion();
                t.setUsuario(u);
                t.setTokenHash(sha256(tokenPlano));
                t.setCreadoEn(Instant.now());
                t.setExpiraEn(Instant.now().plus(Duration.ofMinutes(minutos)));
                tokensRecuperacion.save(t);
                String enlace = props.frontendUrl().replaceAll("/+$", "") + "/restablecer?token=" + tokenPlano;
                mail.enviarEnlaceRecuperacion(u.getEmail(), enlace, minutos);
            }
        }
        return new Mensaje(MSG_RECUPERACION);                                        // CA-03: siempre el mismo mensaje
    }

    @Transactional(readOnly = true)
    public void validarTokenRecuperacion(String token) {
        buscarTokenVigente(token);
    }

    @Transactional
    public Mensaje restablecer(RestablecerRequest req) {
        TokenRecuperacion t = buscarTokenVigente(req.token());
        List<String> errores = PasswordPolicy.validar(req.password(), req.confirmacionPassword());
        if (!errores.isEmpty()) throw ApiException.validacion(errores);
        Usuario u = t.getUsuario();
        Instant ahora = Instant.now();
        u.setPasswordHash(encoder.encode(req.password()));                       // CA-06: solo cambia la contraseña
        u.setPasswordActualizadoEn(ahora);                                       // CA-07: invalida sesiones anteriores
        u.setIntentosFallidosLogin(0);
        u.setFechaBloqueo(null);
        t.setUsadoEn(ahora);                                                     // CA-06: el enlace queda invalidado
        usuarios.save(u);
        tokensRecuperacion.save(t);
        return new Mensaje("Tu contraseña fue actualizada. Ya puedes iniciar sesión.");
    }

    private TokenRecuperacion buscarTokenVigente(String token) {
        if (token == null || token.isBlank()) throw enlaceInvalido();
        TokenRecuperacion t = tokensRecuperacion.findByTokenHash(sha256(token)).orElseThrow(this::enlaceInvalido);
        if (t.getUsadoEn() != null || t.getExpiraEn().isBefore(Instant.now())) throw enlaceInvalido();
        return t;
    }

    private ApiException enlaceInvalido() {
        return ApiException.badRequest("ENLACE_INVALIDO", "Este enlace expiró o ya fue utilizado. Solicita uno nuevo.");
    }

    static String sha256(String valor) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // ---------- HU-005 Perfil ----------

    @Transactional(readOnly = true)
    public UsuarioDto perfil(Long idUsuario) {
        return UsuarioDto.de(usuarios.findById(idUsuario).orElseThrow());
    }

    @Transactional
    public UsuarioDto actualizarPerfil(Long idUsuario, PerfilRequest req) {
        List<String> errores = PasswordPolicy.validarNombres(req.nombre(), req.apellido());
        if (!errores.isEmpty()) throw ApiException.validacion(errores);
        Usuario u = usuarios.findById(idUsuario).orElseThrow();
        u.setNombre(req.nombre().trim());
        u.setApellido(req.apellido().trim());                                    // CA-03: el correo no se toca
        return UsuarioDto.de(usuarios.save(u));
    }

    @Transactional
    public Mensaje cambiarPassword(Long idUsuario, CambioPasswordRequest req) {
        Usuario u = usuarios.findById(idUsuario).orElseThrow();
        if (req.passwordActual() == null || !encoder.matches(req.passwordActual(), u.getPasswordHash())) {
            throw ApiException.badRequest("PASSWORD_ACTUAL_INCORRECTA", "La contraseña actual no coincide con la registrada.");
        }
        List<String> errores = PasswordPolicy.validar(req.passwordNueva(), req.confirmacionPassword());
        if (!errores.isEmpty()) throw ApiException.validacion(errores);
        u.setPasswordHash(encoder.encode(req.passwordNueva()));
        usuarios.save(u);
        return new Mensaje("Tu contraseña fue actualizada correctamente.");
    }
}
