package co.edu.udea.brujula.aplicacion.acceso;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.apoyo.dobles.CifradorFalso;
import co.edu.udea.brujula.apoyo.dobles.RelojFijo;
import co.edu.udea.brujula.apoyo.dobles.TokensDeRecuperacionEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.UsuariosEnMemoria;
import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.modelo.TokenRecuperacion;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Restablecimiento de contraseña")
class RestablecimientoDeContrasenaTest {

    private static final String ENLACE = "token-en-claro";
    private static final String NUEVA = "Nueva.Clave1";

    private final UsuariosEnMemoria usuarios = new UsuariosEnMemoria();
    private final TokensDeRecuperacionEnMemoria tokens = new TokensDeRecuperacionEnMemoria();
    private final CifradorFalso cifrador = new CifradorFalso();
    private final RelojFijo reloj = new RelojFijo();
    private final RestablecimientoDeContrasena restablecimiento =
            new RestablecimientoDeContrasena(usuarios, tokens, cifrador, reloj);

    private Usuario usuario;

    @BeforeEach
    void prepararLaCuentaYElEnlace() {
        usuario = usuarios.agregar(Datos.estudiante(1L, "ana@brujula.local", cifrador.cifrar("Estudiante.2026")));
        tokens.guardar(TokenRecuperacion.nuevo(1L, cifrador.resumen(ENLACE), reloj.ahora(), 30));
        reloj.avanzar(Duration.ofMinutes(1));
    }

    @Test
    void cambia_la_contrasena_marca_el_enlace_como_usado_y_corta_las_sesiones_previas() {
        String mensaje = restablecimiento.restablecer(ENLACE, NUEVA, NUEVA);

        assertNotNull(mensaje);
        assertEquals(cifrador.cifrar(NUEVA), usuario.passwordHash());
        assertEquals(reloj.ahora(), usuario.passwordActualizadoEn());
        assertNotNull(tokens.ultimo().usadoEn());
        assertTrue(usuario.sesionAnteriorAlUltimoCambioDePassword(Datos.AHORA));
    }

    @Test
    void no_deja_usar_el_mismo_enlace_dos_veces() {
        restablecimiento.restablecer(ENLACE, NUEVA, NUEVA);

        assertThrows(DatosInvalidos.class, () -> restablecimiento.restablecer(ENLACE, NUEVA, NUEVA));
    }

    @Test
    void no_cambia_nada_si_la_contrasena_no_cumple_las_reglas() {
        assertThrows(DatosInvalidos.class, () -> restablecimiento.restablecer(ENLACE, "corta", "corta"));

        assertEquals(cifrador.cifrar("Estudiante.2026"), usuario.passwordHash());
    }

    @Test
    void no_cambia_nada_con_un_enlace_vencido() {
        reloj.avanzar(Duration.ofMinutes(30));

        assertThrows(DatosInvalidos.class, () -> restablecimiento.restablecer(ENLACE, NUEVA, NUEVA));
    }
}
