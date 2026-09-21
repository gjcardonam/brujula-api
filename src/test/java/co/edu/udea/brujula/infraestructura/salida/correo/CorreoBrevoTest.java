package co.edu.udea.brujula.infraestructura.salida.correo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import co.edu.udea.brujula.infraestructura.configuracion.BrujulaProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("Envío del correo de recuperación a través de la API HTTPS de Brevo")
class CorreoBrevoTest {

    private static final String CORREO_FROM = "no-reply@brujula.local";
    private static final String CLAVE_DE_API = "clave-secreta-de-prueba-jamas-debe-salir-en-un-log";

    private HttpServer servidor;

    @AfterEach
    void detenerServidor() {
        if (servidor != null) {
            servidor.stop(0);
        }
    }

    private BrujulaProperties propiedades() {
        return new BrujulaProperties(null, "https://brujula.udea.edu.co/", null, false, null,
                CORREO_FROM, null, true, CLAVE_DE_API);
    }

    private record PeticionCapturada(String metodo, String ruta, String apiKey, String contentType, String cuerpo) {
    }

    private HttpServer servidorQueResponde(BlockingQueue<PeticionCapturada> capturas, int estado, String cuerpoDeRespuesta)
            throws IOException {
        HttpServer servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/v3/smtp/email", intercambio -> {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            intercambio.getRequestBody().transferTo(bytes);
            capturas.add(new PeticionCapturada(intercambio.getRequestMethod(), intercambio.getRequestURI().getPath(),
                    intercambio.getRequestHeaders().getFirst("api-key"),
                    intercambio.getRequestHeaders().getFirst("content-type"),
                    bytes.toString(StandardCharsets.UTF_8)));
            byte[] respuesta = cuerpoDeRespuesta.getBytes(StandardCharsets.UTF_8);
            intercambio.sendResponseHeaders(estado, respuesta.length);
            intercambio.getResponseBody().write(respuesta);
            intercambio.close();
        });
        servidor.start();
        return servidor;
    }

    private String urlBase() {
        return "http://localhost:" + servidor.getAddress().getPort();
    }

    @Test
    void llama_a_la_ruta_correcta_con_la_cabecera_api_key_y_un_201_se_considera_exito() throws Exception {
        BlockingQueue<PeticionCapturada> capturas = new ArrayBlockingQueue<>(1);
        servidor = servidorQueResponde(capturas, 201, "{\"messageId\":\"abc-123\"}");
        CorreoBrevo adaptador = new CorreoBrevo(propiedades(), CLAVE_DE_API, urlBase(), new ObjectMapper());

        adaptador.enviarEnlaceDeRecuperacion("estudiante@udea.edu.co", "token-123", 30);

        PeticionCapturada peticion = capturas.poll(5, TimeUnit.SECONDS);
        if (peticion == null) fail("El adaptador nunca llamó al servidor de prueba");
        assertEquals("POST", peticion.metodo());
        assertEquals("/v3/smtp/email", peticion.ruta());
        assertEquals(CLAVE_DE_API, peticion.apiKey());
        assertTrue(peticion.contentType().contains("application/json"));
    }

    @Test
    void el_cuerpo_lleva_remitente_destinatario_asunto_y_el_enlace_con_tildes_intactas() throws Exception {
        BlockingQueue<PeticionCapturada> capturas = new ArrayBlockingQueue<>(1);
        servidor = servidorQueResponde(capturas, 201, "{\"messageId\":\"abc-123\"}");
        CorreoBrevo adaptador = new CorreoBrevo(propiedades(), CLAVE_DE_API, urlBase(), new ObjectMapper());

        adaptador.enviarEnlaceDeRecuperacion("estudiante@udea.edu.co", "token-123", 30);

        PeticionCapturada peticion = capturas.poll(5, TimeUnit.SECONDS);
        if (peticion == null) fail("El adaptador nunca llamó al servidor de prueba");
        JsonNode cuerpo = new ObjectMapper().readTree(peticion.cuerpo());
        assertEquals(CORREO_FROM, cuerpo.get("sender").get("email").asText());
        assertEquals("Brújula", cuerpo.get("sender").get("name").asText());
        assertEquals("estudiante@udea.edu.co", cuerpo.get("to").get(0).get("email").asText());
        assertEquals("Brújula · Restablece tu contraseña", cuerpo.get("subject").asText());
        String enlaceEsperado = "https://brujula.udea.edu.co/restablecer?token=token-123";
        assertTrue(cuerpo.get("textContent").asText().contains(enlaceEsperado));
        assertTrue(cuerpo.get("htmlContent").asText().contains(enlaceEsperado));
        assertTrue(cuerpo.get("textContent").asText().contains("contraseña"));
        assertTrue(cuerpo.get("htmlContent").asText().contains("contraseña"));
    }

    @Test
    void un_400_se_registra_con_el_codigo_y_el_cuerpo_sin_exponer_la_clave_y_sin_lanzar_excepcion() throws Exception {
        BlockingQueue<PeticionCapturada> capturas = new ArrayBlockingQueue<>(1);
        servidor = servidorQueResponde(capturas, 400, "{\"code\":\"invalid_parameter\",\"message\":\"malformed sender\"}");
        CorreoBrevo adaptador = new CorreoBrevo(propiedades(), CLAVE_DE_API, urlBase(), new ObjectMapper());

        Logger logger = (Logger) LoggerFactory.getLogger(CorreoBrevo.class);
        ListAppender<ILoggingEvent> apendedor = new ListAppender<>();
        apendedor.start();
        logger.addAppender(apendedor);
        try {
            adaptador.enviarEnlaceDeRecuperacion("estudiante@udea.edu.co", "token-123", 30);
        } finally {
            logger.detachAppender(apendedor);
        }

        List<ILoggingEvent> eventos = apendedor.list;
        assertFalse(eventos.isEmpty(), "Se esperaba un log de error tras el 400");
        ILoggingEvent evento = eventos.get(0);
        assertEquals(Level.ERROR, evento.getLevel());
        String mensaje = evento.getFormattedMessage();
        assertTrue(mensaje.contains("400"));
        assertTrue(mensaje.contains("invalid_parameter"));
        assertFalse(mensaje.contains(CLAVE_DE_API));
    }

    @Test
    void una_falla_de_red_se_registra_sin_lanzar_excepcion() {
        CorreoBrevo adaptador = new CorreoBrevo(propiedades(), CLAVE_DE_API, "http://localhost:1", new ObjectMapper());

        Logger logger = (Logger) LoggerFactory.getLogger(CorreoBrevo.class);
        ListAppender<ILoggingEvent> apendedor = new ListAppender<>();
        apendedor.start();
        logger.addAppender(apendedor);
        try {
            adaptador.enviarEnlaceDeRecuperacion("estudiante@udea.edu.co", "token-123", 30);
        } finally {
            logger.detachAppender(apendedor);
        }

        assertFalse(apendedor.list.isEmpty(), "Se esperaba un log de error tras la falla de conexión");
        assertEquals(Level.ERROR, apendedor.list.get(0).getLevel());
        assertFalse(apendedor.list.get(0).getFormattedMessage().contains(CLAVE_DE_API));
    }
}
