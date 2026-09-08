package co.edu.udea.brujula.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Parámetros de despliegue (no confundir con parametros_sistema, que son reglas de negocio en BD). */
@ConfigurationProperties(prefix = "brujula")
public record BrujulaProperties(
        String jwtSecret,
        String frontendUrl,
        String googleClientId,
        boolean modoDesarrollo,
        String uploadsDir,
        String mailFrom,
        Admin admin,
        boolean datosEjemplo
) {
    public record Admin(String email, String password) {}

    public boolean googleConfigurado() {
        return googleClientId != null && !googleClientId.isBlank();
    }
}
