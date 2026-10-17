package co.edu.udea.brujula.aplicacion.simulacro;

import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.consulta.ResumenDeSimulacro;
import co.edu.udea.brujula.dominio.puerto.entrada.ListarSimulacros;
import co.edu.udea.brujula.dominio.puerto.salida.IntentoRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.Reloj;
import co.edu.udea.brujula.dominio.puerto.salida.SimulacroRepositorio;
import co.edu.udea.brujula.dominio.servicio.CierreDeSimulacros;
import co.edu.udea.brujula.dominio.servicio.Porcentajes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListadoDeSimulacros implements ListarSimulacros {

    private static final int TAMANO_PAGINA = 20;

    private final SimulacroRepositorio simulacros;
    private final IntentoRepositorio intentos;
    private final CierreDeSimulacros cierre;
    private final Reloj reloj;

    public ListadoDeSimulacros(SimulacroRepositorio simulacros, IntentoRepositorio intentos,
                               CierreDeSimulacros cierre, Reloj reloj) {
        this.simulacros = simulacros;
        this.intentos = intentos;
        this.cierre = cierre;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public Pagina<ResumenDeSimulacro> listar(Long idEstudiante, int pagina) {
        cierre.cerrarLosVencidosDe(idEstudiante, reloj.ahora());
        return simulacros.finalizadosDe(idEstudiante, Math.max(0, pagina), TAMANO_PAGINA).mapear(simulacro -> {
            long respondidos = intentos.respondidosEn(simulacro.id());
            long correctas = intentos.correctasEn(simulacro.id());
            return new ResumenDeSimulacro(simulacro.id(), simulacro.inicio(), simulacro.fin(),
                    simulacro.duracion().minutos(), simulacro.tiempoUtilizadoSeg(), respondidos, correctas,
                    respondidos - correctas, Porcentajes.de(correctas, respondidos));
        });
    }
}
