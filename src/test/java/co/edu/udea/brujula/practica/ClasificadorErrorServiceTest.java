package co.edu.udea.brujula.practica;

import co.edu.udea.brujula.catalogo.*;
import co.edu.udea.brujula.ejercicio.Ejercicio;
import co.edu.udea.brujula.ejercicio.OpcionRespuesta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/** Reglas de HU-027 y HU-028. */
class ClasificadorErrorServiceTest {

    private IntentoRepository intentos;
    private ParametrosService parametros;
    private ClasificadorErrorService clasificador;
    private Ejercicio ejercicio;
    private OpcionRespuesta distractorSinTipo;
    private OpcionRespuesta distractorHabito;

    @BeforeEach
    void setUp() {
        intentos = Mockito.mock(IntentoRepository.class);
        parametros = Mockito.mock(ParametrosService.class);
        TipoErrorRepository tipos = Mockito.mock(TipoErrorRepository.class);
        when(tipos.findByNombre(anyString())).thenAnswer(inv -> Optional.of(tipo(inv.getArgument(0))));
        when(parametros.entero(eq(ParametrosService.VENTANA_INTENTOS), anyInt())).thenReturn(5);
        when(parametros.entero(eq(ParametrosService.MINIMO_INTENTOS_VENTANA), anyInt())).thenReturn(3);
        when(parametros.entero(eq(ParametrosService.UMBRAL_DOMINIO_PCT), anyInt())).thenReturn(70);
        clasificador = new ClasificadorErrorService(intentos, tipos, parametros);

        Componente comp = new Componente();
        comp.setId(1L);
        Competencia compe = new Competencia();
        compe.setId(2L);
        ejercicio = new Ejercicio();
        ejercicio.setComponente(comp);
        ejercicio.setCompetencia(compe);
        distractorSinTipo = new OpcionRespuesta();
        distractorHabito = new OpcionRespuesta();
        distractorHabito.setTipoError(tipo(TipoError.HABITO));
    }

    private static TipoError tipo(String nombre) {
        TipoError t = new TipoError();
        t.setNombre(nombre);
        return t;
    }

    private void ventana(Boolean... previos) {
        when(intentos.ventanaDominio(anyLong(), anyLong(), anyLong(), any(Pageable.class))).thenReturn(List.of(previos));
    }

    @Test
    void confianzaAltaEsAnsiedad() {
        ventana(true, true, true, true, true);
        assertEquals(TipoError.ANSIEDAD, clasificador.clasificar(9L, ejercicio, distractorSinTipo, 4).getNombre());
        assertEquals(TipoError.ANSIEDAD, clasificador.clasificar(9L, ejercicio, distractorSinTipo, 5).getNombre());
    }

    @Test
    void dominioPrevioConConfianzaBajaEsHabito() {
        ventana(true, true, true, false, true);       // 80 % en la ventana
        assertEquals(TipoError.HABITO, clasificador.clasificar(9L, ejercicio, distractorSinTipo, 2).getNombre());
    }

    @Test
    void sinDominioNiTipoDelDistractorEsCognitivo() {
        ventana(true, false, false, true, false);      // 40 %
        assertEquals(TipoError.COGNITIVO, clasificador.clasificar(9L, ejercicio, distractorSinTipo, 3).getNombre());
    }

    @Test
    void conDatosInsuficientesNoSeEvaluaDominio() {
        ventana(true, true);                           // solo 2 intentos previos, 100 %
        assertEquals(TipoError.COGNITIVO, clasificador.clasificar(9L, ejercicio, distractorSinTipo, 1).getNombre());
    }

    @Test
    void sinDominioUsaLaClasificacionDelDistractor() {
        ventana(false, false, false);
        assertEquals(TipoError.HABITO, clasificador.clasificar(9L, ejercicio, distractorHabito, 3).getNombre());
    }
}
