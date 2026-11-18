package co.edu.udea.brujula.aplicacion.simulacro;

import co.edu.udea.brujula.dominio.modelo.Simulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.EstadoDelSimulacro;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarSimulacroEnCurso;
import co.edu.udea.brujula.dominio.puerto.salida.IntentoRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.Reloj;
import co.edu.udea.brujula.dominio.puerto.salida.SimulacroRepositorio;
import co.edu.udea.brujula.dominio.servicio.CierreDeSimulacros;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class ConsultaDelSimulacroEnCurso implements ConsultarSimulacroEnCurso {

    private final SimulacroRepositorio simulacros;
    private final IntentoRepositorio intentos;
    private final CierreDeSimulacros cierre;
    private final Reloj reloj;

    public ConsultaDelSimulacroEnCurso(SimulacroRepositorio simulacros, IntentoRepositorio intentos,
                                       CierreDeSimulacros cierre, Reloj reloj) {
        this.simulacros = simulacros;
        this.intentos = intentos;
        this.cierre = cierre;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public Optional<EstadoDelSimulacro> consultar(Long idEstudiante) {
        Instant ahora = reloj.ahora();
        for (Simulacro abierto : simulacros.enCursoDe(idEstudiante)) {
            Simulacro simulacro = cierre.cerrarSiSeVencio(abierto, ahora);
            if (simulacro.enCurso()) {
                return Optional.of(EstadoDelSimulacro.de(simulacro, ahora, intentos.respondidosEn(simulacro.id()),
                        intentos.correctasEn(simulacro.id())));
            }
        }
        return Optional.empty();
    }
}
