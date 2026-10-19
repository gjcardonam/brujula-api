package co.edu.udea.brujula.dominio.modelo.consulta;

import java.math.BigDecimal;

public record DistribucionDeError(String tipo, long cantidad, BigDecimal porcentaje) {
}
