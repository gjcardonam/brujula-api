package co.edu.udea.brujula.infraestructura.salida.correo;

import co.edu.udea.brujula.dominio.puerto.salida.NotificadorDeCorreo;
import co.edu.udea.brujula.infraestructura.configuracion.BrujulaProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

public class CorreoBrevo implements NotificadorDeCorreo {

    private static final String RUTA_DE_ENVIO = "/v3/smtp/email";
    private static final String NOMBRE_DEL_REMITENTE = "Brújula";
    private static final Duration TIEMPO_DE_CONEXION = Duration.ofSeconds(10);
    private static final Duration TIEMPO_DE_RESPUESTA = Duration.ofSeconds(15);
    private static final int CODIGO_DE_EXITO = 201;

    private static final Logger log = LoggerFactory.getLogger(CorreoBrevo.class);

    private record Remitente(String name, String email) {
    }

    private record Destinatario(String email) {
    }

    private record PeticionDeEnvio(Remitente sender, List<Destinatario> to, String subject, String htmlContent,
                                    String textContent) {
    }

    private final BrujulaProperties propiedades;
    private final String apiKey;
    private final String urlBase;
    private final ObjectMapper json;
    private final HttpClient cliente;

    public CorreoBrevo(BrujulaProperties propiedades, String apiKey, String urlBase, ObjectMapper json) {
        this.propiedades = propiedades;
        this.apiKey = apiKey;
        this.urlBase = urlBase;
        this.json = json;
        this.cliente = HttpClient.newBuilder().connectTimeout(TIEMPO_DE_CONEXION).build();
    }

    @Override
    public void enviarEnlaceDeRecuperacion(String correo, String token, int minutosDeVigencia) {
        MensajeDeRecuperacion.Mensaje mensaje =
                MensajeDeRecuperacion.paraEnlace(propiedades.frontendUrl(), token, minutosDeVigencia);
        try {
            String cuerpo = json.writeValueAsString(peticionPara(correo, mensaje));
            HttpRequest peticion = HttpRequest.newBuilder(URI.create(urlBase + RUTA_DE_ENVIO))
                    .timeout(TIEMPO_DE_RESPUESTA)
                    .header("api-key", apiKey)
                    .header("content-type", "application/json")
                    .header("accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(cuerpo, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() != CODIGO_DE_EXITO) {
                log.error("Brevo respondió {} al enviar el correo de restablecimiento a {}: {}",
                        respuesta.statusCode(), correo, respuesta.body());
            }
        } catch (IOException e) {
            log.error("No fue posible enviar el correo de restablecimiento a {} vía Brevo: {}", correo, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("El envío del correo de restablecimiento a {} vía Brevo fue interrumpido", correo);
        }
    }

    private PeticionDeEnvio peticionPara(String correo, MensajeDeRecuperacion.Mensaje mensaje) {
        return new PeticionDeEnvio(new Remitente(NOMBRE_DEL_REMITENTE, propiedades.mailFrom()),
                List.of(new Destinatario(correo)), mensaje.asunto(), mensaje.html(), mensaje.texto());
    }
}
