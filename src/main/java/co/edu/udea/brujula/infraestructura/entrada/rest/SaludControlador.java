package co.edu.udea.brujula.infraestructura.entrada.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/** Lo consulta Docker y sirve para saber si la API quedó arriba. */
@RestController
public class SaludControlador {

    @GetMapping("/api/salud")
    public Map<String, Object> salud() {
        return Map.of("estado", "ok", "servidor", Instant.now().toString());
    }
}
