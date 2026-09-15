package co.edu.udea.brujula.aplicacion.acceso;

import co.edu.udea.brujula.apoyo.dobles.RelojFijo;
import co.edu.udea.brujula.apoyo.dobles.SesionesEnMemoria;
import co.edu.udea.brujula.dominio.modelo.SesionRevocada;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Depuración de sesiones revocadas")
class DepuracionDeSesionesRevocadasTest {

    private final SesionesEnMemoria sesiones = new SesionesEnMemoria();
    private final RelojFijo reloj = new RelojFijo();
    private final DepuracionDeSesionesRevocadas depuracion = new DepuracionDeSesionesRevocadas(sesiones, reloj);

    @Test
    void borra_solo_los_tokens_revocados_que_ya_vencieron() {
        sesiones.revocar(new SesionRevocada("vencido", 1L, reloj.ahora(), reloj.ahora().minus(Duration.ofHours(1))));
        sesiones.revocar(new SesionRevocada("vigente", 1L, reloj.ahora(), reloj.ahora().plus(Duration.ofHours(1))));

        int borrados = depuracion.depurar();

        assertEquals(1, borrados);
        assertFalse(sesiones.estaRevocada("vencido"));
        assertTrue(sesiones.estaRevocada("vigente"));
    }

    @Test
    void no_borra_nada_cuando_todos_siguen_vigentes() {
        sesiones.revocar(new SesionRevocada("vigente", 1L, reloj.ahora(), reloj.ahora().plus(Duration.ofHours(1))));

        assertEquals(0, depuracion.depurar());
    }
}
