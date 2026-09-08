package co.edu.udea.brujula.apoyo.dobles;

import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.Recomendacion;
import co.edu.udea.brujula.dominio.modelo.Simulacro;
import co.edu.udea.brujula.dominio.puerto.salida.SimulacroRepositorio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SimulacrosEnMemoria implements SimulacroRepositorio {

    private final Map<Long, Simulacro> porId = new LinkedHashMap<>();
    private final Map<Long, List<Recomendacion>> recomendaciones = new LinkedHashMap<>();
    private long siguienteId = 1;

    @Override
    public Simulacro guardar(Simulacro simulacro) {
        if (simulacro.id() == null) simulacro.asignarId(siguienteId++);
        porId.put(simulacro.id(), simulacro);
        return simulacro;
    }

    @Override
    public Optional<Simulacro> deEstudiante(Long idSimulacro, Long idEstudiante) {
        return Optional.ofNullable(porId.get(idSimulacro))
                .filter(s -> s.idEstudiante().equals(idEstudiante));
    }

    @Override
    public List<Simulacro> enCursoDe(Long idEstudiante) {
        return porId.values().stream()
                .filter(s -> s.idEstudiante().equals(idEstudiante) && s.enCurso())
                .toList();
    }

    @Override
    public List<Simulacro> finalizadosDe(Long idEstudiante) {
        return porId.values().stream()
                .filter(s -> s.idEstudiante().equals(idEstudiante) && !s.enCurso())
                .toList();
    }

    @Override
    public Pagina<Simulacro> finalizadosDe(Long idEstudiante, int pagina, int tamano) {
        List<Simulacro> encontrados = finalizadosDe(idEstudiante);
        return Pagina.de(encontrados, pagina, tamano, encontrados.size());
    }

    @Override
    public void guardarRecomendaciones(Long idSimulacro, List<Recomendacion> nuevas) {
        recomendaciones.computeIfAbsent(idSimulacro, k -> new ArrayList<>()).addAll(nuevas);
    }

    @Override
    public List<Recomendacion> recomendacionesDe(Long idSimulacro) {
        return recomendaciones.getOrDefault(idSimulacro, List.of());
    }
}
