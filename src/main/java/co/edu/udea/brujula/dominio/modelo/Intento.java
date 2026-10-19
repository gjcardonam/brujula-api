package co.edu.udea.brujula.dominio.modelo;

import java.time.Instant;
import java.util.UUID;

public record Intento(Long id, Instant respondidoEn, boolean correcto, int nivelConfianza, TipoError tipoError,
                      Long idEstudiante, Ejercicio ejercicio, Opcion opcionSeleccionada, UUID tokenIdempotencia) {

    public static Intento nuevo(Long idEstudiante, Ejercicio ejercicio, Opcion opcion, int nivelConfianza,
                                TipoError tipoError, UUID tokenIdempotencia, Instant ahora) {
        return new Intento(null, ahora, opcion.correcta(), nivelConfianza, tipoError, idEstudiante,
                ejercicio, opcion, tokenIdempotencia);
    }
}
