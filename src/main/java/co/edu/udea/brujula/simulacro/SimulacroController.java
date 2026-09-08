package co.edu.udea.brujula.simulacro;

import co.edu.udea.brujula.common.PageResponse;
import co.edu.udea.brujula.config.UsuarioPrincipal;
import co.edu.udea.brujula.simulacro.SimulacroDtos.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/simulacros")
@PreAuthorize("hasRole('ESTUDIANTE')")
public class SimulacroController {

    private final SimulacroService servicio;

    public SimulacroController(SimulacroService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Estado crear(@AuthenticationPrincipal UsuarioPrincipal p, @RequestBody CrearRequest req) {
        return servicio.crear(p.id(), req.idDuracion());
    }

    @GetMapping("/en-curso")
    public ResponseEntity<Estado> enCurso(@AuthenticationPrincipal UsuarioPrincipal p) {
        return servicio.enCurso(p.id()).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/mios")
    public PageResponse<Resumen> historial(@AuthenticationPrincipal UsuarioPrincipal p, @RequestParam(defaultValue = "0") int pagina) {
        return servicio.historial(p.id(), pagina);
    }

    @GetMapping("/{id}")
    public Estado estado(@AuthenticationPrincipal UsuarioPrincipal p, @PathVariable Long id) {
        return servicio.estado(p.id(), id);
    }

    @GetMapping("/{id}/siguiente-ejercicio")
    public Siguiente siguiente(@AuthenticationPrincipal UsuarioPrincipal p, @PathVariable Long id) {
        return servicio.siguiente(p.id(), id);
    }

    @PostMapping("/{id}/finalizar")
    public Resultado finalizar(@AuthenticationPrincipal UsuarioPrincipal p, @PathVariable Long id) {
        return servicio.finalizarVoluntario(p.id(), id);
    }

    @GetMapping("/{id}/resultado")
    public Resultado resultado(@AuthenticationPrincipal UsuarioPrincipal p, @PathVariable Long id) {
        return servicio.resultado(p.id(), id);
    }
}
