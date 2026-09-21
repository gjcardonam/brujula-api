package co.edu.udea.brujula.infraestructura.configuracion;

import co.edu.udea.brujula.dominio.puerto.salida.NotificadorDeCorreo;
import co.edu.udea.brujula.infraestructura.salida.correo.CorreoBrevo;
import co.edu.udea.brujula.infraestructura.salida.correo.CorreoSmtp;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.env.MockEnvironment;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@DisplayName("Selección del adaptador de correo activo")
class CorreoConfigTest {

    private static final class SinJavaMailSender implements ObjectProvider<JavaMailSender> {

        @Override
        public JavaMailSender getObject(Object... args) {
            throw new UnsupportedOperationException();
        }

        @Override
        public JavaMailSender getObject() {
            throw new UnsupportedOperationException();
        }

        @Override
        public JavaMailSender getIfAvailable() {
            return null;
        }

        @Override
        public JavaMailSender getIfUnique() {
            return null;
        }

        @Override
        public Stream<JavaMailSender> orderedStream() {
            return Stream.empty();
        }
    }

    private BrujulaProperties propiedadesCon(String brevoApiKey) {
        return new BrujulaProperties(null, "https://brujula.udea.edu.co", null, false, null,
                "no-reply@brujula.local", null, true, brevoApiKey);
    }

    @Test
    void usa_brevo_cuando_hay_clave_de_api_configurada() {
        CorreoConfig config = new CorreoConfig();
        MockEnvironment entorno = new MockEnvironment();

        NotificadorDeCorreo notificador = config.notificadorDeCorreo(propiedadesCon("una-clave-de-brevo"), entorno,
                new SinJavaMailSender(), new ObjectMapper());

        assertInstanceOf(CorreoBrevo.class, notificador);
    }

    @Test
    void usa_smtp_cuando_no_hay_clave_de_brevo() {
        CorreoConfig config = new CorreoConfig();
        MockEnvironment entorno = new MockEnvironment();
        entorno.setProperty("spring.mail.host", "smtp.gmail.com");

        NotificadorDeCorreo notificador = config.notificadorDeCorreo(propiedadesCon(null), entorno,
                new SinJavaMailSender(), new ObjectMapper());

        assertInstanceOf(CorreoSmtp.class, notificador);
    }

    @Test
    void usa_smtp_como_conservador_del_comportamiento_actual_cuando_no_hay_clave_de_brevo_ni_smtp() {
        CorreoConfig config = new CorreoConfig();
        MockEnvironment entorno = new MockEnvironment();

        NotificadorDeCorreo notificador = config.notificadorDeCorreo(propiedadesCon(""), entorno,
                new SinJavaMailSender(), new ObjectMapper());

        assertInstanceOf(CorreoSmtp.class, notificador);
    }
}
