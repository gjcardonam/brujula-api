package co.edu.udea.brujula.dominio.servicio;

import co.edu.udea.brujula.dominio.modelo.Intento;
import co.edu.udea.brujula.dominio.modelo.Simulacro;
import co.edu.udea.brujula.dominio.modelo.TipoError;
import co.edu.udea.brujula.dominio.modelo.consulta.DistribucionDeError;
import co.edu.udea.brujula.dominio.modelo.consulta.Estadisticas;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

/** Arma la pantalla "Mis estadísticas" (HU-019) y la distribución de tipos de error (HU-029). */
public final class CalculadoraDeEstadisticas {

    private static final Map<String, String> MENSAJE_POR_TIPO = Map.of(
            TipoError.COGNITIVO, "Tu error predominante es cognitivo. Te recomendamos repasar los conceptos de los "
                    + "componentes con menor desempeño antes de seguir practicando.",
            TipoError.HABITO, "Tu error predominante es de hábito. Te recomendamos leer cada enunciado con calma y "
                    + "revisar el procedimiento antes de confirmar la respuesta.",
            TipoError.ANSIEDAD, "Tu error predominante es de ansiedad. Te recomendamos practicar bajo presión de "
                    + "tiempo con simulacros para ganar seguridad.");

    private CalculadoraDeEstadisticas() {
    }

    /**
     * @param resumenPorSimulacro por cada simulacro finalizado: respondidos y correctas
     */
    public static Estadisticas calcular(List<Intento> intentos, List<Simulacro> simulacros,
                                        Map<Long, long[]> resumenPorSimulacro, Long idComponenteFiltro) {
        long total = intentos.size();
        long correctas = intentos.stream().filter(Intento::correcto).count();
        var resumen = new Estadisticas.Resumen(total, correctas, total - correctas,
                Porcentajes.de(correctas, total), Porcentajes.de(total - correctas, total));

        List<Estadisticas.Desempeno> porComponente = desempeno(intentos,
                i -> i.ejercicio().componente().id(), i -> i.ejercicio().componente().nombre());
        List<Estadisticas.Desempeno> porCompetencia = desempeno(intentos,
                i -> i.ejercicio().competencia().id(), i -> i.ejercicio().competencia().nombre());

        List<Estadisticas.PorConfianza> porConfianza = new ArrayList<>();
        for (int nivel = 1; nivel <= 5; nivel++) {
            final int n = nivel;
            List<Intento> delNivel = intentos.stream().filter(i -> i.nivelConfianza() == n).toList();
            if (delNivel.isEmpty()) continue;   // los niveles que nunca usó no se muestran
            long ok = delNivel.stream().filter(Intento::correcto).count();
            porConfianza.add(new Estadisticas.PorConfianza(nivel, delNivel.size(), ok, delNivel.size() - ok,
                    Porcentajes.de(ok, delNivel.size()), Porcentajes.de(delNivel.size() - ok, delNivel.size())));
        }

        var deSimulacros = resumirSimulacros(simulacros, resumenPorSimulacro);
        var errores = distribucionDeErrores(intentos, idComponenteFiltro);
        List<Estadisticas.Area> componentes = porComponente.stream()
                .map(d -> new Estadisticas.Area(d.id(), d.nombre())).toList();

        boolean sinDatos = total == 0 && simulacros.isEmpty();
        return new Estadisticas(sinDatos, resumen, porComponente, porCompetencia, porConfianza,
                deSimulacros, errores, componentes);
    }

    private static List<Estadisticas.Desempeno> desempeno(List<Intento> intentos, Function<Intento, Long> clave,
                                                          Function<Intento, String> nombre) {
        Map<Long, long[]> conteo = new TreeMap<>();
        Map<Long, String> nombres = new HashMap<>();
        for (Intento i : intentos) {
            Long k = clave.apply(i);
            nombres.putIfAbsent(k, nombre.apply(i));
            long[] valores = conteo.computeIfAbsent(k, x -> new long[2]);
            valores[0]++;
            if (i.correcto()) valores[1]++;
        }
        return conteo.entrySet().stream().map(e -> {
            long t = e.getValue()[0];
            long c = e.getValue()[1];
            return new Estadisticas.Desempeno(e.getKey(), nombres.get(e.getKey()), t, c, t - c,
                    Porcentajes.de(c, t), Porcentajes.de(t - c, t));
        }).toList();
    }

    private static Estadisticas.ResumenDeSimulacros resumirSimulacros(List<Simulacro> simulacros,
                                                                      Map<Long, long[]> resumen) {
        if (simulacros.isEmpty()) {
            return new Estadisticas.ResumenDeSimulacros(0, Porcentajes.cero(), Porcentajes.cero(),
                    Porcentajes.cero(), Porcentajes.cero());
        }
        double sumaPorcentajes = 0;
        double sumaEjercicios = 0;
        double sumaSegundos = 0;
        int conRespuestas = 0;
        for (Simulacro s : simulacros) {
            long[] datos = resumen.getOrDefault(s.id(), new long[2]);
            if (datos[0] > 0) {
                sumaPorcentajes += datos[1] * 100.0 / datos[0];
                conRespuestas++;
            }
            sumaEjercicios += datos[0];
            sumaSegundos += s.tiempoUtilizadoSeg() == null ? 0 : s.tiempoUtilizadoSeg();
        }
        int cantidad = simulacros.size();
        return new Estadisticas.ResumenDeSimulacros(cantidad,
                conRespuestas == 0 ? Porcentajes.cero() : Porcentajes.redondear(sumaPorcentajes / conRespuestas),
                Porcentajes.redondear(sumaEjercicios / cantidad),
                Porcentajes.redondear(sumaSegundos / cantidad),
                sumaSegundos <= 0 ? Porcentajes.cero() : Porcentajes.redondear(sumaEjercicios / (sumaSegundos / 3600.0)));
    }

    private static Estadisticas.Errores distribucionDeErrores(List<Intento> intentos, Long idComponente) {
        List<Intento> incorrectos = intentos.stream()
                .filter(i -> !i.correcto() && i.tipoError() != null)
                .filter(i -> idComponente == null || i.ejercicio().componente().id().equals(idComponente))
                .toList();
        long total = incorrectos.size();

        Map<String, Long> conteo = new LinkedHashMap<>();
        for (String tipo : List.of(TipoError.COGNITIVO, TipoError.HABITO, TipoError.ANSIEDAD)) {
            conteo.put(tipo, 0L);
        }
        for (Intento i : incorrectos) {
            conteo.merge(i.tipoError().nombre(), 1L, Long::sum);
        }
        List<DistribucionDeError> distribucion = conteo.entrySet().stream()
                .map(e -> new DistribucionDeError(e.getKey(), e.getValue(), Porcentajes.de(e.getValue(), total)))
                .toList();

        // Solo se orienta al estudiante cuando un tipo de error claramente domina (HU-029 CA-06).
        String predominante = null;
        String mensaje = null;
        for (DistribucionDeError d : distribucion) {
            if (total > 0 && d.porcentaje().compareTo(BigDecimal.valueOf(50)) > 0) {
                predominante = d.tipo();
                mensaje = MENSAJE_POR_TIPO.get(d.tipo());
            }
        }
        return new Estadisticas.Errores(total, distribucion, predominante, mensaje, idComponente);
    }
}
