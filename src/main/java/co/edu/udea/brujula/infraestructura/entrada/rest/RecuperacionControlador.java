package co.edu.udea.brujula.infraestructura.entrada.rest;

import co.edu.udea.brujula.dominio.puerto.entrada.RestablecerContrasena;
import co.edu.udea.brujula.dominio.puerto.entrada.SolicitarRecuperacion;
import co.edu.udea.brujula.dominio.puerto.entrada.VerificarEnlaceDeRecuperacion;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Peticiones.RecuperarRequest;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Peticiones.RestablecerRequest;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Respuestas.MensajeDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class RecuperacionControlador {

    private final SolicitarRecuperacion solicitud;
    private final VerificarEnlaceDeRecuperacion verificacion;
    private final RestablecerContrasena restablecimiento;

    public RecuperacionControlador(SolicitarRecuperacion solicitud, VerificarEnlaceDeRecuperacion verificacion,
                                   RestablecerContrasena restablecimiento) {
        this.solicitud = solicitud;
        this.verificacion = verificacion;
        this.restablecimiento = restablecimiento;
    }

    @PostMapping("/recuperar")
    public MensajeDto recuperar(@RequestBody RecuperarRequest peticion) {
        return new MensajeDto(solicitud.solicitar(peticion.email()));
    }

    @GetMapping("/restablecer/validar")
    public Map<String, Object> validarEnlace(@RequestParam String token) {
        verificacion.verificar(token);
        return Map.of("valido", true);
    }

    @PostMapping("/restablecer")
    public MensajeDto restablecer(@Valid @RequestBody RestablecerRequest peticion) {
        return new MensajeDto(restablecimiento.restablecer(peticion.token(), peticion.password(),
                peticion.confirmacionPassword()));
    }
}
