package co.edu.udea.brujula.dominio.servicio;

import co.edu.udea.brujula.dominio.modelo.Intento;
import co.edu.udea.brujula.dominio.modelo.Recomendacion;
import co.edu.udea.brujula.dominio.modelo.consulta.DesempenoPorArea;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Al cerrar un simulacro se revisa en qué componentes y competencias quedó por debajo del umbral y
 * se arma una recomendación por cada uno, de la más floja a la menos floja (HU-018).
 */
public final class GeneradorDeRecomendaciones {

    public static final String BUEN_DESEMPENO =
            "Tienes un buen desempeño general. Sigue practicando para mantenerlo.";
    public static final String SIN_RESPUESTAS =
            "No hay resultados suficientes: el simulacro finalizó sin respuestas registradas.";

    private GeneradorDeRecomendaciones() {
    }

    public static List<Recomendacion> generar(List<Intento> intentosDelSimulacro, int umbralPct) {
        if (intentosDelSimulacro.isEmpty()) return List.of();

        record AreaFloja(String tipo, DesempenoPorArea desempeno) {
        }
        BigDecimal umbral = BigDecimal.valueOf(umbralPct);
        List<AreaFloja> flojas = new ArrayList<>();
        CalculadoraDeDesempeno.porComponente(intentosDelSimulacro).stream()
                .filter(d -> d.porcentaje().compareTo(umbral) < 0)
                .forEach(d -> flojas.add(new AreaFloja(Recomendacion.COMPONENTE, d)));
        CalculadoraDeDesempeno.porCompetencia(intentosDelSimulacro).stream()
                .filter(d -> d.porcentaje().compareTo(umbral) < 0)
                .forEach(d -> flojas.add(new AreaFloja(Recomendacion.COMPETENCIA, d)));
        flojas.sort(Comparator.comparing((AreaFloja a) -> a.desempeno().porcentaje())
                .thenComparing(a -> a.desempeno().nombre()));

        List<Recomendacion> recomendaciones = new ArrayList<>();
        int orden = 1;
        for (AreaFloja area : flojas) {
            boolean esComponente = Recomendacion.COMPONENTE.equals(area.tipo());
            recomendaciones.add(new Recomendacion(null, orden++, area.desempeno().nombre(), area.tipo(),
                    esComponente ? area.desempeno().id() : null,
                    esComponente ? null : area.desempeno().id(),
                    area.desempeno().porcentaje(),
                    "Te recomendamos practicar ejercicios de " + area.desempeno().nombre() + "."));
        }
        return recomendaciones;
    }
}
