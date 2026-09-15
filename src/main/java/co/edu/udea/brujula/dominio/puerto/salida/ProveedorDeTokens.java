package co.edu.udea.brujula.dominio.puerto.salida;

import co.edu.udea.brujula.dominio.modelo.Usuario;

import java.time.Instant;
import java.util.Optional;

public interface ProveedorDeTokens {

    record Sesion(String token, String jti, Instant expiraEn) {
    }

    record RegistroPendiente(String googleSub, String email, String nombre, String apellido) {
    }

    record SesionLeida(String jti, Long idUsuario, Instant emitidoEn, Instant expiraEn) {
    }

    Sesion emitirSesion(Usuario usuario);

    Optional<SesionLeida> leerSesion(String token);

    String emitirRegistro(RegistroPendiente registro);

    Optional<RegistroPendiente> leerRegistro(String token);
}
