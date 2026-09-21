package co.edu.udea.brujula.infraestructura.salida.correo;

import co.edu.udea.brujula.dominio.puerto.salida.NotificadorDeCorreo;
import co.edu.udea.brujula.infraestructura.configuracion.BrujulaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

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
        MensajeDeRecuperacion.Mensaje mensaje =
                MensajeDeRecuperacion.paraEnlace(propiedades.frontendUrl(), token, minutosDeVigencia);
        JavaMailSender envio = emisor.getIfAvailable();
        if (!smtpConfigurado || envio == null) {
            log.warn("SMTP no configurado. Enlace de restablecimiento para {}: {}", correo, mensaje.enlace());
            return;
        }
        try {
            SimpleMailMessage mensajeSmtp = new SimpleMailMessage();
            mensajeSmtp.setFrom(propiedades.mailFrom());
            mensajeSmtp.setTo(correo);
            mensajeSmtp.setSubject(mensaje.asunto());
            mensajeSmtp.setText(mensaje.texto());
            envio.send(mensajeSmtp);
        } catch (RuntimeException e) {
            log.error("No fue posible enviar el correo de restablecimiento a {}: {}", correo, e.getMessage());
        }
    }
}
