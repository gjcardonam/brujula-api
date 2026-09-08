package co.edu.udea.brujula.config;

import co.edu.udea.brujula.usuario.JwtService;
import co.edu.udea.brujula.usuario.TokenSesionRevocadoRepository;
import co.edu.udea.brujula.usuario.Usuario;
import co.edu.udea.brujula.usuario.UsuarioRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Valida el token de sesión (HU-002, HU-004): firma, expiración, revocación (cierre de sesión)
 * y emisión posterior al último restablecimiento de contraseña (HU-003 CA-07).
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenSesionRevocadoRepository revocados;
    private final UsuarioRepository usuarios;

    public JwtAuthFilter(JwtService jwtService, TokenSesionRevocadoRepository revocados, UsuarioRepository usuarios) {
        this.jwtService = jwtService;
        this.revocados = revocados;
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            Optional<Claims> claims = jwtService.validarSesion(token);
            if (claims.isPresent()) {
                Claims c = claims.get();
                String jti = c.getId();
                Long idUsuario = Long.valueOf(c.getSubject());
                if (!revocados.existsById(jti)) {
                    Optional<Usuario> u = usuarios.findById(idUsuario);
                    if (u.isPresent() && "Activo".equals(u.get().getEstado()) && emitidoTrasUltimoCambio(c, u.get())) {
                        Usuario usuario = u.get();
                        String rol = usuario.getRol().getNombreRol();
                        UsuarioPrincipal principal = new UsuarioPrincipal(usuario.getId(), usuario.getEmail(), rol, jti);
                        var auth = new UsernamePasswordAuthenticationToken(principal, null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + normalizar(rol))));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            }
        }
        chain.doFilter(request, response);
    }

    private boolean emitidoTrasUltimoCambio(Claims c, Usuario u) {
        if (u.getPasswordActualizadoEn() == null || c.getIssuedAt() == null) return true;
        Instant emitido = c.getIssuedAt().toInstant();
        // El JWT guarda segundos; se tolera el mismo segundo.
        return !emitido.plusSeconds(1).isBefore(u.getPasswordActualizadoEn());
    }

    /** "Administrador" → ADMINISTRADOR, "Estudiante" → ESTUDIANTE. */
    public static String normalizar(String rol) {
        return java.text.Normalizer.normalize(rol, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toUpperCase();
    }
}
