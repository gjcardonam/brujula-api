package co.edu.udea.brujula.dominio.modelo;

import java.math.BigDecimal;

/** Recomendación de estudio generada al cerrar un simulacro (HU-018). */
public record Recomendacion(Long id, int orden, String area, String tipoDeArea, Long idComponente,
                            Long idCompetencia, BigDecimal porcentaje, String mensaje) {

    public static final String COMPONENTE = "Componente";
    public static final String COMPETENCIA = "Competencia";
}
