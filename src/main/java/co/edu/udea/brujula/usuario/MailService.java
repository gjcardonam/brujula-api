package co.edu.udea.brujula.usuario;

import co.edu.udea.brujula.config.BrujulaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Envío del enlace de restablecimiento (HU-003 CA-02). Sin SMTP configurado, el enlace se escribe en el log. */
@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final ObjectProvider<JavaMailSender> sender;
    private final BrujulaProperties props;
    private final boolean smtpConfigurado;

    public MailService(ObjectProvider<JavaMailSender> sender, BrujulaProperties props,
                       org.springframework.core.env.Environment env) {
        this.sender = sender;
        this.props = props;
        String host = env.getProperty("spring.mail.host", "");
        this.smtpConfigurado = host != null && !host.isBlank();
    }

    public void enviarEnlaceRecuperacion(String destinatario, String enlace, int minutosVigencia) {
        String asunto = "Brújula · Restablece tu contraseña";
        String cuerpo = """
                Hola,

                Recibimos una solicitud para restablecer la contraseña de tu cuenta en Brújula.
                Abre este enlace para definir una nueva contraseña (vigente por %d minutos y de un solo uso):

                %s

                Si no hiciste esta solicitud, ignora este mensaje: tu contraseña no cambiará.
                """.formatted(minutosVigencia, enlace);

        JavaMailSender s = sender.getIfAvailable();
        if (!smtpConfigurado || s == null) {
            log.warn("SMTP no configurado. Enlace de restablecimiento para {}: {}", destinatario, enlace);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(props.mailFrom());
            msg.setTo(destinatario);
            msg.setSubject(asunto);
            msg.setText(cuerpo);
            s.send(msg);
        } catch (RuntimeException e) {
            // No se propaga: el mensaje al usuario debe ser neutro (HU-003 CA-03).
            log.error("No fue posible enviar el correo de restablecimiento a {}: {}", destinatario, e.getMessage());
        }
    }
}
