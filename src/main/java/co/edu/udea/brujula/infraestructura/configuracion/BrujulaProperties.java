package co.edu.udea.brujula.infraestructura.configuracion;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuración del despliegue. Las reglas de negocio configurables viven en parametros_sistema. */
@ConfigurationProperties(prefix = "brujula")
public record BrujulaProperties(String jwtSecret, String frontendUrl, String googleClientId, boolean modoDesarrollo,
                                String uploadsDir, String mailFrom, Admin admin, boolean datosEjemplo) {

    public record Admin(String email, String password) {
    }

    public boolean googleConfigurado() {
        return googleClientId != null && !googleClientId.isBlank();
    }
}
