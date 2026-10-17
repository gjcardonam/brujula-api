package co.edu.udea.brujula.dominio.servicio;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.dominio.modelo.Intento;
import co.edu.udea.brujula.dominio.modelo.Simulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.Estadisticas;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Estadísticas personales")
class CalculadoraDeEstadisticasTest {

    private static Simulacro finalizado(Long id, Duration usado) {
        Simulacro simulacro = Simulacro.iniciar(7L, Datos.TREINTA_MINUTOS, Datos.AHORA);
        simulacro.asignarId(id);
        simulacro.finalizar(Datos.AHORA.plus(usado));
        return simulacro;
    }

    @Test
    void sin_intentos_ni_simulacros_avisa_en_lugar_de_mostrar_ceros() {
        Estadisticas estadisticas = CalculadoraDeEstadisticas.calcular(List.of(), List.of());

        assertTrue(estadisticas.sinDatos());
        assertEquals(Estadisticas.SIN_DATOS, estadisticas.mensaje());
        assertTrue(estadisticas.porComponente().isEmpty());
        assertTrue(estadisticas.porConfianza().isEmpty());
    }

    @Test
    void resume_la_practica_y_los_simulacros_juntos() {
        List<Intento> intentos = List.of(
                Datos.intento(7L, null, Datos.ALGEBRA, Datos.INTERPRETACION, true, 5),
                Datos.intento(7L, null, Datos.ALGEBRA, Datos.INTERPRETACION, false, 5),
                Datos.intento(7L, 1L, Datos.GEOMETRIA, Datos.ARGUMENTACION, false, 1));

        Estadisticas estadisticas = CalculadoraDeEstadisticas.calcular(intentos, List.of());

        assertFalse(estadisticas.sinDatos());
        assertNull(estadisticas.mensaje());
        assertEquals(3, estadisticas.resumen().intentados());
        assertEquals(new BigDecimal("33.33"), estadisticas.resumen().porcentajeAciertos());
        assertEquals(new BigDecimal("66.67"), estadisticas.resumen().porcentajeDesaciertos());
    }

    @Test
    void solo_muestra_las_areas_y_niveles_de_confianza_que_tienen_intentos() {
        List<Intento> intentos = List.of(
                Datos.intento(7L, null, Datos.ALGEBRA, Datos.INTERPRETACION, true, 5),
                Datos.intento(7L, null, Datos.ALGEBRA, Datos.INTERPRETACION, false, 5),
                Datos.intento(7L, null, Datos.ALGEBRA, Datos.INTERPRETACION, false, 2));

        Estadisticas estadisticas = CalculadoraDeEstadisticas.calcular(intentos, List.of());

        assertEquals(1, estadisticas.porComponente().size());
        Estadisticas.Desempeno algebra = estadisticas.porComponente().get(0);
        assertEquals(3, algebra.intentados());
        assertEquals(1, algebra.correctas());
        assertEquals(2, algebra.incorrectas());
        assertEquals(new BigDecimal("33.33"), algebra.porcentajeAciertos());
        assertEquals(1, estadisticas.porCompetencia().size());
        assertEquals(2, estadisticas.porConfianza().size());
        assertEquals(2, estadisticas.porConfianza().get(0).nivel());
        assertEquals(new BigDecimal("50.00"), estadisticas.porConfianza().get(1).porcentajeCorrectas());
    }

    @Test
    void promedia_los_simulacros_finalizados() {
        List<Intento> intentos = List.of(
                Datos.intento(7L, 1L, Datos.ALGEBRA, Datos.INTERPRETACION, true, 3),
                Datos.intento(7L, 1L, Datos.ALGEBRA, Datos.INTERPRETACION, false, 3),
                Datos.intento(7L, 2L, Datos.ALGEBRA, Datos.INTERPRETACION, true, 3),
                Datos.intento(7L, 2L, Datos.ALGEBRA, Datos.INTERPRETACION, true, 3));

        Estadisticas.ResumenDeSimulacros simulacros = CalculadoraDeEstadisticas.calcular(intentos,
                List.of(finalizado(1L, Duration.ofMinutes(10)), finalizado(2L, Duration.ofMinutes(20)))).simulacros();

        assertEquals(2, simulacros.cantidad());
        assertEquals(new BigDecimal("75.00"), simulacros.porcentajePromedioAciertos());
        assertEquals(new BigDecimal("2.00"), simulacros.promedioEjercicios());
        assertEquals(new BigDecimal("900.00"), simulacros.tiempoPromedioSeg());
        assertEquals(new BigDecimal("8.00"), simulacros.ejerciciosPorHora());
    }

    @Test
    void sin_simulacros_el_resumen_de_simulacros_queda_en_cero() {
        Estadisticas.ResumenDeSimulacros simulacros = CalculadoraDeEstadisticas.calcular(
                List.of(Datos.intento(Datos.ALGEBRA, Datos.INTERPRETACION, true)), List.of()).simulacros();

        assertEquals(0, simulacros.cantidad());
        assertEquals(new BigDecimal("0.00"), simulacros.ejerciciosPorHora());
    }
}
