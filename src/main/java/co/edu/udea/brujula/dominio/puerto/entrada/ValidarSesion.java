package co.edu.udea.brujula.dominio.puerto.entrada;

import java.time.Instant;
import java.util.Optional;

/**
 * Lo usa el filtro de seguridad en cada petición. Además de la firma revisa que la sesión no esté
 * revocada, que la cuenta siga activa y que el token no sea anterior al último cambio de contraseña.
 */
public interface ValidarSesion {

    record Autenticado(Long id, String email, String rol, String jti, Instant expiraEn) {
        public boolean esAdministrador() {
            return "Administrador".equals(rol);
        }
    }

    Optional<Autenticado> validar(String token);
}
