package co.edu.udea.brujula.aplicacion.practica;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.apoyo.dobles.EjerciciosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.IntentosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.RelojFijo;
import co.edu.udea.brujula.dominio.excepcion.AccesoDenegado;
import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.excepcion.RecursoNoDisponible;
import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.modelo.Opcion;
import co.edu.udea.brujula.dominio.modelo.consulta.ResultadoDeIntento;
import co.edu.udea.brujula.dominio.puerto.entrada.RegistrarIntento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Registro de un intento")
class RegistroDeIntentoTest {

    private static final Long ESTUDIANTE = 7L;

    private final EjerciciosEnMemoria ejercicios = new EjerciciosEnMemoria();
    private final IntentosEnMemoria intentos = new IntentosEnMemoria();
    private final RelojFijo reloj = new RelojFijo();
    private final RegistroDeIntento registro = new RegistroDeIntento(intentos, ejercicios, reloj);

    private Ejercicio ejercicio;

    @BeforeEach
    void prepararElEjercicio() {
        ejercicio = ejercicios.agregar(Datos.ejercicioActivo(1L));
    }

    private RegistrarIntento.Respuesta respuestaCon(Opcion opcion, Integer confianza) {
        return new RegistrarIntento.Respuesta(ejercicio.id(), opcion.id(), confianza, UUID.randomUUID());
    }

    private RegistrarIntento.Respuesta respuestaCon(Opcion opcion, Integer confianza, UUID token) {
        return new RegistrarIntento.Respuesta(ejercicio.id(), opcion.id(), confianza, token);
    }

    @Test
    void guarda_el_intento_con_su_nivel_de_confianza_y_la_hora_del_envio() {
        ResultadoDeIntento resultado = registro.registrar(ESTUDIANTE, respuestaCon(ejercicio.opciones().get(0), 5));

        assertTrue(resultado.esCorrecto());
        assertEquals(5, resultado.nivelConfianza());
        assertEquals(reloj.ahora(), resultado.respondidoEn());
        assertEquals(1, resultado.numeroIntento());
    }

    @Test
    void devuelve_la_retroalimentacion_de_la_opcion_elegida() {
        Opcion distractor = ejercicio.opciones().get(1);

        ResultadoDeIntento resultado = registro.registrar(ESTUDIANTE, respuestaCon(distractor, 2));

        assertFalse(resultado.esCorrecto());
        assertEquals(distractor.retroalimentacion(), resultado.retroalimentacion());
        assertTrue(resultado.retroalimentacionDisponible());
        assertEquals(distractor.id(), resultado.idOpcionSeleccionada());
    }

    @Test
    void avisa_cuando_la_opcion_elegida_no_tiene_retroalimentacion() {
        ResultadoDeIntento resultado = registro.registrar(ESTUDIANTE, respuestaCon(ejercicio.opciones().get(2), 1));

        assertFalse(resultado.retroalimentacionDisponible());
        assertEquals(Opcion.SIN_RETROALIMENTACION, resultado.retroalimentacion());
    }

    @Test
    void exige_el_token_de_idempotencia() {
        RegistrarIntento.Respuesta sinToken = respuestaCon(ejercicio.opciones().get(0), 3, null);

        DatosInvalidos error = assertThrows(DatosInvalidos.class, () -> registro.registrar(ESTUDIANTE, sinToken));

        assertEquals("TOKEN_IDEMPOTENCIA_REQUERIDO", error.codigo());
        assertTrue(intentos.todos().isEmpty());
    }

    @Test
    void exige_un_nivel_de_confianza_entre_uno_y_cinco() {
        assertThrows(DatosInvalidos.class,
                () -> registro.registrar(ESTUDIANTE, respuestaCon(ejercicio.opciones().get(0), null)));
        assertThrows(DatosInvalidos.class,
                () -> registro.registrar(ESTUDIANTE, respuestaCon(ejercicio.opciones().get(0), 6)));
        assertTrue(intentos.todos().isEmpty());
    }

    @Test
    void cada_respuesta_queda_como_un_intento_independiente() {
        registro.registrar(ESTUDIANTE, respuestaCon(ejercicio.opciones().get(1), 2));
        ResultadoDeIntento segundo = registro.registrar(ESTUDIANTE, respuestaCon(ejercicio.opciones().get(0), 4));

        assertEquals(2, intentos.todos().size());
        assertEquals(2, segundo.numeroIntento());
    }

    @Test
    void no_duplica_el_intento_cuando_se_reenvia_el_mismo_token() {
        UUID token = UUID.randomUUID();
        ResultadoDeIntento primero = registro.registrar(ESTUDIANTE, respuestaCon(ejercicio.opciones().get(0), 3, token));
        ResultadoDeIntento reenvio = registro.registrar(ESTUDIANTE, respuestaCon(ejercicio.opciones().get(0), 3, token));

        assertEquals(1, intentos.todos().size());
        assertEquals(primero.idIntento(), reenvio.idIntento());
        assertTrue(reenvio.repetido());
    }

    @Test
    void no_deja_ver_el_intento_de_otro_estudiante_con_su_token() {
        UUID token = UUID.randomUUID();
        registro.registrar(ESTUDIANTE, respuestaCon(ejercicio.opciones().get(0), 3, token));

        assertThrows(AccesoDenegado.class,
                () -> registro.registrar(8L, respuestaCon(ejercicio.opciones().get(0), 3, token)));
    }

    @Test
    void no_registra_sobre_un_ejercicio_desactivado() {
        ejercicios.agregar(Datos.ejercicioDesactivado(2L));
        Ejercicio desactivado = ejercicios.porId(2L).orElseThrow();

        assertThrows(RecursoNoDisponible.class, () -> registro.registrar(ESTUDIANTE,
                new RegistrarIntento.Respuesta(2L, desactivado.opciones().get(0).id(), 3, UUID.randomUUID())));
    }

    @Test
    void rechaza_una_opcion_que_no_pertenece_al_ejercicio() {
        assertThrows(DatosInvalidos.class, () -> registro.registrar(ESTUDIANTE,
                new RegistrarIntento.Respuesta(1L, 999L, 3, UUID.randomUUID())));
    }

    @Test
    void avisa_cuando_el_ejercicio_no_existe() {
        assertThrows(NoEncontrado.class, () -> registro.registrar(ESTUDIANTE,
                new RegistrarIntento.Respuesta(99L, 10L, 3, UUID.randomUUID())));
    }
}
