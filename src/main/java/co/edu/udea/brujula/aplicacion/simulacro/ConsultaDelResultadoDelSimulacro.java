package co.edu.udea.brujula.aplicacion.simulacro;

import co.edu.udea.brujula.dominio.excepcion.Conflicto;
import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.modelo.Simulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.ResultadoDeSimulacro;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarResultadoDelSimulacro;
import co.edu.udea.brujula.dominio.puerto.salida.IntentoRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.Reloj;
import co.edu.udea.brujula.dominio.puerto.salida.SimulacroRepositorio;
import co.edu.udea.brujula.dominio.servicio.CalculadoraDeDesempeno;
import co.edu.udea.brujula.dominio.servicio.CierreDeSimulacros;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class ConsultaDelResultadoDelSimulacro implements ConsultarResultadoDelSimulacro {

    private final SimulacroRepositorio simulacros;
    private final IntentoRepositorio intentos;
    private final CierreDeSimulacros cierre;
    private final Reloj reloj;

    public ConsultaDelResultadoDelSimulacro(SimulacroRepositorio simulacros, IntentoRepositorio intentos,
                                            CierreDeSimulacros cierre, Reloj reloj) {
        this.simulacros = simulacros;
        this.intentos = intentos;
        this.cierre = cierre;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public ResultadoDeSimulacro consultar(Long idEstudiante, Long idSimulacro) {
        Simulacro simulacro = simulacros.deEstudiante(idSimulacro, idEstudiante)
                .map(encontrado -> cierre.cerrarSiSeVencio(encontrado, reloj.ahora()))
                .orElseThrow(() -> new NoEncontrado("El simulacro no existe."));
        if (simulacro.enCurso()) {
            throw new Conflicto("SIMULACRO_EN_CURSO", "El simulacro aún está en curso.",
                    Map.of("idSimulacro", simulacro.id()));
        }
        return CalculadoraDeDesempeno.resultadoDe(simulacro, intentos.delSimulacro(simulacro.id()),
                simulacros.recomendacionesDe(simulacro.id()));
    }
}
