package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.Rol;

import java.time.Instant;
import java.util.Optional;

public interface ValidarSesion {

    record Autenticado(Long id, String email, String rol, String jti, Instant expiraEn) {

        public boolean esAdministrador() {
            return Rol.ADMINISTRADOR.equals(rol);
        }
    }

    Optional<Autenticado> validar(String token);
}
