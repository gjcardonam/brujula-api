package co.edu.udea.brujula.usuario;

import co.edu.udea.brujula.catalogo.ParametrosService;
import co.edu.udea.brujula.config.BrujulaProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Tokens firmados (HS256) para dos propósitos:
 *  - "sesion": token de acceso con jti, que expira a las N horas de inactividad (HU-004 CA-05).
 *    La inactividad se materializa así: el front refresca el token mientras el usuario usa la
 *    plataforma; si deja de usarla, el token vence y hay que volver a autenticarse.
 *  - "registro": token corto que acredita que Google ya verificó el correo (HU-001 CA-02/CA-04).
 */
@Service
public class JwtService {
    public static final String PROPOSITO_SESION = "sesion";
    public static final String PROPOSITO_REGISTRO = "registro";

    private final SecretKey clave;
    private final ParametrosService parametros;

    public JwtService(BrujulaProperties props, ParametrosService parametros) {
        String secreto = props.jwtSecret();
        if (secreto == null || secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("brujula.jwt-secret debe tener al menos 32 caracteres");
        }
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.parametros = parametros;
    }

    public record TokenEmitido(String token, String jti, Instant expiraEn) {}

    public TokenEmitido emitirSesion(Usuario u) {
        int horas = parametros.entero(ParametrosService.HORAS_EXPIRACION_SESION, 2);
        Instant ahora = Instant.now();
        Instant exp = ahora.plus(Duration.ofHours(horas));
        String jti = UUID.randomUUID().toString();
        String token = Jwts.builder()
                .id(jti)
                .subject(String.valueOf(u.getId()))
                .claim("email", u.getEmail())
                .claim("rol", u.getRol().getNombreRol())
                .claim("proposito", PROPOSITO_SESION)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(exp))
                .signWith(clave)
                .compact();
        return new TokenEmitido(token, jti, exp);
    }

    public String emitirRegistro(String googleSub, String email, String nombre, String apellido) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(googleSub)
                .claim("email", email)
                .claim("nombre", nombre == null ? "" : nombre)
                .claim("apellido", apellido == null ? "" : apellido)
                .claim("proposito", PROPOSITO_REGISTRO)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(Duration.ofMinutes(20))))
                .signWith(clave)
                .compact();
    }

    public Optional<Claims> validarSesion(String token) {
        return validar(token, PROPOSITO_SESION);
    }

    public Optional<Claims> validarRegistro(String token) {
        return validar(token, PROPOSITO_REGISTRO);
    }

    private Optional<Claims> validar(String token, String proposito) {
        try {
            Claims c = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
            if (!proposito.equals(c.get("proposito", String.class))) return Optional.empty();
            return Optional.of(c);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public Map<String, Object> claimsComoMapa(Claims c) {
        return Map.copyOf(c);
    }
}
