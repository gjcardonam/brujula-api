package co.edu.udea.brujula.aplicacion.perfil;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.apoyo.dobles.CifradorFalso;
import co.edu.udea.brujula.apoyo.dobles.RelojFijo;
import co.edu.udea.brujula.apoyo.dobles.UsuariosEnMemoria;
import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Cambio de contraseña")
class CambioDeContrasenaTest {

    private static final String ACTUAL = "Estudiante.2026";
    private static final String NUEVA = "Nueva.Clave1";

    private final UsuariosEnMemoria usuarios = new UsuariosEnMemoria();
    private final CifradorFalso cifrador = new CifradorFalso();
    private final RelojFijo reloj = new RelojFijo();
    private final CambioDeContrasena cambio = new CambioDeContrasena(usuarios, cifrador, reloj);

    private Usuario usuario;

    @BeforeEach
    void prepararLaCuenta() {
        usuario = usuarios.agregar(Datos.estudiante(1L, "ana@brujula.local", cifrador.cifrar(ACTUAL)));
    }

    @Test
    void cambia_la_contrasena_y_deja_sin_efecto_las_sesiones_anteriores() {
        reloj.avanzar(Duration.ofMinutes(5));

        cambio.cambiar(1L, ACTUAL, NUEVA, NUEVA);

        assertEquals(cifrador.cifrar(NUEVA), usuario.passwordHash());
        assertTrue(usuario.sesionAnteriorAlUltimoCambioDePassword(Datos.AHORA));
    }

    @Test
    void exige_la_contrasena_actual_correcta() {
        DatosInvalidos error = assertThrows(DatosInvalidos.class,
                () -> cambio.cambiar(1L, "Otra.Clave1", NUEVA, NUEVA));

        assertEquals("PASSWORD_ACTUAL_INCORRECTA", error.codigo());
        assertEquals(cifrador.cifrar(ACTUAL), usuario.passwordHash());
    }

    @Test
    void rechaza_la_nueva_contrasena_cuando_no_cumple_las_reglas_o_no_coincide() {
        assertThrows(DatosInvalidos.class, () -> cambio.cambiar(1L, ACTUAL, "corta", "corta"));
        assertThrows(DatosInvalidos.class, () -> cambio.cambiar(1L, ACTUAL, NUEVA, "Otra.Clave1"));
        assertEquals(cifrador.cifrar(ACTUAL), usuario.passwordHash());
    }
}
