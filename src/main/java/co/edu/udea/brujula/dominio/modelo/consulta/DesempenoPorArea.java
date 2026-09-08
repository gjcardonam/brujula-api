package co.edu.udea.brujula.dominio.modelo.consulta;

import java.math.BigDecimal;

public record DesempenoPorArea(Long id, String nombre, long intentos, long correctas, BigDecimal porcentaje) {
}
