package co.edu.udea.brujula.infraestructura.entrada.rest;

import co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeIntento;
import co.edu.udea.brujula.dominio.modelo.consulta.ResultadoDeIntento;
import co.edu.udea.brujula.dominio.modelo.consulta.ResumenDeIntento;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarHistorialDeIntentos;
import co.edu.udea.brujula.dominio.puerto.entrada.ResponderEjercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.ValidarSesion;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Peticiones.IntentoRequest;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Respuestas.PaginaDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/intentos")
@PreAuthorize("hasRole('ESTUDIANTE')")
public class IntentoControlador {

    private final ResponderEjercicio responder;
    private final ConsultarHistorialDeIntentos historial;

    public IntentoControlador(ResponderEjercicio responder, ConsultarHistorialDeIntentos historial) {
        this.responder = responder;
        this.historial = historial;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResultadoDeIntento registrar(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                        @RequestBody IntentoRequest peticion) {
        var respuesta = new ResponderEjercicio.Respuesta(peticion.idEjercicio(), peticion.idOpcion(),
                peticion.nivelConfianza(), peticion.tokenIdempotencia(), peticion.idSimulacro());
        return responder.responder(usuario.id(), respuesta);
    }

    @GetMapping("/mios")
    public PaginaDto<ResumenDeIntento> mios(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                            @RequestParam(defaultValue = "0") int pagina) {
        return PaginaDto.de(historial.listar(usuario.id(), pagina));
    }

    @GetMapping("/{id}")
    public DetalleDeIntento detalle(@AuthenticationPrincipal ValidarSesion.Autenticado usuario, @PathVariable Long id) {
        return historial.detalle(usuario.id(), id);
    }
}
