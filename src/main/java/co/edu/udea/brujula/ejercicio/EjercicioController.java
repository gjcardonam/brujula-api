package co.edu.udea.brujula.ejercicio;

import co.edu.udea.brujula.common.ApiException;
import co.edu.udea.brujula.common.PageResponse;
import co.edu.udea.brujula.config.UsuarioPrincipal;
import co.edu.udea.brujula.ejercicio.EjercicioDtos.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ejercicios")
public class EjercicioController {

    private final EjercicioService servicio;

    public EjercicioController(EjercicioService servicio) {
        this.servicio = servicio;
    }

    /** Banco de ejercicios (HU-006, HU-007, HU-008). */
    @GetMapping
    public PageResponse<Tarjeta> listar(@AuthenticationPrincipal UsuarioPrincipal p,
                                        @RequestParam(required = false) Long componente,
                                        @RequestParam(defaultValue = "0") int pagina) {
        return servicio.listar(p, componente, pagina);
    }

    /** Componentes con cantidad de ejercicios consultables según el rol (HU-007 CA-02/CA-03/CA-07). */
    @GetMapping("/componentes")
    public Componentes componentes(@AuthenticationPrincipal UsuarioPrincipal p) {
        return servicio.componentesConConteo(p);
    }

    /** Ejercicio para resolver (HU-009). El administrador recibe el detalle completo (HU-022). */
    @GetMapping("/{id}")
    public Object ver(@AuthenticationPrincipal UsuarioPrincipal p, @PathVariable Long id) {
        if (p.esAdministrador()) return servicio.paraAdministrador(id);
        return servicio.paraEstudiante(p.id(), id);
    }

    /** Siguiente ejercicio en práctica libre (HU-010 CA-07 a CA-09). */
    @GetMapping("/{id}/siguiente")
    public Map<String, Object> siguiente(@AuthenticationPrincipal UsuarioPrincipal p, @PathVariable Long id,
                                         @RequestParam(required = false) Long componente) {
        return servicio.siguiente(p.id(), id, componente);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public EjercicioAdmin crear(@AuthenticationPrincipal UsuarioPrincipal p, @RequestBody EjercicioRequest req) {
        return servicio.crear(p.id(), req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public EjercicioAdmin editar(@AuthenticationPrincipal UsuarioPrincipal p, @PathVariable Long id, @RequestBody EjercicioRequest req) {
        return servicio.editar(p.id(), id, req);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public EjercicioAdmin cambiarEstado(@AuthenticationPrincipal UsuarioPrincipal p, @PathVariable Long id, @RequestBody CambioEstadoRequest req) {
        if (req == null || req.estado() == null) throw ApiException.badRequest("ESTADO_INVALIDO", "Debes indicar el estado.");
        return servicio.cambiarEstado(p.id(), id, req.estado());
    }
}
