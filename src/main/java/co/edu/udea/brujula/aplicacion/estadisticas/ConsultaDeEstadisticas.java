package co.edu.udea.brujula.aplicacion.estadisticas;

import co.edu.udea.brujula.dominio.modelo.consulta.Estadisticas;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarEstadisticas;
import co.edu.udea.brujula.dominio.puerto.salida.IntentoRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.Reloj;
import co.edu.udea.brujula.dominio.puerto.salida.SimulacroRepositorio;
import co.edu.udea.brujula.dominio.servicio.CalculadoraDeEstadisticas;
import co.edu.udea.brujula.dominio.servicio.CierreDeSimulacros;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultaDeEstadisticas implements ConsultarEstadisticas {

    private final IntentoRepositorio intentos;
    private final SimulacroRepositorio simulacros;
    private final CierreDeSimulacros cierre;
    private final Reloj reloj;

    public ConsultaDeEstadisticas(IntentoRepositorio intentos, SimulacroRepositorio simulacros,
                                  CierreDeSimulacros cierre, Reloj reloj) {
        this.intentos = intentos;
        this.simulacros = simulacros;
        this.cierre = cierre;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public Estadisticas consultar(Long idEstudiante) {
        cierre.cerrarLosVencidosDe(idEstudiante, reloj.ahora());
        return CalculadoraDeEstadisticas.calcular(intentos.deEstudiante(idEstudiante),
                simulacros.finalizadosDe(idEstudiante));
    }
}
