package co.edu.udea.brujula.estadistica;

import co.edu.udea.brujula.config.UsuarioPrincipal;
import co.edu.udea.brujula.estadistica.EstadisticaDtos.Estadisticas;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/estadisticas")
@PreAuthorize("hasRole('ESTUDIANTE')")
public class EstadisticaController {

    private final EstadisticaService servicio;

    public EstadisticaController(EstadisticaService servicio) {
        this.servicio = servicio;
    }

    /** HU-019 y HU-029. `componente` filtra solo la distribución de errores (HU-029 CA-03). */
    @GetMapping("/mias")
    public Estadisticas mias(@AuthenticationPrincipal UsuarioPrincipal p, @RequestParam(required = false) Long componente) {
        return servicio.mias(p.id(), componente);
    }
}
