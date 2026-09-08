package co.edu.udea.brujula.usuario;

import co.edu.udea.brujula.config.UsuarioPrincipal;
import co.edu.udea.brujula.usuario.AuthDtos.*;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;
    private final JwtService jwt;
    private final GoogleTokenVerifier google;

    public AuthController(AuthService auth, JwtService jwt, GoogleTokenVerifier google) {
        this.auth = auth;
        this.jwt = jwt;
        this.google = google;
    }

    /** Paso 1 de HU-001: el front entrega el ID token de Google; la API dice si hay que completar el registro. */
    @PostMapping("/google")
    public GoogleRegistroPendiente google(@Valid @RequestBody GoogleRequest req) {
        return auth.iniciarConGoogle(req.credential());
    }

    /** Paso 2 de HU-001: formulario con nombres, apellidos, contraseña y términos. */
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> registro(@Valid @RequestBody RegistroRequest req) {
        UsuarioDto u = auth.registrar(req);
        return Map.of("mensaje", "Tu cuenta fue creada exitosamente. Ya puedes iniciar sesión.", "usuario", u);
    }

    @PostMapping("/login")
    public SesionResponse login(@RequestBody LoginRequest req) {
        return auth.login(req);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UsuarioPrincipal p, @RequestHeader("Authorization") String header) {
        auth.cerrarSesion(p, expiracion(header));
        return ResponseEntity.noContent().build();
    }

    /** Renueva el token mientras el usuario sigue activo (expiración por inactividad, HU-004 CA-05). */
    @PostMapping("/refresh")
    public SesionResponse refresh(@AuthenticationPrincipal UsuarioPrincipal p, @RequestHeader("Authorization") String header) {
        return auth.refrescar(p, expiracion(header));
    }

    @GetMapping("/me")
    public UsuarioDto me(@AuthenticationPrincipal UsuarioPrincipal p) {
        return auth.perfil(p.id());
    }

    @PostMapping("/recuperar")
    public Mensaje recuperar(@RequestBody RecuperarRequest req) {
        return auth.solicitarRecuperacion(req.email());
    }

    @GetMapping("/restablecer/validar")
    public Map<String, Object> validar(@RequestParam String token) {
        auth.validarTokenRecuperacion(token);
        return Map.of("valido", true);
    }

    @PostMapping("/restablecer")
    public Mensaje restablecer(@Valid @RequestBody RestablecerRequest req) {
        return auth.restablecer(req);
    }

    /** Le dice al front cómo está configurado el inicio con Google (real o simulado en desarrollo). */
    @GetMapping("/google/config")
    public Map<String, Object> googleConfig() {
        return Map.of("simulado", google.simulado());
    }

    private Instant expiracion(String header) {
        String token = header.substring(7).trim();
        return jwt.validarSesion(token).map(Claims::getExpiration).map(java.util.Date::toInstant).orElse(null);
    }
}
