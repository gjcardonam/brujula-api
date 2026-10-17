package co.edu.udea.brujula.dominio.servicio;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.dominio.modelo.Intento;
import co.edu.udea.brujula.dominio.modelo.Simulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.DesempenoPorArea;
import co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeIntento;
import co.edu.udea.brujula.dominio.modelo.consulta.ResultadoDeSimulacro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Desempeño de un simulacro")
class CalculadoraDeDesempenoTest {

    private final List<Intento> intentos = List.of(
            Datos.intento(7L, 1L, Datos.ALGEBRA, Datos.INTERPRETACION, true, 5),
            Datos.intento(7L, 1L, Datos.ALGEBRA, Datos.ARGUMENTACION, false, 2),
            Datos.intento(7L, 1L, Datos.GEOMETRIA, Datos.INTERPRETACION, true, 4));

    @Test
    void calcula_el_porcentaje_de_aciertos_por_componente_con_dos_decimales() {
        List<DesempenoPorArea> porComponente = CalculadoraDeDesempeno.porComponente(intentos);

        assertEquals(2, porComponente.size());
        assertEquals("Álgebra y cálculo", porComponente.get(0).nombre());
        assertEquals(2, porComponente.get(0).intentos());
        assertEquals(new BigDecimal("50.00"), porComponente.get(0).porcentaje());
        assertEquals(new BigDecimal("100.00"), porComponente.get(1).porcentaje());
    }

    @Test
    void calcula_el_porcentaje_de_aciertos_por_competencia() {
        List<DesempenoPorArea> porCompetencia = CalculadoraDeDesempeno.porCompetencia(intentos);

        assertEquals(2, porCompetencia.size());
        assertEquals(new BigDecimal("100.00"), porCompetencia.get(0).porcentaje());
        assertEquals(new BigDecimal("0.00"), porCompetencia.get(1).porcentaje());
    }

    @Test
    void arma_el_resultado_solo_con_los_intentos_recibidos() {
        Simulacro simulacro = Simulacro.iniciar(7L, Datos.TREINTA_MINUTOS, Datos.AHORA);
        simulacro.asignarId(1L);
        simulacro.finalizar(Datos.AHORA.plus(Duration.ofMinutes(12)));

        ResultadoDeSimulacro resultado = CalculadoraDeDesempeno.resultadoDe(simulacro, intentos, List.of());

        assertEquals(30, resultado.duracionMinutos());
        assertEquals(720, resultado.tiempoUtilizadoSeg());
        assertEquals(3, resultado.respondidos());
        assertEquals(2, resultado.correctas());
        assertEquals(1, resultado.incorrectas());
        assertEquals(new BigDecimal("66.67"), resultado.porcentajeAciertos());
        assertEquals(GeneradorDeRecomendaciones.BUEN_DESEMPENO, resultado.mensajeRecomendacion());
        assertEquals(3, resultado.detalle().size());
    }

    @Test
    void el_detalle_marca_la_opcion_elegida_y_trae_su_retroalimentacion() {
        Intento fallido = intentos.get(1);

        DetalleDeIntento detalle = CalculadoraDeDesempeno.detalleDe(fallido);

        assertFalse(detalle.esCorrecto());
        assertEquals(2, detalle.nivelConfianza());
        assertEquals(fallido.opcionSeleccionada().retroalimentacion(), detalle.retroalimentacion());
        assertTrue(detalle.opciones().get(1).seleccionada());
        assertTrue(detalle.opciones().get(0).esCorrecta());
        assertEquals("A", detalle.opciones().get(0).letra());
        assertEquals(1L, detalle.idSimulacro());
    }
}
