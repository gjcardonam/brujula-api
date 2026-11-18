package co.edu.udea.brujula.apoyo.dobles;

import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.Recomendacion;
import co.edu.udea.brujula.dominio.modelo.Simulacro;
import co.edu.udea.brujula.dominio.puerto.salida.SimulacroRepositorio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SimulacrosEnMemoria implements SimulacroRepositorio {

    private final Map<Long, Simulacro> porId = new LinkedHashMap<>();
    private final Map<Long, List<Recomendacion>> recomendaciones = new LinkedHashMap<>();
    private long siguienteId = 1;

    public Simulacro guardado(Long id) {
        return copia(porId.get(id));
    }

    public List<Simulacro> todos() {
        return porId.values().stream().map(SimulacrosEnMemoria::copia).toList();
    }

    @Override
    public Simulacro guardar(Simulacro simulacro) {
        simulacro.asignarId(siguienteId++);
        porId.put(simulacro.id(), copia(simulacro));
        return simulacro;
    }

    @Override
    public boolean registrarFinalizacion(Simulacro simulacro) {
        Simulacro actual = porId.get(simulacro.id());
        if (actual == null || !actual.enCurso()) return false;
        porId.put(simulacro.id(), copia(simulacro));
        return true;
    }

    @Override
    public Optional<Simulacro> deEstudiante(Long idSimulacro, Long idEstudiante) {
        return Optional.ofNullable(porId.get(idSimulacro))
                .filter(simulacro -> simulacro.idEstudiante().equals(idEstudiante))
                .map(SimulacrosEnMemoria::copia);
    }

    @Override
    public Optional<Simulacro> deEstudianteParaResponder(Long idSimulacro, Long idEstudiante) {
        return deEstudiante(idSimulacro, idEstudiante);
    }

    @Override
    public List<Simulacro> enCursoDe(Long idEstudiante) {
        return delEstudiante(idEstudiante).stream().filter(Simulacro::enCurso).toList();
    }

    @Override
    public List<Simulacro> finalizadosDe(Long idEstudiante) {
        return delEstudiante(idEstudiante).stream().filter(simulacro -> !simulacro.enCurso())
                .sorted(Comparator.comparing(Simulacro::inicio).reversed())
                .toList();
    }

    @Override
    public Pagina<Simulacro> finalizadosDe(Long idEstudiante, int pagina, int tamano) {
        List<Simulacro> encontrados = finalizadosDe(idEstudiante);
        int desde = Math.min(pagina * tamano, encontrados.size());
        int hasta = Math.min(desde + tamano, encontrados.size());
        return Pagina.de(new ArrayList<>(encontrados.subList(desde, hasta)), pagina, tamano, encontrados.size());
    }

    @Override
    public void guardarRecomendaciones(Long idSimulacro, List<Recomendacion> nuevas) {
        recomendaciones.computeIfAbsent(idSimulacro, id -> new ArrayList<>()).addAll(nuevas);
    }

    @Override
    public List<Recomendacion> recomendacionesDe(Long idSimulacro) {
        return List.copyOf(recomendaciones.getOrDefault(idSimulacro, List.of()));
    }

    private List<Simulacro> delEstudiante(Long idEstudiante) {
        return porId.values().stream()
                .filter(simulacro -> simulacro.idEstudiante().equals(idEstudiante))
                .map(SimulacrosEnMemoria::copia)
                .toList();
    }

    private static Simulacro copia(Simulacro simulacro) {
        if (simulacro == null) return null;
        return new Simulacro(simulacro.id(), simulacro.idEstudiante(), simulacro.duracion(), simulacro.inicio(),
                simulacro.fin(), simulacro.estado(), simulacro.tiempoUtilizadoSeg());
    }
}
