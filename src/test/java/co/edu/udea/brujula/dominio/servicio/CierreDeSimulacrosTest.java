package co.edu.udea.brujula.dominio.servicio;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.apoyo.dobles.IntentosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.ParametrosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.SimulacrosEnMemoria;
import co.edu.udea.brujula.dominio.modelo.Simulacro;
import co.edu.udea.brujula.dominio.puerto.salida.ParametrosDelSistema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Cierre de un simulacro")
class CierreDeSimulacrosTest {

    private static final Long ESTUDIANTE = 7L;

    private final SimulacrosEnMemoria simulacros = new SimulacrosEnMemoria();
    private final IntentosEnMemoria intentos = new IntentosEnMemoria();
    private final ParametrosEnMemoria parametros = new ParametrosEnMemoria();
    private final CierreDeSimulacros cierre = new CierreDeSimulacros(simulacros, intentos, parametros);

    private Simulacro iniciado() {
        return simulacros.guardar(Simulacro.iniciar(ESTUDIANTE, Datos.TREINTA_MINUTOS, Datos.AHORA));
    }

    @Test
    void al_cerrar_guarda_el_tiempo_utilizado_y_las_recomendaciones() {
        Simulacro simulacro = iniciado();
        intentos.guardar(Datos.intento(ESTUDIANTE, simulacro.id(), Datos.GEOMETRIA, Datos.INTERPRETACION, false, 4));

        cierre.cerrar(simulacro, Datos.AHORA.plus(Duration.ofMinutes(5)));

        Simulacro guardado = simulacros.guardado(simulacro.id());
        assertFalse(guardado.enCurso());
        assertEquals(300, guardado.tiempoUtilizadoSeg());
        assertEquals(2, simulacros.recomendacionesDe(simulacro.id()).size());
    }

    @Test
    void si_el_tiempo_ya_se_habia_agotado_el_fin_es_el_previsto() {
        Simulacro simulacro = iniciado();

        cierre.cerrarSiSeVencio(simulacro, Datos.AHORA.plus(Duration.ofHours(3)));

        Simulacro guardado = simulacros.guardado(simulacro.id());
        assertEquals(simulacro.finPrevisto(), guardado.fin());
        assertEquals(1800, guardado.tiempoUtilizadoSeg());
    }

    @Test
    void no_cierra_un_simulacro_que_aun_tiene_tiempo() {
        Simulacro simulacro = iniciado();

        cierre.cerrarSiSeVencio(simulacro, Datos.AHORA.plus(Duration.ofMinutes(29)));

        assertTrue(simulacros.guardado(simulacro.id()).enCurso());
    }

    @Test
    void cerrar_dos_veces_no_duplica_las_recomendaciones_ni_cambia_el_fin() {
        Simulacro simulacro = iniciado();
        intentos.guardar(Datos.intento(ESTUDIANTE, simulacro.id(), Datos.GEOMETRIA, Datos.INTERPRETACION, false, 4));
        Instant primerCierre = Datos.AHORA.plus(Duration.ofMinutes(5));
        cierre.cerrar(simulacros.guardado(simulacro.id()), primerCierre);

        Simulacro otraCopia = new Simulacro(simulacro.id(), ESTUDIANTE, Datos.TREINTA_MINUTOS, Datos.AHORA, null,
                Simulacro.EN_CURSO, null);
        Simulacro resultado = cierre.cerrar(otraCopia, Datos.AHORA.plus(Duration.ofMinutes(9)));

        assertEquals(primerCierre, resultado.fin());
        assertEquals(2, simulacros.recomendacionesDe(simulacro.id()).size());
    }

    @Test
    void usa_el_umbral_configurado_en_los_parametros() {
        parametros.con(ParametrosDelSistema.UMBRAL_RECOMENDACION_PCT, 40);
        Simulacro simulacro = iniciado();
        intentos.guardar(Datos.intento(ESTUDIANTE, simulacro.id(), Datos.GEOMETRIA, Datos.INTERPRETACION, true, 4));
        intentos.guardar(Datos.intento(ESTUDIANTE, simulacro.id(), Datos.GEOMETRIA, Datos.INTERPRETACION, false, 4));

        cierre.cerrar(simulacro, Datos.AHORA.plus(Duration.ofMinutes(5)));

        assertTrue(simulacros.recomendacionesDe(simulacro.id()).isEmpty());
    }

    @Test
    void cierra_solo_los_simulacros_vencidos_del_estudiante() {
        Simulacro vencido = iniciado();
        simulacros.guardar(Simulacro.iniciar(8L, Datos.TREINTA_MINUTOS, Datos.AHORA));

        cierre.cerrarLosVencidosDe(ESTUDIANTE, Datos.AHORA.plus(Duration.ofMinutes(31)));

        assertFalse(simulacros.guardado(vencido.id()).enCurso());
        assertTrue(simulacros.guardado(2L).enCurso());
    }
}
