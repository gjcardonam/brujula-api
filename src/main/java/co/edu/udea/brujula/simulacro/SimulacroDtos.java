package co.edu.udea.brujula.simulacro;

import co.edu.udea.brujula.ejercicio.EjercicioDtos.EjercicioEstudiante;
import co.edu.udea.brujula.practica.IntentoDtos.IntentoDetalle;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class SimulacroDtos {
    private SimulacroDtos() {}

    public record CrearRequest(Long idDuracion) {}

    /** Estado del simulacro para el temporizador y el progreso (HU-013 CA-05, HU-014 CA-02). */
    public record Estado(Long id, String estado, int duracionMinutos, Instant fechaInicio, Instant finPrevisto,
                         Instant ahora, long tiempoRestanteSeg, long respondidos, long correctas) {}

    /** Respuesta de "siguiente ejercicio" durante el simulacro (HU-014 CA-01, HU-015 CA-01/CA-03). */
    public record Siguiente(boolean finalizado, String motivo, EjercicioEstudiante ejercicio, Estado estado) {}

    public record DesempenoArea(Long id, String nombre, long intentos, long correctas, BigDecimal porcentaje) {}

    public record Recomendacion(int orden, String area, String tipo, Long idComponente, Long idCompetencia,
                                BigDecimal porcentaje, String mensaje) {}

    /** Resultado completo (HU-017, HU-018). */
    public record Resultado(Long id, Instant fechaInicio, Instant fechaFin, int duracionMinutos, Integer tiempoUtilizadoSeg,
                            long respondidos, long correctas, long incorrectas, BigDecimal porcentajeAciertos,
                            List<DesempenoArea> porComponente, List<DesempenoArea> porCompetencia,
                            List<Recomendacion> recomendaciones, String mensajeRecomendacion, List<IntentoDetalle> detalle) {}

    /** Fila del historial de simulacros (HU-026 CA-02). */
    public record Resumen(Long id, Instant fechaInicio, Instant fechaFin, int duracionMinutos, Integer tiempoUtilizadoSeg,
                          long respondidos, long correctas, long incorrectas, BigDecimal porcentajeAciertos) {}
}
