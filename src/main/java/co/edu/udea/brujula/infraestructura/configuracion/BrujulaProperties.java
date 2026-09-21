package co.edu.udea.brujula.infraestructura.configuracion;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "brujula")
public record BrujulaProperties(String jwtSecret, String frontendUrl, String googleClientId, boolean modoDesarrollo,
                                String uploadsDir, String mailFrom, Admin admin, boolean datosEjemplo,
                                String brevoApiKey) {

    public record Admin(String email, String password) {
    }

    public boolean googleConfigurado() {
        return googleClientId != null && !googleClientId.isBlank();
    }

    public boolean brevoConfigurado() {
        return brevoApiKey != null && !brevoApiKey.isBlank();
    }
}
