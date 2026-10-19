package co.edu.udea.brujula.infraestructura.entrada.rest;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarErroresDelEstudiante;
import co.edu.udea.brujula.dominio.puerto.entrada.ValidarSesion;
import co.edu.udea.brujula.dominio.modelo.consulta.ErroresDelEstudiante;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/estadisticas")
@PreAuthorize("hasRole('ESTUDIANTE')")
public class ErroresDelEstudianteControlador {
    private final ConsultarErroresDelEstudiante errores;
    public ErroresDelEstudianteControlador(ConsultarErroresDelEstudiante errores) { this.errores = errores; }
    @GetMapping("/errores")
    public ErroresDelEstudiante consultar(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                         @RequestParam(required = false) Long componente) {
        return errores.consultar(usuario.id(), componente);
    }
}
