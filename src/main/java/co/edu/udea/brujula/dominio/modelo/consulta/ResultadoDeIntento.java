package co.edu.udea.brujula.dominio.modelo.consulta;

import java.time.Instant;

/**
 * Lo que ve el estudiante justo después de confirmar. No incluye el tipo de error: esa clasificación
 * es interna y solo se muestra agregada en las estadísticas (HU-027 CA-07).
 */
public record ResultadoDeIntento(Long idIntento, boolean esCorrecto, Long idOpcionSeleccionada,
                                 Long idOpcionCorrecta, String retroalimentacion, boolean retroalimentacionDisponible,
                                 int nivelConfianza, Instant fechaHora, long numeroIntento, boolean repetido,
                                 Long idSimulacro) {
}
