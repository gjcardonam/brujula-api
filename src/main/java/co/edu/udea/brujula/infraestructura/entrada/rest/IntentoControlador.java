package co.edu.udea.brujula.infraestructura.entrada.rest;

import co.edu.udea.brujula.dominio.modelo.consulta.ResultadoDeIntento;
import co.edu.udea.brujula.dominio.puerto.entrada.RegistrarIntento;
import co.edu.udea.brujula.dominio.puerto.entrada.ValidarSesion;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Peticiones.IntentoRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/intentos")
@PreAuthorize("hasRole('ESTUDIANTE')")
public class IntentoControlador {

    private final RegistrarIntento registro;

    public IntentoControlador(RegistrarIntento registro) {
        this.registro = registro;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResultadoDeIntento registrar(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                        @RequestBody IntentoRequest peticion) {
        return registro.registrar(usuario.id(), new RegistrarIntento.Respuesta(peticion.idEjercicio(),
                peticion.idOpcion(), peticion.nivelConfianza(), peticion.tokenIdempotencia()));
    }
}
