package co.edu.udea.brujula.aplicacion.simulacro;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.apoyo.dobles.EjerciciosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.IntentosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.ParametrosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.RelojFijo;
import co.edu.udea.brujula.apoyo.dobles.SimulacrosEnMemoria;
import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.modelo.Intento;
import co.edu.udea.brujula.dominio.modelo.Simulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.SiguienteDelSimulacro;
import co.edu.udea.brujula.dominio.servicio.CierreDeSimulacros;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Presentación del siguiente ejercicio del simulacro")
class PresentacionDelSiguienteEjercicioTest {

    private static final Long ESTUDIANTE = 7L;

    private final SimulacrosEnMemoria simulacros = new SimulacrosEnMemoria();
    private final IntentosEnMemoria intentos = new IntentosEnMemoria();
    private final EjerciciosEnMemoria ejercicios = new EjerciciosEnMemoria().descartandoLosRespondidosEn(intentos);
    private final RelojFijo reloj = new RelojFijo();
    private final PresentacionDelSiguienteEjercicio presentacion = new PresentacionDelSiguienteEjercicio(simulacros,
            intentos, ejercicios, new CierreDeSimulacros(simulacros, intentos, new ParametrosEnMemoria()), reloj);

    private final Simulacro simulacro =
            simulacros.guardar(Simulacro.iniciar(ESTUDIANTE, Datos.TREINTA_MINUTOS, reloj.ahora()));

    private void responder(Ejercicio ejercicio) {
        intentos.guardar(Intento.nuevo(ESTUDIANTE, ejercicio, ejercicio.opciones().get(0), 3, UUID.randomUUID(),
                simulacro.id(), reloj.ahora()));
    }

    @Test
    void entrega_un_ejercicio_activo_con_el_estado_del_temporizador() {
        ejercicios.agregar(Datos.ejercicioDesactivado(1L));
        ejercicios.agregar(Datos.ejercicioActivo(2L));

        SiguienteDelSimulacro siguiente = presentacion.presentar(ESTUDIANTE, simulacro.id());

        assertFalse(siguiente.finalizado());
        assertEquals(2L, siguiente.ejercicio().id());
        assertEquals(1800, siguiente.estado().tiempoRestanteSeg());
    }

    @Test
    void mientras_no_responda_vuelve_a_entregar_el_mismo_ejercicio() {
        ejercicios.agregar(Datos.ejercicioActivo(1L));
        ejercicios.agregar(Datos.ejercicioActivo(2L));

        Long primero = presentacion.presentar(ESTUDIANTE, simulacro.id()).ejercicio().id();
        Long otraVez = presentacion.presentar(ESTUDIANTE, simulacro.id()).ejercicio().id();

        assertEquals(primero, otraVez);
    }

    @Test
    void no_repite_ejercicios_dentro_del_simulacro_y_cierra_cuando_se_acaban() {
        for (long id = 1; id <= 3; id++) ejercicios.agregar(Datos.ejercicioActivo(id));
        Set<Long> vistos = new HashSet<>();

        SiguienteDelSimulacro siguiente = presentacion.presentar(ESTUDIANTE, simulacro.id());
        while (!siguiente.finalizado()) {
            assertTrue(vistos.add(siguiente.ejercicio().id()));
            responder(siguiente.ejercicio());
            siguiente = presentacion.presentar(ESTUDIANTE, simulacro.id());
        }

        assertEquals(3, vistos.size());
        assertEquals(SiguienteDelSimulacro.SIN_EJERCICIOS, siguiente.motivo());
        assertNull(siguiente.ejercicio());
        assertFalse(simulacros.guardado(simulacro.id()).enCurso());
    }

    @Test
    void cuando_se_agota_el_tiempo_finaliza_en_lugar_de_entregar_otro() {
        ejercicios.agregar(Datos.ejercicioActivo(1L));
        reloj.avanzar(Duration.ofMinutes(30));

        SiguienteDelSimulacro siguiente = presentacion.presentar(ESTUDIANTE, simulacro.id());

        assertTrue(siguiente.finalizado());
        assertEquals(SiguienteDelSimulacro.POR_TIEMPO, siguiente.motivo());
        assertEquals(Simulacro.FINALIZADO, siguiente.estado().estado());
    }

    @Test
    void sobre_un_simulacro_ya_finalizado_solo_informa_que_termino() {
        ejercicios.agregar(Datos.ejercicioActivo(1L));
        new CierreDeSimulacros(simulacros, intentos, new ParametrosEnMemoria()).cerrar(simulacro, reloj.ahora());

        SiguienteDelSimulacro siguiente = presentacion.presentar(ESTUDIANTE, simulacro.id());

        assertEquals(SiguienteDelSimulacro.YA_FINALIZADO, siguiente.motivo());
        assertNull(siguiente.ejercicio());
    }

    @Test
    void otro_estudiante_no_puede_pedir_ejercicios_del_simulacro() {
        ejercicios.agregar(Datos.ejercicioActivo(1L));

        assertThrows(NoEncontrado.class, () -> presentacion.presentar(8L, simulacro.id()));
    }
}
