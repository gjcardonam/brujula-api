package co.edu.udea.brujula.aplicacion.simulacro;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.apoyo.dobles.IntentosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.ParametrosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.RelojFijo;
import co.edu.udea.brujula.apoyo.dobles.SimulacrosEnMemoria;
import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.modelo.Simulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.ResultadoDeSimulacro;
import co.edu.udea.brujula.dominio.servicio.CierreDeSimulacros;
import co.edu.udea.brujula.dominio.servicio.GeneradorDeRecomendaciones;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Finalización voluntaria de un simulacro")
class FinalizacionDeSimulacroTest {

    private static final Long ESTUDIANTE = 7L;

    private final SimulacrosEnMemoria simulacros = new SimulacrosEnMemoria();
    private final IntentosEnMemoria intentos = new IntentosEnMemoria();
    private final RelojFijo reloj = new RelojFijo();
    private final FinalizacionDeSimulacro finalizacion = new FinalizacionDeSimulacro(simulacros, intentos,
            new CierreDeSimulacros(simulacros, intentos, new ParametrosEnMemoria()), reloj);

    private final Simulacro simulacro =
            simulacros.guardar(Simulacro.iniciar(ESTUDIANTE, Datos.TREINTA_MINUTOS, reloj.ahora()));

    @Test
    void conserva_los_intentos_confirmados_y_devuelve_el_resultado() {
        intentos.guardar(Datos.intento(ESTUDIANTE, simulacro.id(), Datos.ALGEBRA, Datos.INTERPRETACION, true, 5));
        intentos.guardar(Datos.intento(ESTUDIANTE, simulacro.id(), Datos.GEOMETRIA, Datos.ARGUMENTACION, false, 4));
        reloj.avanzar(Duration.ofMinutes(8));

        ResultadoDeSimulacro resultado = finalizacion.finalizar(ESTUDIANTE, simulacro.id());

        assertFalse(simulacros.guardado(simulacro.id()).enCurso());
        assertEquals(480, resultado.tiempoUtilizadoSeg());
        assertEquals(2, resultado.respondidos());
        assertEquals(new BigDecimal("50.00"), resultado.porcentajeAciertos());
        assertEquals(2, resultado.recomendaciones().size());
        assertEquals("Argumentación", resultado.recomendaciones().get(0).area());
    }

    @Test
    void finalizar_sin_respuestas_lo_informa() {
        ResultadoDeSimulacro resultado = finalizacion.finalizar(ESTUDIANTE, simulacro.id());

        assertEquals(0, resultado.respondidos());
        assertEquals(GeneradorDeRecomendaciones.SIN_RESPUESTAS, resultado.mensajeRecomendacion());
    }

    @Test
    void finalizar_de_nuevo_no_cambia_el_resultado() {
        intentos.guardar(Datos.intento(ESTUDIANTE, simulacro.id(), Datos.ALGEBRA, Datos.INTERPRETACION, false, 5));
        reloj.avanzar(Duration.ofMinutes(5));
        ResultadoDeSimulacro primero = finalizacion.finalizar(ESTUDIANTE, simulacro.id());
        reloj.avanzar(Duration.ofMinutes(5));

        ResultadoDeSimulacro segundo = finalizacion.finalizar(ESTUDIANTE, simulacro.id());

        assertEquals(primero.fechaFin(), segundo.fechaFin());
        assertEquals(primero.recomendaciones().size(), segundo.recomendaciones().size());
    }

    @Test
    void otro_estudiante_no_puede_finalizarlo() {
        assertThrows(NoEncontrado.class, () -> finalizacion.finalizar(8L, simulacro.id()));
    }
}
