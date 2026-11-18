package co.edu.udea.brujula.aplicacion.simulacro;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.apoyo.dobles.IntentosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.ParametrosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.RelojFijo;
import co.edu.udea.brujula.apoyo.dobles.SimulacrosEnMemoria;
import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.modelo.Simulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.EstadoDelSimulacro;
import co.edu.udea.brujula.dominio.servicio.CierreDeSimulacros;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Consulta del estado de un simulacro")
class ConsultaDelEstadoDelSimulacroTest {

    private static final Long ESTUDIANTE = 7L;

    private final SimulacrosEnMemoria simulacros = new SimulacrosEnMemoria();
    private final IntentosEnMemoria intentos = new IntentosEnMemoria();
    private final RelojFijo reloj = new RelojFijo();
    private final ConsultaDelEstadoDelSimulacro consulta = new ConsultaDelEstadoDelSimulacro(simulacros, intentos,
            new CierreDeSimulacros(simulacros, intentos, new ParametrosEnMemoria()), reloj);

    private final Simulacro simulacro =
            simulacros.guardar(Simulacro.iniciar(ESTUDIANTE, Datos.TREINTA_MINUTOS, reloj.ahora()));

    @Test
    void informa_la_hora_del_servidor_y_el_tiempo_restante() {
        reloj.avanzar(Duration.ofSeconds(90));

        EstadoDelSimulacro estado = consulta.consultar(ESTUDIANTE, simulacro.id());

        assertEquals(reloj.ahora(), estado.ahora());
        assertEquals(1710, estado.tiempoRestanteSeg());
        assertEquals(Simulacro.EN_CURSO, estado.estado());
    }

    @Test
    void cuando_el_tiempo_llega_a_cero_queda_finalizado() {
        reloj.avanzar(Duration.ofMinutes(30));

        EstadoDelSimulacro estado = consulta.consultar(ESTUDIANTE, simulacro.id());

        assertEquals(Simulacro.FINALIZADO, estado.estado());
        assertEquals(0, estado.tiempoRestanteSeg());
    }

    @Test
    void otro_estudiante_no_puede_ver_el_simulacro() {
        assertThrows(NoEncontrado.class, () -> consulta.consultar(8L, simulacro.id()));
    }
}
