package co.edu.udea.brujula.dominio.servicio;

import co.edu.udea.brujula.dominio.modelo.Recomendacion;
import co.edu.udea.brujula.dominio.modelo.Simulacro;
import co.edu.udea.brujula.dominio.puerto.salida.IntentoRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.ParametrosDelSistema;
import co.edu.udea.brujula.dominio.puerto.salida.SimulacroRepositorio;

import java.time.Instant;
import java.util.List;

public class CierreDeSimulacros {

    private static final int UMBRAL_POR_DEFECTO = 60;

    private final SimulacroRepositorio simulacros;
    private final IntentoRepositorio intentos;
    private final ParametrosDelSistema parametros;

    public CierreDeSimulacros(SimulacroRepositorio simulacros, IntentoRepositorio intentos,
                              ParametrosDelSistema parametros) {
        this.simulacros = simulacros;
        this.intentos = intentos;
        this.parametros = parametros;
    }

    public Simulacro cerrarSiSeVencio(Simulacro simulacro, Instant ahora) {
        if (simulacro.enCurso() && simulacro.tiempoAgotado(ahora)) {
            return cerrar(simulacro, ahora);
        }
        return simulacro;
    }

    public void cerrarLosVencidosDe(Long idEstudiante, Instant ahora) {
        simulacros.enCursoDe(idEstudiante).forEach(simulacro -> cerrarSiSeVencio(simulacro, ahora));
    }

    public Simulacro cerrar(Simulacro simulacro, Instant ahora) {
        if (!simulacro.enCurso()) return simulacro;
        simulacro.finalizar(ahora);
        if (!simulacros.registrarFinalizacion(simulacro)) {
            return simulacros.deEstudiante(simulacro.id(), simulacro.idEstudiante()).orElse(simulacro);
        }
        int umbral = parametros.entero(ParametrosDelSistema.UMBRAL_RECOMENDACION_PCT, UMBRAL_POR_DEFECTO);
        List<Recomendacion> recomendaciones =
                GeneradorDeRecomendaciones.generar(intentos.delSimulacro(simulacro.id()), umbral);
        if (!recomendaciones.isEmpty()) {
            simulacros.guardarRecomendaciones(simulacro.id(), recomendaciones);
        }
        return simulacro;
    }
}
