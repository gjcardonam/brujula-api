package co.edu.udea.brujula.aplicacion;

import co.edu.udea.brujula.dominio.modelo.Intento;
import co.edu.udea.brujula.dominio.modelo.Simulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.Estadisticas;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarEstadisticas;
import co.edu.udea.brujula.dominio.puerto.salida.IntentoRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.SimulacroRepositorio;
import co.edu.udea.brujula.dominio.servicio.CalculadoraDeEstadisticas;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** HU-019 y HU-029: reúne los datos y deja los cálculos al servicio de dominio. */
@Service
public class EstadisticasDelEstudiante implements ConsultarEstadisticas {

    private final IntentoRepositorio intentos;
    private final SimulacroRepositorio simulacros;

    public EstadisticasDelEstudiante(IntentoRepositorio intentos, SimulacroRepositorio simulacros) {
        this.intentos = intentos;
        this.simulacros = simulacros;
    }

    @Override
    @Transactional(readOnly = true)
    public Estadisticas deEstudiante(Long idEstudiante, Long idComponenteFiltro) {
        List<Intento> todos = intentos.deEstudiante(idEstudiante);
        List<Simulacro> finalizados = simulacros.finalizadosDe(idEstudiante);

        Map<Long, long[]> resumenPorSimulacro = new HashMap<>();
        for (Simulacro s : finalizados) {
            long respondidos = intentos.respondidosEn(s.id());
            long correctas = respondidos == 0 ? 0 : intentos.correctasEn(s.id());
            resumenPorSimulacro.put(s.id(), new long[]{respondidos, correctas});
        }
        return CalculadoraDeEstadisticas.calcular(todos, finalizados, resumenPorSimulacro, idComponenteFiltro);
    }
}
