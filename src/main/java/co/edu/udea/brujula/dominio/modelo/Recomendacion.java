package co.edu.udea.brujula.dominio.modelo;

import java.math.BigDecimal;

public record Recomendacion(Long id, int orden, String area, String tipoDeArea, Long idComponente,
                            Long idCompetencia, BigDecimal porcentaje, String mensaje) {

    public static final String COMPONENTE = "Componente";
    public static final String COMPETENCIA = "Competencia";

    public boolean esDeComponente() {
        return COMPONENTE.equals(tipoDeArea);
    }
}
