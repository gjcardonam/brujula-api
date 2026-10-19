package co.edu.udea.brujula.dominio.servicio;
import co.edu.udea.brujula.dominio.modelo.TipoError;
import co.edu.udea.brujula.dominio.modelo.consulta.ErrorRegistrado;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class DistribucionDeErroresTest {
    @Test void sinIntentosNoGeneraOrientacion() {
        var r = DistribucionDeErrores.calcular(List.of(), null);
        assertEquals(0, r.totalIncorrectos()); assertNull(r.mensajeOrientador());
        assertTrue(r.distribucion().stream().allMatch(d -> d.porcentaje().signum() == 0));
    }
    @Test void empateNoEsPredominio() {
        var r = DistribucionDeErrores.calcular(List.of(error(1L, TipoError.COGNITIVO), error(1L, TipoError.HABITO)), null);
        assertNull(r.tipoPredominante()); assertNull(r.mensajeOrientador());
        assertEquals(50, r.distribucion().getFirst().porcentaje().intValue());
    }
    @Test void filtraComponenteYConservaOpcionesDelFiltro() {
        var r = DistribucionDeErrores.calcular(List.of(error(1L, TipoError.COGNITIVO), error(2L, TipoError.ANSIEDAD)), 2L);
        assertEquals(1, r.totalIncorrectos()); assertEquals(2, r.componentesConIntentos().size());
        assertEquals(TipoError.ANSIEDAD, r.tipoPredominante()); assertNotNull(r.mensajeOrientador());
    }
    @Test void erroresAntiguosNoSeReclasificanNiDiluyenPorcentajes() {
        var r = DistribucionDeErrores.calcular(List.of(error(1L, null), error(1L, TipoError.HABITO)), null);
        assertEquals(2, r.totalIncorrectos()); assertEquals(1, r.sinClasificar());
        assertEquals(TipoError.HABITO, r.tipoPredominante());
        assertEquals(100, r.distribucion().get(1).porcentaje().intValue());
    }
    @Test void componenteSinErroresDevuelveCeros() {
        var r = DistribucionDeErrores.calcular(List.of(error(1L, TipoError.COGNITIVO)), 2L);
        assertEquals(0, r.totalIncorrectos()); assertNull(r.tipoPredominante());
    }
    private ErrorRegistrado error(Long id, String tipo) { return new ErrorRegistrado(id, "Componente " + id, tipo); }
}
