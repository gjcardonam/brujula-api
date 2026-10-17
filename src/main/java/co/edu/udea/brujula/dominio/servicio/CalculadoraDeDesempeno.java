package co.edu.udea.brujula.dominio.servicio;

import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.modelo.Intento;
import co.edu.udea.brujula.dominio.modelo.Opcion;
import co.edu.udea.brujula.dominio.modelo.Recomendacion;
import co.edu.udea.brujula.dominio.modelo.Simulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.DesempenoPorArea;
import co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeIntento;
import co.edu.udea.brujula.dominio.modelo.consulta.ResultadoDeSimulacro;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public final class CalculadoraDeDesempeno {

    private CalculadoraDeDesempeno() {
    }

    public static List<DesempenoPorArea> porComponente(List<Intento> intentos) {
        return agrupar(intentos, i -> i.ejercicio().componente().id(), i -> i.ejercicio().componente().nombre());
    }

    public static List<DesempenoPorArea> porCompetencia(List<Intento> intentos) {
        return agrupar(intentos, i -> i.ejercicio().competencia().id(), i -> i.ejercicio().competencia().nombre());
    }

    public static ResultadoDeSimulacro resultadoDe(Simulacro simulacro, List<Intento> intentosDelSimulacro,
                                                   List<Recomendacion> recomendaciones) {
        long total = intentosDelSimulacro.size();
        long correctas = intentosDelSimulacro.stream().filter(Intento::correcto).count();
        List<DetalleDeIntento> detalle = intentosDelSimulacro.stream().map(CalculadoraDeDesempeno::detalleDe).toList();
        return new ResultadoDeSimulacro(simulacro.id(), simulacro.inicio(), simulacro.fin(),
                simulacro.duracion().minutos(), simulacro.tiempoUtilizadoSeg(), total, correctas, total - correctas,
                Porcentajes.de(correctas, total), porComponente(intentosDelSimulacro),
                porCompetencia(intentosDelSimulacro), recomendaciones,
                GeneradorDeRecomendaciones.mensajeGeneral(total, recomendaciones), detalle);
    }

    public static DetalleDeIntento detalleDe(Intento intento) {
        Ejercicio ejercicio = intento.ejercicio();
        Opcion elegida = intento.opcionSeleccionada();
        List<DetalleDeIntento.OpcionRevisada> opciones = ejercicio.opciones().stream()
                .sorted(Comparator.comparingInt(Opcion::orden))
                .map(o -> new DetalleDeIntento.OpcionRevisada(o.id(), o.letra(), o.texto(), o.imagen(),
                        o.id().equals(elegida.id()), o.correcta()))
                .toList();
        return new DetalleDeIntento(intento.id(), ejercicio.id(), ejercicio.numero(), ejercicio.enunciado(),
                ejercicio.imagen(), ejercicio.componente().nombre(), ejercicio.competencia().nombre(),
                ejercicio.nivel().nombre(), ejercicio.estado(), opciones, intento.correcto(),
                intento.nivelConfianza(), intento.respondidoEn(), elegida.retroalimentacionParaMostrar(),
                intento.idSimulacro());
    }

    private static List<DesempenoPorArea> agrupar(List<Intento> intentos, Function<Intento, Long> clave,
                                                  Function<Intento, String> nombre) {
        Map<Long, String> nombres = new TreeMap<>();
        Map<Long, Long> totales = new TreeMap<>();
        Map<Long, Long> correctas = new TreeMap<>();
        for (Intento intento : intentos) {
            Long id = clave.apply(intento);
            nombres.putIfAbsent(id, nombre.apply(intento));
            totales.merge(id, 1L, Long::sum);
            correctas.merge(id, intento.correcto() ? 1L : 0L, Long::sum);
        }
        return totales.keySet().stream()
                .map(id -> new DesempenoPorArea(id, nombres.get(id), totales.get(id), correctas.get(id),
                        Porcentajes.de(correctas.get(id), totales.get(id))))
                .toList();
    }
}
