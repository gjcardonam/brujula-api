package co.edu.udea.brujula.infraestructura.configuracion;

import co.edu.udea.brujula.dominio.puerto.entrada.ValidarSesion;
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
import java.text.Normalizer;
import java.util.List;

/**
 * Adaptador de entrada de la seguridad: saca el token de la cabecera y le pregunta al caso de uso
 * si la sesión sirve. Las reglas no están aquí.
 */
@Component
public class FiltroDeAutenticacion extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final ValidarSesion validarSesion;

    public FiltroDeAutenticacion(ValidarSesion validarSesion) {
        this.validarSesion = validarSesion;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {
        String cabecera = peticion.getHeader("Authorization");
        if (cabecera != null && cabecera.startsWith(PREFIJO)) {
            validarSesion.validar(cabecera.substring(PREFIJO.length()).trim()).ifPresent(usuario -> {
                var autenticacion = new UsernamePasswordAuthenticationToken(usuario, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + comoAutoridad(usuario.rol()))));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            });
        }
        cadena.doFilter(peticion, respuesta);
    }

    /** "Administrador" queda como ADMINISTRADOR y "Estudiante" como ESTUDIANTE, sin tildes. */
    static String comoAutoridad(String rol) {
        return Normalizer.normalize(rol, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toUpperCase();
    }
}
