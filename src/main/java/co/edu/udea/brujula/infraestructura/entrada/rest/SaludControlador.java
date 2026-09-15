package co.edu.udea.brujula.infraestructura.entrada.rest;

import co.edu.udea.brujula.dominio.puerto.salida.Reloj;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SaludControlador {

    private final Reloj reloj;

    public SaludControlador(Reloj reloj) {
        this.reloj = reloj;
    }

    @GetMapping("/api/salud")
    public Map<String, Object> salud() {
        return Map.of("estado", "ok", "servidor", reloj.ahora().toString());
    }
}
