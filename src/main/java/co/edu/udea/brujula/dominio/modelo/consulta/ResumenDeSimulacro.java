package co.edu.udea.brujula.dominio.modelo.consulta;

import java.math.BigDecimal;
import java.time.Instant;

/** Fila del historial de simulacros (HU-026 CA-02). */
public record ResumenDeSimulacro(Long id, Instant fechaInicio, Instant fechaFin, int duracionMinutos,
                                 Integer tiempoUtilizadoSeg, long respondidos, long correctas, long incorrectas,
                                 BigDecimal porcentajeAciertos) {
}
