package co.edu.udea.brujula.aplicacion.simulacro;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.apoyo.dobles.IntentosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.ParametrosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.RelojFijo;
import co.edu.udea.brujula.apoyo.dobles.SimulacrosEnMemoria;
import co.edu.udea.brujula.dominio.excepcion.Conflicto;
import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.modelo.Recomendacion;
import co.edu.udea.brujula.dominio.modelo.Simulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.ResultadoDeSimulacro;
import co.edu.udea.brujula.dominio.servicio.CierreDeSimulacros;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Consulta del resultado de un simulacro")
class ConsultaDelResultadoDelSimulacroTest {

    private static final Long ESTUDIANTE = 7L;

    private final SimulacrosEnMemoria simulacros = new SimulacrosEnMemoria();
    private final IntentosEnMemoria intentos = new IntentosEnMemoria();
    private final RelojFijo reloj = new RelojFijo();
    private final CierreDeSimulacros cierre = new CierreDeSimulacros(simulacros, intentos, new ParametrosEnMemoria());
    private final ConsultaDelResultadoDelSimulacro consulta =
            new ConsultaDelResultadoDelSimulacro(simulacros, intentos, cierre, reloj);

    private Simulacro iniciado() {
        return simulacros.guardar(Simulacro.iniciar(ESTUDIANTE, Datos.TREINTA_MINUTOS, reloj.ahora()));
    }

    @Test
    void muestra_el_desempeno_por_componente_competencia_y_el_detalle() {
        Simulacro simulacro = iniciado();
        intentos.guardar(Datos.intento(ESTUDIANTE, simulacro.id(), Datos.ALGEBRA, Datos.INTERPRETACION, true, 5));
        intentos.guardar(Datos.intento(ESTUDIANTE, simulacro.id(), Datos.GEOMETRIA, Datos.INTERPRETACION, false, 2));
        intentos.guardar(Datos.intento(ESTUDIANTE, null, Datos.GEOMETRIA, Datos.INTERPRETACION, true, 2));
        reloj.avanzar(Duration.ofMinutes(10));
        cierre.cerrar(simulacro, reloj.ahora());

        ResultadoDeSimulacro resultado = consulta.consultar(ESTUDIANTE, simulacro.id());

        assertEquals(30, resultado.duracionMinutos());
        assertEquals(600, resultado.tiempoUtilizadoSeg());
        assertEquals(2, resultado.respondidos());
        assertEquals(1, resultado.correctas());
        assertEquals(1, resultado.incorrectas());
        assertEquals(2, resultado.porComponente().size());
        assertEquals(new BigDecimal("0.00"), resultado.porComponente().get(1).porcentaje());
        assertEquals(new BigDecimal("50.00"), resultado.porCompetencia().get(0).porcentaje());
        assertEquals(2, resultado.detalle().size());
        assertEquals(2, resultado.detalle().get(1).nivelConfianza());
    }

    @Test
    void las_recomendaciones_quedan_asociadas_a_su_simulacro() {
        Simulacro primero = iniciado();
        intentos.guardar(Datos.intento(ESTUDIANTE, primero.id(), Datos.GEOMETRIA, Datos.INTERPRETACION, false, 2));
        cierre.cerrar(primero, reloj.ahora());
        Simulacro segundo = iniciado();
        intentos.guardar(Datos.intento(ESTUDIANTE, segundo.id(), Datos.ALGEBRA, Datos.ARGUMENTACION, true, 4));
        cierre.cerrar(segundo, reloj.ahora());

        List<Recomendacion> delPrimero = consulta.consultar(ESTUDIANTE, primero.id()).recomendaciones();
        ResultadoDeSimulacro delSegundo = consulta.consultar(ESTUDIANTE, segundo.id());

        assertEquals(List.of("Geometría", "Interpretación y representación"),
                delPrimero.stream().map(Recomendacion::area).toList());
        assertEquals(List.of(), delSegundo.recomendaciones());
        assertEquals("Tienes un buen desempeño general. Sigue practicando para mantenerlo.",
                delSegundo.mensajeRecomendacion());
    }

    @Test
    void no_hay_resultado_mientras_el_simulacro_sigue_en_curso() {
        Simulacro simulacro = iniciado();

        Conflicto error = assertThrows(Conflicto.class, () -> consulta.consultar(ESTUDIANTE, simulacro.id()));

        assertEquals("SIMULACRO_EN_CURSO", error.codigo());
    }

    @Test
    void si_el_tiempo_ya_paso_lo_finaliza_y_muestra_el_resultado() {
        Simulacro simulacro = iniciado();
        reloj.avanzar(Duration.ofHours(2));

        ResultadoDeSimulacro resultado = consulta.consultar(ESTUDIANTE, simulacro.id());

        assertEquals(1800, resultado.tiempoUtilizadoSeg());
    }

    @Test
    void cada_estudiante_solo_ve_sus_resultados() {
        Simulacro simulacro = iniciado();
        cierre.cerrar(simulacro, reloj.ahora());

        assertThrows(NoEncontrado.class, () -> consulta.consultar(8L, simulacro.id()));
    }
}
