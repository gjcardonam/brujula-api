package co.edu.udea.brujula.dominio.modelo.consulta;

import java.math.BigDecimal;
import java.util.List;

/** Desempeño histórico de un ejercicio, para la vista de detalle del administrador (HU-022 CA-03). */
public record UsoDelEjercicio(long intentos, long correctos, BigDecimal porcentajeAciertos,
                              List<DistribucionDeError> errores) {
}
