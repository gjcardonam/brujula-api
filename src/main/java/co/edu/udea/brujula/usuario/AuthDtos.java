package co.edu.udea.brujula.usuario;

import jakarta.validation.constraints.NotBlank;

public final class AuthDtos {
    private AuthDtos() {}

    public record GoogleRequest(@NotBlank String credential) {}

    /** Respuesta a /auth/google cuando el correo aún no tiene cuenta (HU-001 CA-04). */
    public record GoogleRegistroPendiente(String registroToken, String email, String nombre, String apellido, boolean simulado) {}

    public record RegistroRequest(@NotBlank String registroToken, String nombre, String apellido,
                                  String password, String confirmacionPassword, Boolean aceptoTerminos) {}

    public record LoginRequest(String email, String password) {}

    public record UsuarioDto(Long id, String nombre, String apellido, String email, String rol) {
        public static UsuarioDto de(Usuario u) {
            return new UsuarioDto(u.getId(), u.getNombre(), u.getApellido(), u.getEmail(), u.getRol().getNombreRol());
        }
    }

    public record SesionResponse(String token, java.time.Instant expiraEn, UsuarioDto usuario) {}

    public record RecuperarRequest(String email) {}

    public record RestablecerRequest(@NotBlank String token, String password, String confirmacionPassword) {}

    public record PerfilRequest(String nombre, String apellido) {}

    public record CambioPasswordRequest(String passwordActual, String passwordNueva, String confirmacionPassword) {}

    public record Mensaje(String mensaje) {}
}
