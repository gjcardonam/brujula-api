package co.edu.udea.brujula.dominio.modelo;

import java.time.Instant;
import java.util.UUID;

/**
 * Respuesta confirmada por un estudiante. Es inmutable a propósito: una vez registrado no se toca
 * (HU-010 CA-05, HU-012 CA-04), y la clasificación del error se calcula una sola vez (HU-027 CA-09).
 *
 * Según de dónde venga, el ejercicio puede llegar sin sus opciones: para el historial y las
 * estadísticas basta con el componente, la competencia y el nivel.
 */
public record Intento(Long id, Instant fechaHora, boolean correcto, int nivelConfianza, TipoError tipoError,
                      Long idEstudiante, Ejercicio ejercicio, Opcion opcionSeleccionada, Long idSimulacro,
                      UUID tokenIdempotencia) {

    public static Intento nuevo(Long idEstudiante, Ejercicio ejercicio, Opcion opcion, int nivelConfianza,
                                TipoError tipoError, Long idSimulacro, UUID token, Instant ahora) {
        return new Intento(null, ahora, opcion.correcta(), nivelConfianza, tipoError, idEstudiante,
                ejercicio, opcion, idSimulacro, token);
    }

    public boolean fueEnSimulacro() {
        return idSimulacro != null;
    }
}
