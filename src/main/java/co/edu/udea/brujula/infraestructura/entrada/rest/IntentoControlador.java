package co.edu.udea.brujula.infraestructura.entrada.rest;

import co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeIntento;
import co.edu.udea.brujula.dominio.modelo.consulta.ResumenDeIntento;
import co.edu.udea.brujula.dominio.modelo.consulta.ResultadoDeIntento;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarHistorialDeIntentos;
import co.edu.udea.brujula.dominio.puerto.entrada.RegistrarIntento;
import co.edu.udea.brujula.dominio.puerto.entrada.ValidarSesion;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Peticiones.IntentoRequest;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Respuestas.PaginaDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/intentos")
@PreAuthorize("hasRole('ESTUDIANTE')")
public class IntentoControlador {

    private final RegistrarIntento registro;
    private final ConsultarHistorialDeIntentos historial;

    public IntentoControlador(RegistrarIntento registro, ConsultarHistorialDeIntentos historial) {
        this.registro = registro;
        this.historial = historial;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResultadoDeIntento registrar(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                        @RequestBody IntentoRequest peticion) {
        return registro.registrar(usuario.id(), new RegistrarIntento.Respuesta(peticion.idEjercicio(),
                peticion.idOpcion(), peticion.nivelConfianza(), peticion.tokenIdempotencia()));
    }

    @GetMapping("/mios")
    public PaginaDto<ResumenDeIntento> mios(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                            @RequestParam(defaultValue = "0") int pagina) {
        return PaginaDto.de(historial.listar(usuario.id(), pagina));
    }

    @GetMapping("/{id}")
    public DetalleDeIntento detalle(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                    @PathVariable Long id) {
        return historial.detalle(usuario.id(), id);
    }
}
