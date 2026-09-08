package co.edu.udea.brujula.usuario;

import co.edu.udea.brujula.common.ApiException;
import co.edu.udea.brujula.config.BrujulaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Verifica el ID token que entrega "Continuar con Google" (Google Identity Services) en el front.
 * Solo se lee el token: nunca se pide ni se guarda la contraseña de Google (HU-001 CA-12).
 *
 * Si no hay GOOGLE_CLIENT_ID configurado y la API corre en modo desarrollo, acepta credenciales
 * simuladas con la forma "dev:correo@dominio:Nombre:Apellido" para poder probar el flujo completo.
 */
@Service
public class GoogleTokenVerifier {

    private static final Logger log = LoggerFactory.getLogger(GoogleTokenVerifier.class);
    private static final Set<String> EMISORES = Set.of("https://accounts.google.com", "accounts.google.com");

    public record IdentidadGoogle(String sub, String email, String nombre, String apellido) {}

    private final BrujulaProperties props;
    private volatile NimbusJwtDecoder decoder;

    public GoogleTokenVerifier(BrujulaProperties props) {
        this.props = props;
    }

    public boolean simulado() {
        return !props.googleConfigurado() && props.modoDesarrollo();
    }

    public IdentidadGoogle verificar(String credential) {
        if (credential == null || credential.isBlank()) {
            throw ApiException.badRequest("GOOGLE_CANCELADO", "No se recibió la autenticación de Google. Puedes intentarlo de nuevo.");
        }
        if (props.googleConfigurado()) {
            return verificarReal(credential);
        }
        if (props.modoDesarrollo() && credential.startsWith("dev:")) {
            String[] partes = credential.split(":", -1);
            String email = partes.length > 1 ? partes[1].trim().toLowerCase() : "";
            if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                throw ApiException.badRequest("GOOGLE_INVALIDO", "El correo simulado no es válido.");
            }
            String nombre = partes.length > 2 ? partes[2] : "";
            String apellido = partes.length > 3 ? partes[3] : "";
            log.warn("Autenticación de Google SIMULADA para {} (modo desarrollo, sin GOOGLE_CLIENT_ID)", email);
            return new IdentidadGoogle("dev-" + email, email, nombre, apellido);
        }
        throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "GOOGLE_NO_DISPONIBLE",
                "El inicio con Google no está disponible en este momento. Intenta más tarde.");
    }

    private IdentidadGoogle verificarReal(String credential) {
        try {
            Jwt jwt = decoder().decode(credential);
            Boolean verificado = jwt.getClaimAsBoolean("email_verified");
            String email = jwt.getClaimAsString("email");
            if (email == null || (verificado != null && !verificado)) {
                throw ApiException.badRequest("GOOGLE_SIN_CORREO", "Google no entregó un correo verificado. Concede el permiso de correo e intenta de nuevo.");
            }
            return new IdentidadGoogle(jwt.getSubject(), email.toLowerCase(),
                    jwt.getClaimAsString("given_name"), jwt.getClaimAsString("family_name"));
        } catch (JwtException e) {
            log.warn("ID token de Google rechazado: {}", e.getMessage());
            throw ApiException.badRequest("GOOGLE_INVALIDO", "No fue posible validar la autenticación con Google. Intenta de nuevo.");
        } catch (ApiException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("Error consultando las llaves de Google", e);
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "GOOGLE_NO_DISPONIBLE",
                    "El servicio de Google no respondió. Intenta de nuevo en unos minutos.");
        }
    }

    private NimbusJwtDecoder decoder() {
        if (decoder == null) {
            synchronized (this) {
                if (decoder == null) {
                    NimbusJwtDecoder d = NimbusJwtDecoder.withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs").build();
                    OAuth2TokenValidator<Jwt> emisor = jwt -> EMISORES.contains(String.valueOf(jwt.getIssuer()))
                            ? OAuth2TokenValidatorResult.success()
                            : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Emisor no es Google", null));
                    OAuth2TokenValidator<Jwt> audiencia = jwt -> {
                        List<String> aud = jwt.getAudience();
                        return aud != null && aud.contains(props.googleClientId())
                                ? OAuth2TokenValidatorResult.success()
                                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Audiencia incorrecta", null));
                    };
                    d.setJwtValidator(new DelegatingOAuth2TokenValidator<>(new JwtTimestampValidator(), emisor, audiencia));
                    decoder = d;
                }
            }
        }
        return decoder;
    }
}
