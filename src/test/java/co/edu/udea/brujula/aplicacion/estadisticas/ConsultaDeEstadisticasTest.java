package co.edu.udea.brujula.aplicacion.estadisticas;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.apoyo.dobles.IntentosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.ParametrosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.RelojFijo;
import co.edu.udea.brujula.apoyo.dobles.SimulacrosEnMemoria;
import co.edu.udea.brujula.dominio.modelo.Simulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.Estadisticas;
import co.edu.udea.brujula.dominio.servicio.CierreDeSimulacros;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Consulta de las estadísticas personales")
class ConsultaDeEstadisticasTest {

    private static final Long ESTUDIANTE = 7L;

    private final SimulacrosEnMemoria simulacros = new SimulacrosEnMemoria();
    private final IntentosEnMemoria intentos = new IntentosEnMemoria();
    private final RelojFijo reloj = new RelojFijo();
    private final CierreDeSimulacros cierre = new CierreDeSimulacros(simulacros, intentos, new ParametrosEnMemoria());
    private final ConsultaDeEstadisticas consulta = new ConsultaDeEstadisticas(intentos, simulacros, cierre, reloj);

    @Test
    void sin_actividad_devuelve_el_mensaje_informativo() {
        intentos.guardar(Datos.intento(8L, null, Datos.ALGEBRA, Datos.INTERPRETACION, true, 3));

        Estadisticas estadisticas = consulta.consultar(ESTUDIANTE);

        assertTrue(estadisticas.sinDatos());
        assertEquals(Estadisticas.SIN_DATOS, estadisticas.mensaje());
    }

    @Test
    void combina_la_practica_y_los_simulacros_del_estudiante() {
        intentos.guardar(Datos.intento(ESTUDIANTE, null, Datos.ALGEBRA, Datos.INTERPRETACION, true, 5));
        Simulacro simulacro = simulacros.guardar(Simulacro.iniciar(ESTUDIANTE, Datos.TREINTA_MINUTOS, reloj.ahora()));
        intentos.guardar(Datos.intento(ESTUDIANTE, simulacro.id(), Datos.GEOMETRIA, Datos.ARGUMENTACION, false, 2));
        cierre.cerrar(simulacro, reloj.ahora().plus(Duration.ofMinutes(6)));
        intentos.guardar(Datos.intento(8L, null, Datos.GEOMETRIA, Datos.ARGUMENTACION, false, 2));

        Estadisticas estadisticas = consulta.consultar(ESTUDIANTE);

        assertEquals(2, estadisticas.resumen().intentados());
        assertEquals(new BigDecimal("50.00"), estadisticas.resumen().porcentajeAciertos());
        assertEquals(1, estadisticas.simulacros().cantidad());
        assertEquals(new BigDecimal("360.00"), estadisticas.simulacros().tiempoPromedioSeg());
        assertEquals(new BigDecimal("10.00"), estadisticas.simulacros().ejerciciosPorHora());
    }

    @Test
    void un_simulacro_vencido_cuenta_al_consultar() {
        simulacros.guardar(Simulacro.iniciar(ESTUDIANTE, Datos.TREINTA_MINUTOS, reloj.ahora()));
        reloj.avanzar(Duration.ofHours(1));

        assertEquals(1, consulta.consultar(ESTUDIANTE).simulacros().cantidad());
    }
}
