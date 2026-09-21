package co.edu.udea.brujula.infraestructura.configuracion;

import co.edu.udea.brujula.dominio.puerto.salida.NotificadorDeCorreo;
import co.edu.udea.brujula.infraestructura.salida.correo.CorreoBrevo;
import co.edu.udea.brujula.infraestructura.salida.correo.CorreoSmtp;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class CorreoConfig {

    private static final String URL_BASE_BREVO = "https://api.brevo.com";

    @Bean
    public NotificadorDeCorreo notificadorDeCorreo(BrujulaProperties propiedades, Environment entorno,
            ObjectProvider<JavaMailSender> emisor, ObjectMapper json) {
        if (propiedades.brevoConfigurado()) {
            return new CorreoBrevo(propiedades, propiedades.brevoApiKey(), URL_BASE_BREVO, json);
        }
        return new CorreoSmtp(emisor, propiedades, entorno);
    }
}
