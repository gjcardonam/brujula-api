package co.edu.udea.brujula.estadistica;

import java.math.BigDecimal;
import java.util.List;

public final class EstadisticaDtos {
    private EstadisticaDtos() {}

    public record Resumen(long intentados, long correctas, long incorrectas, BigDecimal porcentajeAciertos, BigDecimal porcentajeDesaciertos) {}

    public record PorArea(Long id, String nombre, long intentados, long correctas, long incorrectas,
                          BigDecimal porcentajeAciertos, BigDecimal porcentajeDesaciertos) {}

    public record PorConfianza(int nivel, long intentados, long correctas, long incorrectas,
                               BigDecimal porcentajeCorrectas, BigDecimal porcentajeIncorrectas) {}

    public record Simulacros(long cantidad, BigDecimal porcentajePromedioAciertos, BigDecimal promedioEjercicios,
                             BigDecimal tiempoPromedioSeg, BigDecimal ejerciciosPorHora) {}

    public record TipoErrorDist(String tipo, long cantidad, BigDecimal porcentaje) {}

    public record Errores(long totalIncorrectos, List<TipoErrorDist> distribucion, String tipoPredominante,
                          String mensajeOrientador, Long idComponenteFiltro) {}

    public record Estadisticas(boolean sinDatos, Resumen resumen, List<PorArea> porComponente, List<PorArea> porCompetencia,
                               List<PorConfianza> porConfianza, Simulacros simulacros, Errores errores,
                               List<co.edu.udea.brujula.catalogo.CatalogoController.Item> componentesConIntentos) {}
}
