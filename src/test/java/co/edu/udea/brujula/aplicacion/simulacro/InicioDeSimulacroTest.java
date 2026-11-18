package co.edu.udea.brujula.aplicacion.simulacro;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.apoyo.dobles.CatalogosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.EjerciciosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.IntentosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.ParametrosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.RelojFijo;
import co.edu.udea.brujula.apoyo.dobles.SimulacrosEnMemoria;
import co.edu.udea.brujula.dominio.excepcion.Conflicto;
import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.modelo.Simulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.EstadoDelSimulacro;
import co.edu.udea.brujula.dominio.servicio.CierreDeSimulacros;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Inicio de un simulacro")
class InicioDeSimulacroTest {

    private static final Long ESTUDIANTE = 7L;
    private static final Long SESENTA_MINUTOS = 3L;

    private final SimulacrosEnMemoria simulacros = new SimulacrosEnMemoria();
    private final IntentosEnMemoria intentos = new IntentosEnMemoria();
    private final EjerciciosEnMemoria ejercicios = new EjerciciosEnMemoria();
    private final RelojFijo reloj = new RelojFijo();
    private final InicioDeSimulacro inicio = new InicioDeSimulacro(simulacros, ejercicios, new CatalogosEnMemoria(),
            new CierreDeSimulacros(simulacros, intentos, new ParametrosEnMemoria()), reloj);

    @Test
    void crea_el_simulacro_con_la_duracion_elegida_y_la_hora_de_inicio() {
        ejercicios.agregar(Datos.ejercicioActivo(1L));

        EstadoDelSimulacro estado = inicio.iniciar(ESTUDIANTE, SESENTA_MINUTOS);

        assertEquals(Simulacro.EN_CURSO, estado.estado());
        assertEquals(60, estado.duracionMinutos());
        assertEquals(reloj.ahora(), estado.fechaInicio());
        assertEquals(reloj.ahora().plus(Duration.ofMinutes(60)), estado.finPrevisto());
        assertEquals(3600, estado.tiempoRestanteSeg());
        assertEquals(0, estado.respondidos());
        assertEquals(ESTUDIANTE, simulacros.guardado(estado.id()).idEstudiante());
    }

    @Test
    void exige_elegir_una_duracion() {
        ejercicios.agregar(Datos.ejercicioActivo(1L));

        assertThrows(DatosInvalidos.class, () -> inicio.iniciar(ESTUDIANTE, null));
        assertTrue(simulacros.todos().isEmpty());
    }

    @Test
    void rechaza_una_duracion_que_no_esta_en_el_catalogo() {
        ejercicios.agregar(Datos.ejercicioActivo(1L));

        DatosInvalidos error = assertThrows(DatosInvalidos.class, () -> inicio.iniciar(ESTUDIANTE, 99L));

        assertEquals("DURACION_INVALIDA", error.codigo());
    }

    @Test
    void no_permite_un_segundo_simulacro_mientras_otro_sigue_en_curso() {
        ejercicios.agregar(Datos.ejercicioActivo(1L));
        EstadoDelSimulacro primero = inicio.iniciar(ESTUDIANTE, SESENTA_MINUTOS);

        Conflicto error = assertThrows(Conflicto.class, () -> inicio.iniciar(ESTUDIANTE, 1L));

        assertEquals("SIMULACRO_EN_CURSO", error.codigo());
        assertEquals(primero.id(), error.datos().get("idSimulacro"));
        assertEquals(60, simulacros.guardado(primero.id()).duracion().minutos());
    }

    @Test
    void si_el_anterior_ya_se_vencio_lo_cierra_y_deja_iniciar_otro() {
        ejercicios.agregar(Datos.ejercicioActivo(1L));
        EstadoDelSimulacro primero = inicio.iniciar(ESTUDIANTE, 1L);
        reloj.avanzar(Duration.ofMinutes(31));

        EstadoDelSimulacro segundo = inicio.iniciar(ESTUDIANTE, 1L);

        assertFalse(simulacros.guardado(primero.id()).enCurso());
        assertTrue(simulacros.guardado(segundo.id()).enCurso());
    }

    @Test
    void no_inicia_si_no_hay_ejercicios_activos() {
        ejercicios.agregar(Datos.ejercicioDesactivado(1L));

        Conflicto error = assertThrows(Conflicto.class, () -> inicio.iniciar(ESTUDIANTE, 1L));

        assertEquals("SIN_EJERCICIOS", error.codigo());
    }
}
