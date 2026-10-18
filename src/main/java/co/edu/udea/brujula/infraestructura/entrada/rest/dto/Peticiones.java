package co.edu.udea.brujula.infraestructura.entrada.rest.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

public final class Peticiones {

    private Peticiones() {
    }

    public record GoogleRequest(@NotBlank String credential) {
    }

    public record RegistroRequest(@NotBlank String registroToken, String nombre, String apellido, String password,
                                  String confirmacionPassword, Boolean aceptoTerminos) {
    }

    public record LoginRequest(String email, String password) {
    }

    public record RecuperarRequest(String email) {
    }

    public record RestablecerRequest(@NotBlank String token, String password, String confirmacionPassword) {
    }

    public record PerfilRequest(String nombre, String apellido) {
    }

    public record CambioPasswordRequest(String passwordActual, String passwordNueva, String confirmacionPassword) {
    }

    public record IntentoRequest(Long idEjercicio, Long idOpcion, Integer nivelConfianza, UUID tokenIdempotencia) {
    }

    public record OpcionRequest(String descripcion, String imagen, Boolean esCorrecta, String retroalimentacion) {
    }

    public record EjercicioRequest(String enunciado, String imagenEnunciado, Long idComponente, Long idCompetencia,
                                   Long idNivelDificultad, List<OpcionRequest> opciones) {
    }
}
