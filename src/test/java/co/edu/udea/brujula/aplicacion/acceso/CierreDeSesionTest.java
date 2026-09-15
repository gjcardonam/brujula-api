package co.edu.udea.brujula.aplicacion.acceso;

import co.edu.udea.brujula.apoyo.dobles.RelojFijo;
import co.edu.udea.brujula.apoyo.dobles.SesionesEnMemoria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Cierre de sesión")
class CierreDeSesionTest {

    private final SesionesEnMemoria sesiones = new SesionesEnMemoria();
    private final RelojFijo reloj = new RelojFijo();
    private final CierreDeSesion cierreDeSesion = new CierreDeSesion(sesiones, reloj);

    @Test
    void revoca_el_token_de_la_sesion() {
        Instant expira = reloj.ahora().plus(Duration.ofHours(2));

        cierreDeSesion.cerrar(1L, "jti-1", expira);

        assertTrue(sesiones.estaRevocada("jti-1"));
        assertEquals(expira, sesiones.todas().get("jti-1").expiraEn());
    }

    @Test
    void cerrar_dos_veces_no_duplica_la_revocacion() {
        cierreDeSesion.cerrar(1L, "jti-1", reloj.ahora().plus(Duration.ofHours(2)));
        cierreDeSesion.cerrar(1L, "jti-1", reloj.ahora().plus(Duration.ofHours(2)));

        assertEquals(1, sesiones.todas().size());
    }

    @Test
    void sin_fecha_de_expiracion_supone_una_vigencia_para_poder_limpiar_despues() {
        cierreDeSesion.cerrar(1L, "jti-1", null);

        assertEquals(reloj.ahora().plus(Duration.ofHours(3)), sesiones.todas().get("jti-1").expiraEn());
    }
}
