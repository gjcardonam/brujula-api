package co.edu.udea.brujula.infraestructura.entrada.rest;

import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.puerto.entrada.CerrarSesion;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarPerfil;
import co.edu.udea.brujula.dominio.puerto.entrada.IniciarSesion;
import co.edu.udea.brujula.dominio.puerto.entrada.RegistrarConGoogle;
import co.edu.udea.brujula.dominio.puerto.entrada.RegistrarEstudiante;
import co.edu.udea.brujula.dominio.puerto.entrada.RenovarSesion;
import co.edu.udea.brujula.dominio.puerto.entrada.ValidarSesion;
import co.edu.udea.brujula.dominio.puerto.salida.VerificadorDeGoogle;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Peticiones.GoogleRequest;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Peticiones.LoginRequest;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Peticiones.RegistroRequest;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Respuestas.SesionDto;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Respuestas.UsuarioDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AutenticacionControlador {

    private final RegistrarConGoogle registroConGoogle;
    private final RegistrarEstudiante registro;
    private final IniciarSesion inicioDeSesion;
    private final CerrarSesion cierreDeSesion;
    private final RenovarSesion renovacionDeSesion;
    private final ConsultarPerfil perfil;
    private final VerificadorDeGoogle google;

    public AutenticacionControlador(RegistrarConGoogle registroConGoogle, RegistrarEstudiante registro,
                                    IniciarSesion inicioDeSesion, CerrarSesion cierreDeSesion,
                                    RenovarSesion renovacionDeSesion, ConsultarPerfil perfil,
                                    VerificadorDeGoogle google) {
        this.registroConGoogle = registroConGoogle;
        this.registro = registro;
        this.inicioDeSesion = inicioDeSesion;
        this.cierreDeSesion = cierreDeSesion;
        this.renovacionDeSesion = renovacionDeSesion;
        this.perfil = perfil;
        this.google = google;
    }

    @PostMapping("/google")
    public RegistrarConGoogle.RegistroPendiente conGoogle(@Valid @RequestBody GoogleRequest peticion) {
        return registroConGoogle.iniciar(peticion.credential());
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> registrar(@Valid @RequestBody RegistroRequest peticion) {
        Usuario creado = registro.registrar(new RegistrarEstudiante.Datos(peticion.registroToken(), peticion.nombre(),
                peticion.apellido(), peticion.password(), peticion.confirmacionPassword(), peticion.aceptoTerminos()));
        return Map.of("mensaje", "Tu cuenta fue creada exitosamente. Ya puedes iniciar sesión.",
                "usuario", UsuarioDto.de(creado));
    }

    @PostMapping("/login")
    public SesionDto iniciarSesion(@RequestBody LoginRequest peticion) {
        return SesionDto.de(inicioDeSesion.iniciar(peticion.email(), peticion.password()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> cerrarSesion(@AuthenticationPrincipal ValidarSesion.Autenticado usuario) {
        cierreDeSesion.cerrar(usuario.id(), usuario.jti(), usuario.expiraEn());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public SesionDto renovar(@AuthenticationPrincipal ValidarSesion.Autenticado usuario) {
        return SesionDto.de(renovacionDeSesion.renovar(usuario.id(), usuario.jti(), usuario.expiraEn()));
    }

    @GetMapping("/me")
    public UsuarioDto miCuenta(@AuthenticationPrincipal ValidarSesion.Autenticado usuario) {
        return UsuarioDto.de(perfil.consultar(usuario.id()));
    }

    @GetMapping("/google/config")
    public Map<String, Object> configuracionDeGoogle() {
        return Map.of("simulado", google.estaSimulado());
    }
}
