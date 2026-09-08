package co.edu.udea.brujula.dominio.servicio;

import co.edu.udea.brujula.dominio.modelo.Intento;
import co.edu.udea.brujula.dominio.modelo.consulta.DesempenoPorArea;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** Agrupa intentos por componente o por competencia y saca el porcentaje de aciertos de cada uno. */
public final class CalculadoraDeDesempeno {

    private CalculadoraDeDesempeno() {
    }

    public static List<DesempenoPorArea> porComponente(List<Intento> intentos) {
        return agrupar(intentos, i -> i.ejercicio().componente().id(), i -> i.ejercicio().componente().nombre());
    }

    public static List<DesempenoPorArea> porCompetencia(List<Intento> intentos) {
        return agrupar(intentos, i -> i.ejercicio().competencia().id(), i -> i.ejercicio().competencia().nombre());
    }

    private static List<DesempenoPorArea> agrupar(List<Intento> intentos, Function<Intento, Long> clave,
                                                  Function<Intento, String> nombre) {
        Map<Long, long[]> conteo = new LinkedHashMap<>();   // [0] total, [1] correctas
        Map<Long, String> nombres = new HashMap<>();
        for (Intento i : intentos) {
            Long k = clave.apply(i);
            nombres.putIfAbsent(k, nombre.apply(i));
            long[] valores = conteo.computeIfAbsent(k, x -> new long[2]);
            valores[0]++;
            if (i.correcto()) valores[1]++;
        }
        return conteo.entrySet().stream()
                .map(e -> new DesempenoPorArea(e.getKey(), nombres.get(e.getKey()), e.getValue()[0], e.getValue()[1],
                        Porcentajes.de(e.getValue()[1], e.getValue()[0])))
                .sorted(Comparator.comparing(DesempenoPorArea::id))
                .toList();
    }
}
