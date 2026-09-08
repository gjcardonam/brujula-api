package co.edu.udea.brujula.infraestructura.entrada.rest;

import co.edu.udea.brujula.dominio.modelo.consulta.Estadisticas;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarEstadisticas;
import co.edu.udea.brujula.dominio.puerto.entrada.ValidarSesion;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/estadisticas")
@PreAuthorize("hasRole('ESTUDIANTE')")
public class EstadisticaControlador {

    private final ConsultarEstadisticas estadisticas;

    public EstadisticaControlador(ConsultarEstadisticas estadisticas) {
        this.estadisticas = estadisticas;
    }

    /** El parámetro componente filtra únicamente la distribución de errores (HU-029 CA-03). */
    @GetMapping("/mias")
    public Estadisticas mias(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                             @RequestParam(required = false) Long componente) {
        return estadisticas.deEstudiante(usuario.id(), componente);
    }
}
