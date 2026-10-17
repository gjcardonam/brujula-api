package co.edu.udea.brujula.dominio.servicio;

import co.edu.udea.brujula.dominio.modelo.Intento;
import co.edu.udea.brujula.dominio.modelo.Recomendacion;
import co.edu.udea.brujula.dominio.modelo.consulta.DesempenoPorArea;

import java.math.BigDecimal;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class GeneradorDeRecomendaciones {

    public static final String BUEN_DESEMPENO =
            "Tienes un buen desempeño general. Sigue practicando para mantenerlo.";
    public static final String SIN_RESPUESTAS =
            "No hay resultados suficientes: el simulacro finalizó sin respuestas registradas.";

    private static final Collator ORDEN_ALFABETICO = Collator.getInstance(Locale.forLanguageTag("es"));

    private GeneradorDeRecomendaciones() {
    }

    private record AreaPorReforzar(String tipo, DesempenoPorArea desempeno) {
    }

    public static List<Recomendacion> generar(List<Intento> intentosDelSimulacro, int umbralPct) {
        if (intentosDelSimulacro.isEmpty()) return List.of();

        BigDecimal umbral = BigDecimal.valueOf(umbralPct);
        List<AreaPorReforzar> areas = new ArrayList<>();
        CalculadoraDeDesempeno.porComponente(intentosDelSimulacro).stream()
                .filter(d -> d.porcentaje().compareTo(umbral) < 0)
                .forEach(d -> areas.add(new AreaPorReforzar(Recomendacion.COMPONENTE, d)));
        CalculadoraDeDesempeno.porCompetencia(intentosDelSimulacro).stream()
                .filter(d -> d.porcentaje().compareTo(umbral) < 0)
                .forEach(d -> areas.add(new AreaPorReforzar(Recomendacion.COMPETENCIA, d)));
        areas.sort(Comparator.comparing((AreaPorReforzar a) -> a.desempeno().porcentaje())
                .thenComparing(a -> a.desempeno().nombre(), ORDEN_ALFABETICO));

        List<Recomendacion> recomendaciones = new ArrayList<>();
        for (AreaPorReforzar area : areas) {
            boolean esComponente = Recomendacion.COMPONENTE.equals(area.tipo());
            DesempenoPorArea desempeno = area.desempeno();
            recomendaciones.add(new Recomendacion(null, recomendaciones.size() + 1, desempeno.nombre(), area.tipo(),
                    esComponente ? desempeno.id() : null, esComponente ? null : desempeno.id(),
                    desempeno.porcentaje(), "Te recomendamos practicar ejercicios de " + desempeno.nombre() + "."));
        }
        return recomendaciones;
    }

    public static String mensajeGeneral(long respondidos, List<Recomendacion> recomendaciones) {
        if (respondidos == 0) return SIN_RESPUESTAS;
        return recomendaciones.isEmpty() ? BUEN_DESEMPENO : null;
    }
}
