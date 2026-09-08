package co.edu.udea.brujula.infraestructura.entrada.rest;

import co.edu.udea.brujula.dominio.modelo.consulta.EstadoDelSimulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.ResumenDeSimulacro;
import co.edu.udea.brujula.dominio.puerto.entrada.GestionarSimulacro;
import co.edu.udea.brujula.dominio.puerto.entrada.ValidarSesion;
import co.edu.udea.brujula.dominio.puerto.salida.AlmacenDeImagenes;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Peticiones.SimulacroRequest;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Respuestas.PaginaDto;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Respuestas.ResultadoSimulacroDto;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Respuestas.SiguienteSimulacroDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/simulacros")
@PreAuthorize("hasRole('ESTUDIANTE')")
public class SimulacroControlador {

    private final GestionarSimulacro simulacros;
    private final AlmacenDeImagenes imagenes;

    public SimulacroControlador(GestionarSimulacro simulacros, AlmacenDeImagenes imagenes) {
        this.simulacros = simulacros;
        this.imagenes = imagenes;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EstadoDelSimulacro iniciar(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                      @RequestBody SimulacroRequest peticion) {
        return simulacros.iniciar(usuario.id(), peticion.idDuracion());
    }

    @GetMapping("/en-curso")
    public ResponseEntity<EstadoDelSimulacro> enCurso(@AuthenticationPrincipal ValidarSesion.Autenticado usuario) {
        return simulacros.enCurso(usuario.id())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/mios")
    public PaginaDto<ResumenDeSimulacro> mios(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                              @RequestParam(defaultValue = "0") int pagina) {
        return PaginaDto.de(simulacros.historial(usuario.id(), pagina));
    }

    @GetMapping("/{id}")
    public EstadoDelSimulacro estado(@AuthenticationPrincipal ValidarSesion.Autenticado usuario, @PathVariable Long id) {
        return simulacros.estado(usuario.id(), id);
    }

    @GetMapping("/{id}/siguiente-ejercicio")
    public SiguienteSimulacroDto siguienteEjercicio(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                                    @PathVariable Long id) {
        return SiguienteSimulacroDto.de(simulacros.siguienteEjercicio(usuario.id(), id), imagenes);
    }

    @PostMapping("/{id}/finalizar")
    public ResultadoSimulacroDto finalizar(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                           @PathVariable Long id) {
        return ResultadoSimulacroDto.de(simulacros.finalizar(usuario.id(), id));
    }

    @GetMapping("/{id}/resultado")
    public ResultadoSimulacroDto resultado(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                           @PathVariable Long id) {
        return ResultadoSimulacroDto.de(simulacros.resultado(usuario.id(), id));
    }
}
