package co.edu.udea.brujula.aplicacion.simulacro;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.apoyo.dobles.IntentosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.ParametrosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.RelojFijo;
import co.edu.udea.brujula.apoyo.dobles.SimulacrosEnMemoria;
import co.edu.udea.brujula.dominio.modelo.Simulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.EstadoDelSimulacro;
import co.edu.udea.brujula.dominio.servicio.CierreDeSimulacros;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Consulta del simulacro en curso")
class ConsultaDelSimulacroEnCursoTest {

    private static final Long ESTUDIANTE = 7L;

    private final SimulacrosEnMemoria simulacros = new SimulacrosEnMemoria();
    private final IntentosEnMemoria intentos = new IntentosEnMemoria();
    private final RelojFijo reloj = new RelojFijo();
    private final ConsultaDelSimulacroEnCurso consulta = new ConsultaDelSimulacroEnCurso(simulacros, intentos,
            new CierreDeSimulacros(simulacros, intentos, new ParametrosEnMemoria()), reloj);

    @Test
    void devuelve_el_simulacro_abierto_con_el_tiempo_que_siguio_corriendo() {
        Simulacro simulacro = simulacros.guardar(Simulacro.iniciar(ESTUDIANTE, Datos.TREINTA_MINUTOS, reloj.ahora()));
        intentos.guardar(Datos.intento(ESTUDIANTE, simulacro.id(), Datos.ALGEBRA, Datos.INTERPRETACION, true, 3));
        reloj.avanzar(Duration.ofMinutes(10));

        EstadoDelSimulacro estado = consulta.consultar(ESTUDIANTE).orElseThrow();

        assertEquals(simulacro.id(), estado.id());
        assertEquals(1200, estado.tiempoRestanteSeg());
        assertEquals(1, estado.respondidos());
        assertEquals(1, estado.correctas());
    }

    @Test
    void al_reconectar_despues_del_tiempo_aplica_la_finalizacion_automatica() {
        Simulacro simulacro = simulacros.guardar(Simulacro.iniciar(ESTUDIANTE, Datos.TREINTA_MINUTOS, reloj.ahora()));
        reloj.avanzar(Duration.ofMinutes(40));

        Optional<EstadoDelSimulacro> estado = consulta.consultar(ESTUDIANTE);

        assertTrue(estado.isEmpty());
        assertFalse(simulacros.guardado(simulacro.id()).enCurso());
        assertEquals(1800, simulacros.guardado(simulacro.id()).tiempoUtilizadoSeg());
    }

    @Test
    void no_muestra_los_simulacros_de_otros_estudiantes() {
        simulacros.guardar(Simulacro.iniciar(8L, Datos.TREINTA_MINUTOS, reloj.ahora()));

        assertTrue(consulta.consultar(ESTUDIANTE).isEmpty());
    }
}
