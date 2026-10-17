package co.edu.udea.brujula.aplicacion.simulacro;

import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.modelo.Simulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.EstadoDelSimulacro;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarEstadoDelSimulacro;
import co.edu.udea.brujula.dominio.puerto.salida.IntentoRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.Reloj;
import co.edu.udea.brujula.dominio.puerto.salida.SimulacroRepositorio;
import co.edu.udea.brujula.dominio.servicio.CierreDeSimulacros;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ConsultaDelEstadoDelSimulacro implements ConsultarEstadoDelSimulacro {

    private final SimulacroRepositorio simulacros;
    private final IntentoRepositorio intentos;
    private final CierreDeSimulacros cierre;
    private final Reloj reloj;

    public ConsultaDelEstadoDelSimulacro(SimulacroRepositorio simulacros, IntentoRepositorio intentos,
                                         CierreDeSimulacros cierre, Reloj reloj) {
        this.simulacros = simulacros;
        this.intentos = intentos;
        this.cierre = cierre;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public EstadoDelSimulacro consultar(Long idEstudiante, Long idSimulacro) {
        Instant ahora = reloj.ahora();
        Simulacro simulacro = simulacros.deEstudiante(idSimulacro, idEstudiante)
                .map(encontrado -> cierre.cerrarSiSeVencio(encontrado, ahora))
                .orElseThrow(() -> new NoEncontrado("El simulacro no existe."));
        return EstadoDelSimulacro.de(simulacro, ahora, intentos.respondidosEn(simulacro.id()),
                intentos.correctasEn(simulacro.id()));
    }
}
