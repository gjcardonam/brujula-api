package co.edu.udea.brujula.infraestructura.entrada.rest;

import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.puerto.entrada.*;
import co.edu.udea.brujula.dominio.puerto.salida.VerificadorDeGoogle;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Peticiones.*;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Respuestas.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AutenticacionControlador {

    private final RegistrarEstudiante registro;
    private final AutenticarUsuario autenticacion;
    private final GestionarSesion sesiones;
    private final RecuperarContrasena recuperacion;
    private final GestionarPerfil perfil;
    private final VerificadorDeGoogle google;

    public AutenticacionControlador(RegistrarEstudiante registro, AutenticarUsuario autenticacion,
                                    GestionarSesion sesiones, RecuperarContrasena recuperacion,
                                    GestionarPerfil perfil, VerificadorDeGoogle google) {
        this.registro = registro;
        this.autenticacion = autenticacion;
        this.sesiones = sesiones;
        this.recuperacion = recuperacion;
        this.perfil = perfil;
        this.google = google;
    }

    /** Paso 1 del registro: Google verifica el correo y la API dice si falta crear la cuenta. */
    @PostMapping("/google")
    public RegistrarEstudiante.RegistroPendiente conGoogle(@Valid @RequestBody GoogleRequest peticion) {
        return registro.iniciarConGoogle(peticion.credential());
    }

    /** Paso 2: nombres, apellidos, contraseña y aceptación de términos. */
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
        return SesionDto.de(autenticacion.autenticar(peticion.email(), peticion.password()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> cerrarSesion(@AuthenticationPrincipal ValidarSesion.Autenticado usuario) {
        sesiones.cerrar(usuario.id(), usuario.jti(), usuario.expiraEn());
        return ResponseEntity.noContent().build();
    }

    /** El front renueva el token mientras el usuario siga trabajando (HU-004 CA-05). */
    @PostMapping("/refresh")
    public SesionDto renovar(@AuthenticationPrincipal ValidarSesion.Autenticado usuario) {
        return SesionDto.de(sesiones.renovar(usuario.id(), usuario.jti(), usuario.expiraEn()));
    }

    @GetMapping("/me")
    public UsuarioDto miCuenta(@AuthenticationPrincipal ValidarSesion.Autenticado usuario) {
        return UsuarioDto.de(perfil.consultar(usuario.id()));
    }

    @PostMapping("/recuperar")
    public MensajeDto recuperar(@RequestBody RecuperarRequest peticion) {
        return new MensajeDto(recuperacion.solicitarEnlace(peticion.email()));
    }

    @GetMapping("/restablecer/validar")
    public Map<String, Object> validarEnlace(@RequestParam String token) {
        recuperacion.verificarEnlace(token);
        return Map.of("valido", true);
    }

    @PostMapping("/restablecer")
    public MensajeDto restablecer(@Valid @RequestBody RestablecerRequest peticion) {
        return new MensajeDto(recuperacion.restablecer(peticion.token(), peticion.password(),
                peticion.confirmacionPassword()));
    }

    /** El front necesita saber si el botón de Google es el real o el simulado de desarrollo. */
    @GetMapping("/google/config")
    public Map<String, Object> configuracionDeGoogle() {
        return Map.of("simulado", google.estaSimulado());
    }
}
