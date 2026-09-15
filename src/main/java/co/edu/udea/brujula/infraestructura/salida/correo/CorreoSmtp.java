package co.edu.udea.brujula.infraestructura.salida.correo;

import co.edu.udea.brujula.dominio.puerto.salida.NotificadorDeCorreo;
import co.edu.udea.brujula.infraestructura.configuracion.BrujulaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class CorreoSmtp implements NotificadorDeCorreo {

    private static final Logger log = LoggerFactory.getLogger(CorreoSmtp.class);

    private final ObjectProvider<JavaMailSender> emisor;
    private final BrujulaProperties propiedades;
    private final boolean smtpConfigurado;

    public CorreoSmtp(ObjectProvider<JavaMailSender> emisor, BrujulaProperties propiedades, Environment entorno) {
        this.emisor = emisor;
        this.propiedades = propiedades;
        String host = entorno.getProperty("spring.mail.host", "");
        this.smtpConfigurado = host != null && !host.isBlank();
    }

    @Override
    public void enviarEnlaceDeRecuperacion(String correo, String token, int minutosDeVigencia) {
        String enlace = propiedades.frontendUrl().replaceAll("/+$", "") + "/restablecer?token=" + token;
        String cuerpo = """
                Hola,

                Recibimos una solicitud para restablecer la contraseña de tu cuenta en Brújula.
                Abre este enlace para definir una nueva contraseña (vigente por %d minutos y de un solo uso):

                %s

                Si no hiciste esta solicitud, ignora este mensaje: tu contraseña no cambiará.
                """.formatted(minutosDeVigencia, enlace);

        JavaMailSender envio = emisor.getIfAvailable();
        if (!smtpConfigurado || envio == null) {
            log.warn("SMTP no configurado. Enlace de restablecimiento para {}: {}", correo, enlace);
            return;
        }
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(propiedades.mailFrom());
            mensaje.setTo(correo);
            mensaje.setSubject("Brújula · Restablece tu contraseña");
            mensaje.setText(cuerpo);
            envio.send(mensaje);
        } catch (RuntimeException e) {

            log.error("No fue posible enviar el correo de restablecimiento a {}: {}", correo, e.getMessage());
        }
    }
}
