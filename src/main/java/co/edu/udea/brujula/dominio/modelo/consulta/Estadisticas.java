package co.edu.udea.brujula.dominio.modelo.consulta;

import java.math.BigDecimal;
import java.util.List;

public record Estadisticas(boolean sinDatos, String mensaje, Resumen resumen, List<Desempeno> porComponente,
                           List<Desempeno> porCompetencia, List<PorConfianza> porConfianza,
                           ResumenDeSimulacros simulacros) {

    public static final String SIN_DATOS = "Aún no has resuelto ejercicios ni simulacros. Cuando practiques, "
            + "aquí verás tu desempeño.";

    public record Resumen(long intentados, long correctas, long incorrectas, BigDecimal porcentajeAciertos,
                          BigDecimal porcentajeDesaciertos) {
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
}
