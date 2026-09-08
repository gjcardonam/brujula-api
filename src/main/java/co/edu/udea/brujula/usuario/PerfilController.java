package co.edu.udea.brujula.usuario;

import co.edu.udea.brujula.config.UsuarioPrincipal;
import co.edu.udea.brujula.usuario.AuthDtos.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final AuthService auth;

    public PerfilController(AuthService auth) {
        this.auth = auth;
    }

    @GetMapping
    public UsuarioDto ver(@AuthenticationPrincipal UsuarioPrincipal p) {
        return auth.perfil(p.id());
    }

    @PutMapping
    public UsuarioDto actualizar(@AuthenticationPrincipal UsuarioPrincipal p, @RequestBody PerfilRequest req) {
        return auth.actualizarPerfil(p.id(), req);
    }

    @PutMapping("/password")
    public Mensaje cambiarPassword(@AuthenticationPrincipal UsuarioPrincipal p, @RequestBody CambioPasswordRequest req) {
        return auth.cambiarPassword(p.id(), req);
    }
}
