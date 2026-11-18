package co.edu.udea.brujula.dominio.servicio;

import co.edu.udea.brujula.dominio.modelo.Intento;
import co.edu.udea.brujula.dominio.modelo.Simulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.DesempenoPorArea;
import co.edu.udea.brujula.dominio.modelo.consulta.Estadisticas;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class CalculadoraDeEstadisticas {

    private static final int NIVEL_MINIMO_DE_CONFIANZA = 1;
    private static final int NIVEL_MAXIMO_DE_CONFIANZA = 5;
    private static final double SEGUNDOS_POR_HORA = 3600.0;

    private CalculadoraDeEstadisticas() {
    }

    public static Estadisticas calcular(List<Intento> intentos, List<Simulacro> simulacrosFinalizados) {
        long total = intentos.size();
        long correctas = intentos.stream().filter(Intento::correcto).count();
        Estadisticas.Resumen resumen = new Estadisticas.Resumen(total, correctas, total - correctas,
                Porcentajes.de(correctas, total), Porcentajes.de(total - correctas, total));

        boolean sinDatos = total == 0 && simulacrosFinalizados.isEmpty();
        return new Estadisticas(sinDatos, sinDatos ? Estadisticas.SIN_DATOS : null, resumen,
                desempeno(CalculadoraDeDesempeno.porComponente(intentos)),
                desempeno(CalculadoraDeDesempeno.porCompetencia(intentos)),
                porConfianza(intentos), resumirSimulacros(intentos, simulacrosFinalizados));
    }

    private static List<Estadisticas.Desempeno> desempeno(List<DesempenoPorArea> areas) {
        return areas.stream().map(area -> {
            long incorrectas = area.intentos() - area.correctas();
            return new Estadisticas.Desempeno(area.id(), area.nombre(), area.intentos(), area.correctas(),
                    incorrectas, area.porcentaje(), Porcentajes.de(incorrectas, area.intentos()));
        }).toList();
    }

    private static List<Estadisticas.PorConfianza> porConfianza(List<Intento> intentos) {
        List<Estadisticas.PorConfianza> niveles = new ArrayList<>();
        for (int nivel = NIVEL_MINIMO_DE_CONFIANZA; nivel <= NIVEL_MAXIMO_DE_CONFIANZA; nivel++) {
            int buscado = nivel;
            List<Intento> delNivel = intentos.stream().filter(i -> i.nivelConfianza() == buscado).toList();
            if (delNivel.isEmpty()) continue;
            long total = delNivel.size();
            long correctas = delNivel.stream().filter(Intento::correcto).count();
            niveles.add(new Estadisticas.PorConfianza(nivel, total, correctas, total - correctas,
                    Porcentajes.de(correctas, total), Porcentajes.de(total - correctas, total)));
        }
        return niveles;
    }

    private static Estadisticas.ResumenDeSimulacros resumirSimulacros(List<Intento> intentos,
                                                                      List<Simulacro> simulacros) {
        if (simulacros.isEmpty()) {
            return new Estadisticas.ResumenDeSimulacros(0, Porcentajes.cero(), Porcentajes.cero(),
                    Porcentajes.cero(), Porcentajes.cero());
        }
        Map<Long, List<Intento>> porSimulacro = intentos.stream()
                .filter(Intento::fueEnSimulacro)
                .collect(Collectors.groupingBy(Intento::idSimulacro));

        double sumaDePorcentajes = 0;
        int conRespuestas = 0;
        long ejercicios = 0;
        long segundos = 0;
        for (Simulacro simulacro : simulacros) {
            List<Intento> respondidos = porSimulacro.getOrDefault(simulacro.id(), List.of());
            if (!respondidos.isEmpty()) {
                long correctas = respondidos.stream().filter(Intento::correcto).count();
                sumaDePorcentajes += correctas * 100.0 / respondidos.size();
                conRespuestas++;
            }
            ejercicios += respondidos.size();
            segundos += simulacro.tiempoUtilizadoSeg() == null ? 0 : simulacro.tiempoUtilizadoSeg();
        }
        int cantidad = simulacros.size();
        return new Estadisticas.ResumenDeSimulacros(cantidad,
                conRespuestas == 0 ? Porcentajes.cero() : Porcentajes.redondear(sumaDePorcentajes / conRespuestas),
                Porcentajes.redondear((double) ejercicios / cantidad),
                Porcentajes.redondear((double) segundos / cantidad),
                segundos == 0 ? Porcentajes.cero()
                        : Porcentajes.redondear(ejercicios / (segundos / SEGUNDOS_POR_HORA)));
    }
}
