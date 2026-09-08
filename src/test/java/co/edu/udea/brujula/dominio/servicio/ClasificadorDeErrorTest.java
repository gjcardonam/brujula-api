package co.edu.udea.brujula.dominio.servicio;

import co.edu.udea.brujula.dominio.modelo.Opcion;
import co.edu.udea.brujula.dominio.modelo.TipoError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * El motor es una función pura: recibe la ventana de resultados y devuelve el tipo de error, así
 * que se puede probar sin base de datos ni Spring.
 */
@DisplayName("Motor de clasificación de errores (HU-027 y HU-028)")
class ClasificadorDeErrorTest {

    private static final ClasificadorDeError.Ventana VENTANA = new ClasificadorDeError.Ventana(5, 3, 70);

    private final Opcion sinClasificar = opcion(null);
    private final Opcion clasificadaComoHabito = opcion(new TipoError(2L, TipoError.HABITO));

    private static Opcion opcion(TipoError tipo) {
        return new Opcion(1L, "B. 95.000", null, false, "Restaste el 20 % de una sola camiseta.", tipo, 2);
    }

    @Test
    void confianzaAltaConRespuestaIncorrectaEsAnsiedad() {
        List<Boolean> venia = List.of(true, true, true, true, true);
        assertEquals(TipoError.ANSIEDAD, ClasificadorDeError.clasificar(4, sinClasificar, venia, VENTANA));
        assertEquals(TipoError.ANSIEDAD, ClasificadorDeError.clasificar(5, sinClasificar, venia, VENTANA));
    }

    @Test
    void fallarLoQueYaDominabaConConfianzaBajaEsHabito() {
        List<Boolean> venia = List.of(true, true, true, false, true);   // 80 % en la ventana
        assertEquals(TipoError.HABITO, ClasificadorDeError.clasificar(2, sinClasificar, venia, VENTANA));
    }

    @Test
    void sinDominioNiClasificacionDelDistractorQuedaComoCognitivo() {
        List<Boolean> venia = List.of(true, false, false, true, false);   // 40 %
        assertEquals(TipoError.COGNITIVO, ClasificadorDeError.clasificar(3, sinClasificar, venia, VENTANA));
    }

    @Test
    void conMenosDeTresIntentosPreviosNoSeEvaluaElDominio() {
        List<Boolean> venia = List.of(true, true);   // acertó todo, pero son muy pocos datos
        assertEquals(TipoError.COGNITIVO, ClasificadorDeError.clasificar(1, sinClasificar, venia, VENTANA));
    }

    @Test
    void sinDominioSeRespetaLaClasificacionDelDistractor() {
        List<Boolean> venia = List.of(false, false, false);
        assertEquals(TipoError.HABITO, ClasificadorDeError.clasificar(3, clasificadaComoHabito, venia, VENTANA));
    }

    @Test
    void laVentanaSoloMiraLosUltimosIntentos() {
        // Los tres más recientes son fallos; los viejos, aunque sean aciertos, quedan por fuera.
        List<Boolean> venia = List.of(false, false, false, false, false, true, true, true, true, true);
        assertFalse(ClasificadorDeError.dominaElArea(venia, VENTANA));
    }

    @Test
    void elUmbralDeDominioEsInclusivo() {
        List<Boolean> justoEnElUmbral = List.of(true, true, true, false);   // 75 %
        assertTrue(ClasificadorDeError.dominaElArea(justoEnElUmbral, VENTANA));
    }
}
