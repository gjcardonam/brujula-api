package co.edu.udea.brujula.estadistica;

import co.edu.udea.brujula.catalogo.CatalogoController.Item;
import co.edu.udea.brujula.catalogo.TipoError;
import co.edu.udea.brujula.common.Porcentajes;
import co.edu.udea.brujula.estadistica.EstadisticaDtos.*;
import co.edu.udea.brujula.practica.Intento;
import co.edu.udea.brujula.practica.IntentoRepository;
import co.edu.udea.brujula.simulacro.Simulacro;
import co.edu.udea.brujula.simulacro.SimulacroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;

/** HU-019 (estadísticas personales) y HU-029 (distribución de tipos de error). */
@Service
public class EstadisticaService {

    private static final Map<String, String> MENSAJES = Map.of(
            TipoError.COGNITIVO, "Tu error predominante es cognitivo. Te recomendamos repasar los conceptos de los componentes con menor desempeño antes de seguir practicando.",
            TipoError.HABITO, "Tu error predominante es de hábito. Te recomendamos leer cada enunciado con calma y revisar el procedimiento antes de confirmar la respuesta.",
            TipoError.ANSIEDAD, "Tu error predominante es de ansiedad. Te recomendamos practicar bajo presión de tiempo con simulacros para ganar seguridad.");

    private final IntentoRepository intentos;
    private final SimulacroRepository simulacros;

    public EstadisticaService(IntentoRepository intentos, SimulacroRepository simulacros) {
        this.intentos = intentos;
        this.simulacros = simulacros;
    }

    @Transactional(readOnly = true)
    public Estadisticas mias(Long idUsuario, Long idComponenteFiltro) {
        List<Intento> todos = intentos.findByUsuario_Id(idUsuario);                                       // CA-10/CA-12: solo los propios
        List<Simulacro> finalizados = simulacros.findByUsuario_IdAndEstadoOrderByFechaInicioDesc(idUsuario, Simulacro.FINALIZADO);

        long total = todos.size();
        long correctas = todos.stream().filter(Intento::isEsCorrecto).count();
        Resumen resumen = new Resumen(total, correctas, total - correctas, Porcentajes.de(correctas, total), Porcentajes.de(total - correctas, total));

        List<PorArea> porComponente = porArea(todos, i -> i.getEjercicio().getComponente().getId(), i -> i.getEjercicio().getComponente().getNombre());
        List<PorArea> porCompetencia = porArea(todos, i -> i.getEjercicio().getCompetencia().getId(), i -> i.getEjercicio().getCompetencia().getNombre());

        List<PorConfianza> porConfianza = new ArrayList<>();
        for (int nivel = 1; nivel <= 5; nivel++) {
            final int n = nivel;
            List<Intento> del = todos.stream().filter(i -> i.getNivelConfianza() == n).toList();
            if (del.isEmpty()) continue;                                                                   // CA-05: solo niveles usados
            long c = del.stream().filter(Intento::isEsCorrecto).count();
            porConfianza.add(new PorConfianza(nivel, del.size(), c, del.size() - c, Porcentajes.de(c, del.size()), Porcentajes.de(del.size() - c, del.size())));
        }

        Simulacros sims = resumenSimulacros(finalizados);
        Errores errores = errores(todos, idComponenteFiltro);
        List<Item> componentes = porComponente.stream().map(a -> new Item(a.id(), a.nombre())).toList();
        boolean sinDatos = total == 0 && finalizados.isEmpty();                                             // CA-09
        return new Estadisticas(sinDatos, resumen, porComponente, porCompetencia, porConfianza, sims, errores, componentes);
    }

    private List<PorArea> porArea(List<Intento> lista, Function<Intento, Long> id, Function<Intento, String> nombre) {
        Map<Long, long[]> acc = new TreeMap<>();
        Map<Long, String> nombres = new HashMap<>();
        for (Intento i : lista) {
            Long k = id.apply(i);
            nombres.putIfAbsent(k, nombre.apply(i));
            long[] a = acc.computeIfAbsent(k, x -> new long[2]);
            a[0]++;
            if (i.isEsCorrecto()) a[1]++;
        }
        return acc.entrySet().stream().map(e -> {
            long t = e.getValue()[0], c = e.getValue()[1];
            return new PorArea(e.getKey(), nombres.get(e.getKey()), t, c, t - c, Porcentajes.de(c, t), Porcentajes.de(t - c, t));
        }).toList();                                                                                         // CA-07: sin intentos no aparece
    }

    /** CA-06: cantidad, % promedio de aciertos, ejercicios promedio, tiempo promedio y ejercicios por hora. */
    private Simulacros resumenSimulacros(List<Simulacro> finalizados) {
        if (finalizados.isEmpty()) return new Simulacros(0, cero(), cero(), cero(), cero());
        double sumaPct = 0, sumaEj = 0, sumaSeg = 0;
        int conRespuestas = 0;
        for (Simulacro s : finalizados) {
            long t = intentos.countBySimulacro_Id(s.getId());
            long c = t == 0 ? 0 : intentos.countBySimulacro_IdAndEsCorrectoTrue(s.getId());
            if (t > 0) {
                sumaPct += c * 100.0 / t;
                conRespuestas++;
            }
            sumaEj += t;
            sumaSeg += s.getTiempoUtilizadoSeg() == null ? 0 : s.getTiempoUtilizadoSeg();
        }
        int n = finalizados.size();
        BigDecimal pctProm = conRespuestas == 0 ? cero() : Porcentajes.redondear(sumaPct / conRespuestas);
        BigDecimal ejProm = Porcentajes.redondear(sumaEj / n);
        BigDecimal segProm = Porcentajes.redondear(sumaSeg / n);
        BigDecimal porHora = sumaSeg <= 0 ? cero() : Porcentajes.redondear(sumaEj / (sumaSeg / 3600.0));
        return new Simulacros(n, pctProm, ejProm, segProm, porHora);
    }

    /** HU-029: distribución de tipos de error sobre los intentos incorrectos, filtrable por componente. */
    private Errores errores(List<Intento> todos, Long idComponente) {
        List<Intento> incorrectos = todos.stream()
                .filter(i -> !i.isEsCorrecto() && i.getTipoError() != null)
                .filter(i -> idComponente == null || i.getEjercicio().getComponente().getId().equals(idComponente))
                .toList();
        long total = incorrectos.size();
        Map<String, Long> conteo = new LinkedHashMap<>();
        for (String t : List.of(TipoError.COGNITIVO, TipoError.HABITO, TipoError.ANSIEDAD)) conteo.put(t, 0L);
        for (Intento i : incorrectos) conteo.merge(i.getTipoError().getNombre(), 1L, Long::sum);
        List<TipoErrorDist> dist = conteo.entrySet().stream()
                .map(e -> new TipoErrorDist(e.getKey(), e.getValue(), Porcentajes.de(e.getValue(), total))).toList();
        String predominante = null;
        String mensaje = null;
        for (TipoErrorDist d : dist) {
            if (total > 0 && d.porcentaje().compareTo(BigDecimal.valueOf(50)) > 0) {                        // CA-06: más del 50 %
                predominante = d.tipo();
                mensaje = MENSAJES.get(d.tipo());
            }
        }
        return new Errores(total, dist, predominante, mensaje, idComponente);
    }

    private static BigDecimal cero() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
}
