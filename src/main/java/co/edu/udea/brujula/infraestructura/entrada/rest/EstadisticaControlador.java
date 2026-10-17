package co.edu.udea.brujula.infraestructura.entrada.rest;

import co.edu.udea.brujula.dominio.modelo.consulta.Estadisticas;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarEstadisticas;
import co.edu.udea.brujula.dominio.puerto.entrada.ValidarSesion;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estadisticas")
@PreAuthorize("hasRole('ESTUDIANTE')")
public class EstadisticaControlador {

    private final ConsultarEstadisticas estadisticas;

    public EstadisticaControlador(ConsultarEstadisticas estadisticas) {
        this.estadisticas = estadisticas;
    }

    @GetMapping("/mias")
    public Estadisticas mias(@AuthenticationPrincipal ValidarSesion.Autenticado usuario) {
        return estadisticas.consultar(usuario.id());
    }
}
