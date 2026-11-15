package co.edu.udea.brujula.dominio.modelo;

import java.time.Instant;
import java.util.UUID;

public record Intento(Long id, Instant respondidoEn, boolean correcto, int nivelConfianza, TipoError tipoError,
                      Long idEstudiante, Ejercicio ejercicio, Opcion opcionSeleccionada, UUID tokenIdempotencia,
                      Integer tiempoRespuestaMs) {

    public Intento(Long id, Instant respondidoEn, boolean correcto, int nivelConfianza, TipoError tipoError,
                   Long idEstudiante, Ejercicio ejercicio, Opcion opcionSeleccionada, UUID tokenIdempotencia) {
        this(id, respondidoEn, correcto, nivelConfianza, tipoError, idEstudiante, ejercicio, opcionSeleccionada,
                tokenIdempotencia, null);
    }

    public static Intento nuevo(Long idEstudiante, Ejercicio ejercicio, Opcion opcion, int nivelConfianza,
                                TipoError tipoError, UUID tokenIdempotencia, Instant ahora) {
        return nuevo(idEstudiante, ejercicio, opcion, nivelConfianza, tipoError, tokenIdempotencia, ahora, null);
    }

    public static Intento nuevo(Long idEstudiante, Ejercicio ejercicio, Opcion opcion, int nivelConfianza,
                                TipoError tipoError, UUID tokenIdempotencia, Instant ahora,
                                Integer tiempoRespuestaMs) {
        return new Intento(null, ahora, opcion.correcta(), nivelConfianza, tipoError, idEstudiante,
                ejercicio, opcion, tokenIdempotencia, tiempoRespuestaMs);
    }
}
