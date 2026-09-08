package co.edu.udea.brujula.practica;

import co.edu.udea.brujula.common.PageResponse;
import co.edu.udea.brujula.config.UsuarioPrincipal;
import co.edu.udea.brujula.practica.IntentoDtos.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/intentos")
@PreAuthorize("hasRole('ESTUDIANTE')")
public class IntentoController {

    private final IntentoService servicio;

    public IntentoController(IntentoService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResultadoIntento registrar(@AuthenticationPrincipal UsuarioPrincipal p, @RequestBody IntentoRequest req) {
        return servicio.registrar(p.id(), req);
    }

    @GetMapping("/mios")
    public PageResponse<IntentoResumen> historial(@AuthenticationPrincipal UsuarioPrincipal p, @RequestParam(defaultValue = "0") int pagina) {
        return servicio.historial(p.id(), pagina);
    }

    @GetMapping("/{id}")
    public IntentoDetalle detalle(@AuthenticationPrincipal UsuarioPrincipal p, @PathVariable Long id) {
        return servicio.detalle(p.id(), id);
    }
}
