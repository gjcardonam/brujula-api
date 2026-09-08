package co.edu.udea.brujula.aplicacion;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.apoyo.dobles.*;
import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.excepcion.RecursoNoDisponible;
import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.modelo.Opcion;
import co.edu.udea.brujula.dominio.modelo.TipoError;
import co.edu.udea.brujula.dominio.modelo.consulta.ResultadoDeIntento;
import co.edu.udea.brujula.dominio.puerto.entrada.ResponderEjercicio;
import co.edu.udea.brujula.dominio.puerto.salida.ParametrosDelSistema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Registro de respuestas (HU-010 a HU-012, HU-016 y HU-027)")
class RegistroDeIntentosTest {

    private static final Long ANA = 7L;

    private IntentosEnMemoria intentos;
    private EjerciciosEnMemoria ejercicios;
    private RegistroDeIntentos registro;
    private Ejercicio ejercicio;
    private Opcion correcta;
    private Opcion incorrecta;

    @BeforeEach
    void prepararEscenario() {
        intentos = new IntentosEnMemoria();
        ejercicios = new EjerciciosEnMemoria();
        var catalogos = new CatalogosEnMemoria();
        var parametros = new ParametrosEnMemoria()
                .con(ParametrosDelSistema.VENTANA_INTENTOS, 5)
                .con(ParametrosDelSistema.MINIMO_INTENTOS_VENTANA, 3)
                .con(ParametrosDelSistema.UMBRAL_DOMINIO_PCT, 70);
        RelojFijo reloj = new RelojFijo();

        correcta = Datos.opcionCorrecta(11L);
        incorrecta = Datos.distractor(12L, null);
        ejercicio = ejercicios.agregar(Datos.ejercicio(1L, Datos.ALGEBRA, Datos.INTERPRETACION,
                List.of(correcta, incorrecta)));

        var simulacros = new Simulacros(new SimulacrosEnMemoria(), intentos, ejercicios, catalogos, parametros, reloj);
        registro = new RegistroDeIntentos(intentos, ejercicios, catalogos, parametros, simulacros, reloj);
    }

    private ResponderEjercicio.Respuesta respuesta(Opcion opcion, int confianza, UUID token) {
        return new ResponderEjercicio.Respuesta(ejercicio.id(), opcion.id(), confianza, token, null);
    }

    @Test
    void guardaElIntentoYDevuelveLaRetroalimentacionDeLaOpcionElegida() {
        ResultadoDeIntento resultado = registro.responder(ANA, respuesta(incorrecta, 2, null));

        assertFalse(resultado.esCorrecto());
        assertEquals(correcta.id(), resultado.idOpcionCorrecta());
        assertEquals(incorrecta.retroalimentacion(), resultado.retroalimentacion());
        assertTrue(resultado.retroalimentacionDisponible());
        assertEquals(1, resultado.numeroIntento());
    }

    @Test
    void clasificaComoAnsiedadCuandoFallaMuySeguro() {
        registro.responder(ANA, respuesta(incorrecta, 5, null));

        assertEquals(TipoError.ANSIEDAD, intentos.todos().get(0).tipoError().nombre());
    }

    @Test
    void noClasificaLasRespuestasCorrectas() {
        registro.responder(ANA, respuesta(correcta, 3, null));

        assertNull(intentos.todos().get(0).tipoError());
    }

    @Test
    void reenviarLaMismaRespuestaNoCreaUnIntentoNuevo() {
        UUID token = UUID.randomUUID();
        ResultadoDeIntento primera = registro.responder(ANA, respuesta(incorrecta, 2, token));
        ResultadoDeIntento reenvio = registro.responder(ANA, respuesta(incorrecta, 2, token));

        assertEquals(primera.idIntento(), reenvio.idIntento());
        assertTrue(reenvio.repetido());
        assertEquals(1, intentos.todos().size());
    }

    @Test
    void cadaRespuestaSinTokenQuedaComoUnIntentoIndependiente() {
        registro.responder(ANA, respuesta(incorrecta, 2, null));
        ResultadoDeIntento segunda = registro.responder(ANA, respuesta(correcta, 4, null));

        assertEquals(2, intentos.todos().size());
        assertEquals(2, segunda.numeroIntento());
    }

    @Test
    void noAceptaRespuestasSobreUnEjercicioDesactivado() {
        ejercicio.desactivar();

        assertThrows(RecursoNoDisponible.class, () -> registro.responder(ANA, respuesta(correcta, 3, null)));
    }

    @Test
    void exigeElNivelDeConfianza() {
        assertThrows(DatosInvalidos.class,
                () -> registro.responder(ANA, new ResponderEjercicio.Respuesta(ejercicio.id(), correcta.id(), null, null, null)));
        assertThrows(DatosInvalidos.class, () -> registro.responder(ANA, respuesta(correcta, 6, null)));
    }

    @Test
    void rechazaUnaOpcionQueNoEsDelEjercicio() {
        var ajena = new ResponderEjercicio.Respuesta(ejercicio.id(), 999L, 3, null, null);

        assertThrows(DatosInvalidos.class, () -> registro.responder(ANA, ajena));
    }
}
