package co.edu.udea.brujula.practica;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class IntentoDtos {
    private IntentoDtos() {}

    public record IntentoRequest(Long idEjercicio, Long idOpcion, Integer nivelConfianza, UUID tokenIdempotencia, Long idSimulacro) {}

    /** Resultado inmediato (HU-010 CA-04, HU-011). El tipo de error no se expone aquí (HU-027 CA-07). */
    public record ResultadoIntento(Long idIntento, boolean esCorrecto, Long idOpcionSeleccionada, Long idOpcionCorrecta,
                                   String retroalimentacion, boolean retroalimentacionDisponible, int nivelConfianza,
                                   Instant fechaHora, long numeroIntento, boolean repetido, Long idSimulacro) {}

    /** Fila del historial (HU-025 CA-02). */
    public record IntentoResumen(Long id, Long idEjercicio, Integer numeroEjercicio, String componente, String competencia,
                                 String nivel, boolean esCorrecto, Instant fechaHora, String estadoEjercicio, boolean enSimulacro) {}

    public record OpcionHistorial(Long id, String letra, String descripcion, String imagen, boolean seleccionada, boolean esCorrecta) {}

    /** Detalle de un intento (HU-025 CA-07). */
    public record IntentoDetalle(Long id, Long idEjercicio, Integer numeroEjercicio, String enunciado, String imagenEnunciado,
                                 String componente, String competencia, String nivel, String estadoEjercicio,
                                 List<OpcionHistorial> opciones, boolean esCorrecto, int nivelConfianza, Instant fechaHora,
                                 String retroalimentacion, Long idSimulacro) {}
}
