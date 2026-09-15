package co.edu.udea.brujula.aplicacion.acceso;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.apoyo.dobles.CifradorFalso;
import co.edu.udea.brujula.apoyo.dobles.RelojFijo;
import co.edu.udea.brujula.apoyo.dobles.SesionesEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.TokensFalsos;
import co.edu.udea.brujula.apoyo.dobles.UsuariosEnMemoria;
import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.modelo.SesionIniciada;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Renovación de sesión")
class RenovacionDeSesionTest {

    private final SesionesEnMemoria sesiones = new SesionesEnMemoria();
    private final UsuariosEnMemoria usuarios = new UsuariosEnMemoria();
    private final RelojFijo reloj = new RelojFijo();
    private final RenovacionDeSesion renovacion =
            new RenovacionDeSesion(sesiones, usuarios, new TokensFalsos(reloj), reloj);

    @Test
    void entrega_un_token_nuevo_y_revoca_el_anterior() {
        usuarios.agregar(Datos.estudiante(1L, "ana@brujula.local", new CifradorFalso().cifrar("Estudiante.2026")));

        SesionIniciada nueva = renovacion.renovar(1L, "jti-anterior", reloj.ahora().plus(Duration.ofHours(2)));

        assertNotNull(nueva.token());
        assertTrue(sesiones.estaRevocada("jti-anterior"));
        assertEquals("ana@brujula.local", nueva.usuario().email());
    }

    @Test
    void no_renueva_la_sesion_de_una_cuenta_que_no_existe() {
        assertThrows(NoEncontrado.class, () -> renovacion.renovar(99L, "jti-1", reloj.ahora()));
    }
}
