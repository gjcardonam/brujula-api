package co.edu.udea.brujula.aplicacion.simulacro;

import co.edu.udea.brujula.dominio.excepcion.Conflicto;
import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.modelo.DuracionSimulacro;
import co.edu.udea.brujula.dominio.modelo.Simulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.EstadoDelSimulacro;
import co.edu.udea.brujula.dominio.puerto.entrada.IniciarSimulacro;
import co.edu.udea.brujula.dominio.puerto.salida.CatalogoRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.EjercicioRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.Reloj;
import co.edu.udea.brujula.dominio.puerto.salida.SimulacroRepositorio;
import co.edu.udea.brujula.dominio.servicio.CierreDeSimulacros;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class InicioDeSimulacro implements IniciarSimulacro {

    private final SimulacroRepositorio simulacros;
    private final EjercicioRepositorio ejercicios;
    private final CatalogoRepositorio catalogos;
    private final CierreDeSimulacros cierre;
    private final Reloj reloj;

    public InicioDeSimulacro(SimulacroRepositorio simulacros, EjercicioRepositorio ejercicios,
                             CatalogoRepositorio catalogos, CierreDeSimulacros cierre, Reloj reloj) {
        this.simulacros = simulacros;
        this.ejercicios = ejercicios;
        this.catalogos = catalogos;
        this.cierre = cierre;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public EstadoDelSimulacro iniciar(Long idEstudiante, Long idDuracion) {
        if (idDuracion == null) {
            throw new DatosInvalidos(List.of("Debes seleccionar la duración del simulacro."));
        }
        DuracionSimulacro duracion = catalogos.duracion(idDuracion)
                .orElseThrow(() -> new DatosInvalidos("DURACION_INVALIDA",
                        "La duración seleccionada no está disponible."));

        Instant ahora = reloj.ahora();
        for (Simulacro abierto : simulacros.enCursoDe(idEstudiante)) {
            if (cierre.cerrarSiSeVencio(abierto, ahora).enCurso()) {
                throw new Conflicto("SIMULACRO_EN_CURSO",
                        "Ya tienes un simulacro en curso. Continúalo o finalízalo antes de iniciar otro.",
                        Map.of("idSimulacro", abierto.id()));
            }
        }
        if (ejercicios.cantidadDeActivos() == 0) {
            throw new Conflicto("SIN_EJERCICIOS", "Aún no hay ejercicios disponibles para hacer un simulacro.");
        }
        Simulacro nuevo = simulacros.guardar(Simulacro.iniciar(idEstudiante, duracion, ahora));
        return EstadoDelSimulacro.de(nuevo, ahora, 0, 0);
    }
}
