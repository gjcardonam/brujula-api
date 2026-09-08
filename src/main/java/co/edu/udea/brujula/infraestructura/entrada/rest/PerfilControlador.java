package co.edu.udea.brujula.infraestructura.entrada.rest;

import co.edu.udea.brujula.dominio.puerto.entrada.GestionarPerfil;
import co.edu.udea.brujula.dominio.puerto.entrada.ValidarSesion;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Peticiones.CambioPasswordRequest;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Peticiones.PerfilRequest;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Respuestas.MensajeDto;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Respuestas.UsuarioDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/perfil")
public class PerfilControlador {

    private final GestionarPerfil perfil;

    public PerfilControlador(GestionarPerfil perfil) {
        this.perfil = perfil;
    }

    @GetMapping
    public UsuarioDto consultar(@AuthenticationPrincipal ValidarSesion.Autenticado usuario) {
        return UsuarioDto.de(perfil.consultar(usuario.id()));
    }

    @PutMapping
    public UsuarioDto actualizar(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                 @RequestBody PerfilRequest peticion) {
        return UsuarioDto.de(perfil.actualizarDatos(usuario.id(), peticion.nombre(), peticion.apellido()));
    }

    @PutMapping("/password")
    public MensajeDto cambiarPassword(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                      @RequestBody CambioPasswordRequest peticion) {
        return new MensajeDto(perfil.cambiarContrasena(usuario.id(), peticion.passwordActual(),
                peticion.passwordNueva(), peticion.confirmacionPassword()));
    }
}
