package co.edu.udea.brujula.dominio.modelo.consulta;

import java.math.BigDecimal;
import java.util.List;

/** Todo lo que muestra "Mis estadísticas" (HU-019) más la distribución de errores (HU-029). */
public record Estadisticas(boolean sinDatos, Resumen resumen, List<Desempeno> porComponente,
                           List<Desempeno> porCompetencia, List<PorConfianza> porConfianza,
                           ResumenDeSimulacros simulacros, Errores errores, List<Area> componentesConIntentos) {

    public record Resumen(long intentados, long correctas, long incorrectas,
                          BigDecimal porcentajeAciertos, BigDecimal porcentajeDesaciertos) {
    }

    public record Desempeno(Long id, String nombre, long intentados, long correctas, long incorrectas,
                            BigDecimal porcentajeAciertos, BigDecimal porcentajeDesaciertos) {
    }

    public record PorConfianza(int nivel, long intentados, long correctas, long incorrectas,
                               BigDecimal porcentajeCorrectas, BigDecimal porcentajeIncorrectas) {
    }

    public record ResumenDeSimulacros(long cantidad, BigDecimal porcentajePromedioAciertos,
                                      BigDecimal promedioEjercicios, BigDecimal tiempoPromedioSeg,
                                      BigDecimal ejerciciosPorHora) {
    }

    public record Errores(long totalIncorrectos, List<DistribucionDeError> distribucion, String tipoPredominante,
                          String mensajeOrientador, Long idComponenteFiltro) {
    }

    public record Area(Long id, String nombre) {
    }
}
