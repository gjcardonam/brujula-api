package co.edu.udea.brujula.dominio.modelo.consulta;

import co.edu.udea.brujula.dominio.modelo.Recomendacion;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ResultadoDeSimulacro(Long id, Instant fechaInicio, Instant fechaFin, int duracionMinutos,
                                   Integer tiempoUtilizadoSeg, long respondidos, long correctas, long incorrectas,
                                   BigDecimal porcentajeAciertos, List<DesempenoPorArea> porComponente,
                                   List<DesempenoPorArea> porCompetencia, List<Recomendacion> recomendaciones,
                                   String mensajeRecomendacion, List<DetalleDeIntento> detalle) {
}
