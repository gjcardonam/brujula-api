package co.edu.udea.brujula.aplicacion.simulacro;

import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.modelo.Simulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.ResultadoDeSimulacro;
import co.edu.udea.brujula.dominio.puerto.entrada.FinalizarSimulacro;
import co.edu.udea.brujula.dominio.puerto.salida.IntentoRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.Reloj;
import co.edu.udea.brujula.dominio.puerto.salida.SimulacroRepositorio;
import co.edu.udea.brujula.dominio.servicio.CalculadoraDeDesempeno;
import co.edu.udea.brujula.dominio.servicio.CierreDeSimulacros;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinalizacionDeSimulacro implements FinalizarSimulacro {

    private final SimulacroRepositorio simulacros;
    private final IntentoRepositorio intentos;
    private final CierreDeSimulacros cierre;
    private final Reloj reloj;

    public FinalizacionDeSimulacro(SimulacroRepositorio simulacros, IntentoRepositorio intentos,
                                   CierreDeSimulacros cierre, Reloj reloj) {
        this.simulacros = simulacros;
        this.intentos = intentos;
        this.cierre = cierre;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public ResultadoDeSimulacro finalizar(Long idEstudiante, Long idSimulacro) {
        Simulacro simulacro = simulacros.deEstudiante(idSimulacro, idEstudiante)
                .map(encontrado -> cierre.cerrar(encontrado, reloj.ahora()))
                .orElseThrow(() -> new NoEncontrado("El simulacro no existe."));
        return CalculadoraDeDesempeno.resultadoDe(simulacro, intentos.delSimulacro(simulacro.id()),
                simulacros.recomendacionesDe(simulacro.id()));
    }
}
