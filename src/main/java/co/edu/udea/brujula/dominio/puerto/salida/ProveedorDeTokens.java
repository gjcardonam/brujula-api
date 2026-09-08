package co.edu.udea.brujula.dominio.puerto.salida;

import co.edu.udea.brujula.dominio.modelo.Usuario;

import java.time.Instant;
import java.util.Optional;

public interface ProveedorDeTokens {

    /** Token de sesión. El jti permite revocarlo al cerrar sesión (HU-004 CA-06). */
    record Sesion(String token, String jti, Instant expiraEn) {
    }

    /** Datos que Google ya verificó y que viajan firmados entre los dos pasos del registro (HU-001). */
    record RegistroPendiente(String googleSub, String email, String nombre, String apellido) {
    }

    /** Contenido de un token de sesión que llegó firmado y sin vencer. */
    record SesionLeida(String jti, Long idUsuario, Instant emitidoEn, Instant expiraEn) {
    }

    Sesion emitirSesion(Usuario usuario);

    Optional<SesionLeida> leerSesion(String token);

    String emitirRegistro(RegistroPendiente registro);

    Optional<RegistroPendiente> leerRegistro(String token);
}
