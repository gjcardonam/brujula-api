package co.edu.udea.brujula.infraestructura.salida.seguridad;

import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.puerto.salida.ParametrosDelSistema;
import co.edu.udea.brujula.dominio.puerto.salida.ProveedorDeTokens;
import co.edu.udea.brujula.infraestructura.configuracion.BrujulaProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Tokens firmados con HMAC. Hay dos tipos y el campo "proposito" impide usar uno donde va el otro:
 * el de sesión, que dura lo que diga el parámetro de horas de inactividad, y el de registro, que
 * solo acredita que Google ya verificó el correo mientras el usuario llena el formulario.
 */
@Component
public class JwtProveedorDeTokens implements ProveedorDeTokens {

    private static final String SESION = "sesion";
    private static final String REGISTRO = "registro";
    private static final Duration VIGENCIA_DEL_REGISTRO = Duration.ofMinutes(20);

    private final SecretKey clave;
    private final ParametrosDelSistema parametros;

    public JwtProveedorDeTokens(BrujulaProperties propiedades, ParametrosDelSistema parametros) {
        String secreto = propiedades.jwtSecret();
        if (secreto == null || secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("brujula.jwt-secret debe tener al menos 32 caracteres");
        }
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.parametros = parametros;
    }

    @Override
    public Sesion emitirSesion(Usuario usuario) {
        int horas = parametros.entero(ParametrosDelSistema.HORAS_EXPIRACION_SESION, 2);
        Instant ahora = Instant.now();
        Instant expira = ahora.plus(Duration.ofHours(horas));
        String jti = UUID.randomUUID().toString();
        String token = Jwts.builder()
                .id(jti)
                .subject(String.valueOf(usuario.id()))
                .claim("email", usuario.email())
                .claim("rol", usuario.rol().nombre())
                .claim("proposito", SESION)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expira))
                .signWith(clave)
                .compact();
        return new Sesion(token, jti, expira);
    }

    @Override
    public Optional<SesionLeida> leerSesion(String token) {
        return leer(token, SESION).map(c -> new SesionLeida(c.getId(), Long.valueOf(c.getSubject()),
                c.getIssuedAt().toInstant(), c.getExpiration().toInstant()));
    }

    @Override
    public String emitirRegistro(RegistroPendiente registro) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(registro.googleSub())
                .claim("email", registro.email())
                .claim("nombre", registro.nombre() == null ? "" : registro.nombre())
                .claim("apellido", registro.apellido() == null ? "" : registro.apellido())
                .claim("proposito", REGISTRO)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(VIGENCIA_DEL_REGISTRO)))
                .signWith(clave)
                .compact();
    }

    @Override
    public Optional<RegistroPendiente> leerRegistro(String token) {
        return leer(token, REGISTRO).map(c -> new RegistroPendiente(c.getSubject(),
                c.get("email", String.class), c.get("nombre", String.class), c.get("apellido", String.class)));
    }

    private Optional<Claims> leer(String token, String proposito) {
        if (token == null || token.isBlank()) return Optional.empty();
        try {
            Claims claims = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
            return proposito.equals(claims.get("proposito", String.class)) ? Optional.of(claims) : Optional.empty();
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
