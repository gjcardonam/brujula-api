package co.edu.udea.brujula.dominio.modelo.consulta;

import java.math.BigDecimal;
import java.util.List;

public record UsoDelEjercicio(long intentos, long correctos, BigDecimal porcentajeAciertos,
                              List<DistribucionDeError> errores) {
}
