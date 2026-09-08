package co.edu.udea.brujula.infraestructura.salida.google;

import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.excepcion.ServicioNoDisponible;
import co.edu.udea.brujula.dominio.puerto.salida.VerificadorDeGoogle;
import co.edu.udea.brujula.infraestructura.configuracion.BrujulaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Verifica el ID token que entrega el botón "Continuar con Google" del front. Solo se lee el token:
 * la contraseña de Google nunca pasa por aquí (HU-001 CA-12).
 *
 * Sin GOOGLE_CLIENT_ID y en modo desarrollo acepta credenciales de la forma
 * "dev:correo@dominio:Nombre:Apellido", para poder probar el registro sin crear credenciales reales.
 */
@Component
public class GoogleIdentityServices implements VerificadorDeGoogle {

    private static final Logger log = LoggerFactory.getLogger(GoogleIdentityServices.class);
    private static final Set<String> EMISORES = Set.of("https://accounts.google.com", "accounts.google.com");
    private static final String JWKS = "https://www.googleapis.com/oauth2/v3/certs";

    private final BrujulaProperties propiedades;
    private volatile NimbusJwtDecoder decodificador;

    public GoogleIdentityServices(BrujulaProperties propiedades) {
        this.propiedades = propiedades;
    }

    @Override
    public boolean estaSimulado() {
        return !propiedades.googleConfigurado() && propiedades.modoDesarrollo();
    }

    @Override
    public CuentaDeGoogle verificar(String credencial) {
        if (credencial == null || credencial.isBlank()) {
            throw new DatosInvalidos("GOOGLE_CANCELADO",
                    "No se recibió la autenticación de Google. Puedes intentarlo de nuevo.");
        }
        if (propiedades.googleConfigurado()) {
            return verificarConGoogle(credencial);
        }
        if (estaSimulado() && credencial.startsWith("dev:")) {
            return cuentaSimulada(credencial);
        }
        throw new ServicioNoDisponible("GOOGLE_NO_DISPONIBLE",
                "El inicio con Google no está disponible en este momento. Intenta más tarde.");
    }

    private CuentaDeGoogle cuentaSimulada(String credencial) {
        String[] partes = credencial.split(":", -1);
        String email = partes.length > 1 ? partes[1].trim().toLowerCase() : "";
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new DatosInvalidos("GOOGLE_INVALIDO", "El correo simulado no es válido.");
        }
        log.warn("Autenticación de Google SIMULADA para {} (modo desarrollo, sin GOOGLE_CLIENT_ID)", email);
        return new CuentaDeGoogle("dev-" + email, email,
                partes.length > 2 ? partes[2] : "", partes.length > 3 ? partes[3] : "");
    }

    private CuentaDeGoogle verificarConGoogle(String credencial) {
        try {
            Jwt token = decodificador().decode(credencial);
            Boolean correoVerificado = token.getClaimAsBoolean("email_verified");
            String email = token.getClaimAsString("email");
            if (email == null || Boolean.FALSE.equals(correoVerificado)) {
                throw new DatosInvalidos("GOOGLE_SIN_CORREO",
                        "Google no entregó un correo verificado. Concede el permiso de correo e intenta de nuevo.");
            }
            return new CuentaDeGoogle(token.getSubject(), email.toLowerCase(),
                    token.getClaimAsString("given_name"), token.getClaimAsString("family_name"));
        } catch (JwtException e) {
            log.warn("ID token de Google rechazado: {}", e.getMessage());
            throw new DatosInvalidos("GOOGLE_INVALIDO",
                    "No fue posible validar la autenticación con Google. Intenta de nuevo.");
        } catch (DatosInvalidos e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("Error consultando las llaves públicas de Google", e);
            throw new ServicioNoDisponible("GOOGLE_NO_DISPONIBLE",
                    "El servicio de Google no respondió. Intenta de nuevo en unos minutos.");
        }
    }

    /** Se arma la primera vez que se necesita, para no golpear a Google al arrancar. */
    private NimbusJwtDecoder decodificador() {
        if (decodificador == null) {
            synchronized (this) {
                if (decodificador == null) {
                    NimbusJwtDecoder nuevo = NimbusJwtDecoder.withJwkSetUri(JWKS).build();
                    OAuth2TokenValidator<Jwt> emisorEsGoogle = jwt -> EMISORES.contains(String.valueOf(jwt.getIssuer()))
                            ? OAuth2TokenValidatorResult.success()
                            : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "El emisor no es Google", null));
                    OAuth2TokenValidator<Jwt> audienciaEsNuestra = jwt -> {
                        List<String> audiencia = jwt.getAudience();
                        return audiencia != null && audiencia.contains(propiedades.googleClientId())
                                ? OAuth2TokenValidatorResult.success()
                                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Audiencia incorrecta", null));
                    };
                    nuevo.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                            new JwtTimestampValidator(), emisorEsGoogle, audienciaEsNuestra));
                    decodificador = nuevo;
                }
            }
        }
        return decodificador;
    }
}
