package co.edu.udea.brujula.aplicacion.acceso;

import co.edu.udea.brujula.apoyo.dobles.CifradorFalso;
import co.edu.udea.brujula.apoyo.dobles.RelojFijo;
import co.edu.udea.brujula.apoyo.dobles.TokensDeRecuperacionEnMemoria;
import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.modelo.TokenRecuperacion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Verificación del enlace de recuperación")
class VerificacionDelEnlaceDeRecuperacionTest {

    private static final String ENLACE = "token-en-claro";

    private final TokensDeRecuperacionEnMemoria tokens = new TokensDeRecuperacionEnMemoria();
    private final CifradorFalso cifrador = new CifradorFalso();
    private final RelojFijo reloj = new RelojFijo();
    private final VerificacionDelEnlaceDeRecuperacion verificacion =
            new VerificacionDelEnlaceDeRecuperacion(tokens, cifrador, reloj);

    private TokenRecuperacion guardarEnlace() {
        return tokens.guardar(TokenRecuperacion.nuevo(1L, cifrador.resumen(ENLACE), reloj.ahora(), 30));
    }

    @Test
    void acepta_un_enlace_vigente() {
        guardarEnlace();

        assertDoesNotThrow(() -> verificacion.verificar(ENLACE));
    }

    @Test
    void rechaza_un_enlace_vencido() {
        guardarEnlace();
        reloj.avanzar(Duration.ofMinutes(31));

        DatosInvalidos error = assertThrows(DatosInvalidos.class, () -> verificacion.verificar(ENLACE));

        assertEquals("ENLACE_INVALIDO", error.codigo());
    }

    @Test
    void rechaza_un_enlace_ya_usado() {
        tokens.guardar(guardarEnlace().marcarUsado(reloj.ahora()));

        assertThrows(DatosInvalidos.class, () -> verificacion.verificar(ENLACE));
    }

    @Test
    void rechaza_un_enlace_inexistente_o_vacio() {
        assertThrows(DatosInvalidos.class, () -> verificacion.verificar("cualquier-cosa"));
        assertThrows(DatosInvalidos.class, () -> verificacion.verificar(" "));
    }
}
