package co.edu.udea.brujula.infraestructura.entrada.rest;

import co.edu.udea.brujula.dominio.puerto.entrada.ActualizarPerfil;
import co.edu.udea.brujula.dominio.puerto.entrada.CambiarContrasena;
import co.edu.udea.brujula.dominio.puerto.entrada.ValidarSesion;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Peticiones.CambioPasswordRequest;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Peticiones.PerfilRequest;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Respuestas.MensajeDto;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Respuestas.UsuarioDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/perfil")
public class PerfilControlador {

    private final ActualizarPerfil actualizacion;
    private final CambiarContrasena cambioDeContrasena;

    public PerfilControlador(ActualizarPerfil actualizacion, CambiarContrasena cambioDeContrasena) {
        this.actualizacion = actualizacion;
        this.cambioDeContrasena = cambioDeContrasena;
    }

    @PutMapping
    public UsuarioDto actualizar(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                 @RequestBody PerfilRequest peticion) {
        return UsuarioDto.de(actualizacion.actualizar(usuario.id(), peticion.nombre(), peticion.apellido()));
    }

    @PutMapping("/password")
    public MensajeDto cambiarPassword(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                      @RequestBody CambioPasswordRequest peticion) {
        return new MensajeDto(cambioDeContrasena.cambiar(usuario.id(), peticion.passwordActual(),
                peticion.passwordNueva(), peticion.confirmacionPassword()));
    }
}
