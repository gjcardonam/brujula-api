package co.edu.udea.brujula.aplicacion.simulacro;

import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.modelo.Simulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.EstadoDelSimulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.SiguienteDelSimulacro;
import co.edu.udea.brujula.dominio.puerto.entrada.PresentarSiguienteEjercicio;
import co.edu.udea.brujula.dominio.puerto.salida.EjercicioRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.IntentoRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.Reloj;
import co.edu.udea.brujula.dominio.puerto.salida.SimulacroRepositorio;
import co.edu.udea.brujula.dominio.servicio.CierreDeSimulacros;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class PresentacionDelSiguienteEjercicio implements PresentarSiguienteEjercicio {

    private final SimulacroRepositorio simulacros;
    private final IntentoRepositorio intentos;
    private final EjercicioRepositorio ejercicios;
    private final CierreDeSimulacros cierre;
    private final Reloj reloj;

    public PresentacionDelSiguienteEjercicio(SimulacroRepositorio simulacros, IntentoRepositorio intentos,
                                             EjercicioRepositorio ejercicios, CierreDeSimulacros cierre,
                                             Reloj reloj) {
        this.simulacros = simulacros;
        this.intentos = intentos;
        this.ejercicios = ejercicios;
        this.cierre = cierre;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public SiguienteDelSimulacro presentar(Long idEstudiante, Long idSimulacro) {
        Instant ahora = reloj.ahora();
        Simulacro simulacro = simulacros.deEstudiante(idSimulacro, idEstudiante)
                .orElseThrow(() -> new NoEncontrado("El simulacro no existe."));
        if (!simulacro.enCurso()) {
            return SiguienteDelSimulacro.finalizado(SiguienteDelSimulacro.YA_FINALIZADO, estadoDe(simulacro, ahora));
        }
        if (simulacro.tiempoAgotado(ahora)) {
            Simulacro cerrado = cierre.cerrar(simulacro, ahora);
            return SiguienteDelSimulacro.finalizado(SiguienteDelSimulacro.POR_TIEMPO, estadoDe(cerrado, ahora));
        }
        Optional<Ejercicio> siguiente = ejercicios.siguienteParaSimulacro(simulacro.id()).flatMap(ejercicios::porId);
        if (siguiente.isEmpty()) {
            Simulacro cerrado = cierre.cerrar(simulacro, ahora);
            return SiguienteDelSimulacro.finalizado(SiguienteDelSimulacro.SIN_EJERCICIOS, estadoDe(cerrado, ahora));
        }
        return SiguienteDelSimulacro.con(siguiente.get(), estadoDe(simulacro, ahora));
    }

    private EstadoDelSimulacro estadoDe(Simulacro simulacro, Instant ahora) {
        return EstadoDelSimulacro.de(simulacro, ahora, intentos.respondidosEn(simulacro.id()),
                intentos.correctasEn(simulacro.id()));
    }
}
