package co.edu.udea.brujula.aplicacion.acceso;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.apoyo.dobles.CifradorFalso;
import co.edu.udea.brujula.apoyo.dobles.GoogleFalso;
import co.edu.udea.brujula.apoyo.dobles.RelojFijo;
import co.edu.udea.brujula.apoyo.dobles.TokensFalsos;
import co.edu.udea.brujula.apoyo.dobles.UsuariosEnMemoria;
import co.edu.udea.brujula.dominio.excepcion.Conflicto;
import co.edu.udea.brujula.dominio.excepcion.ServicioNoDisponible;
import co.edu.udea.brujula.dominio.puerto.entrada.RegistrarConGoogle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Registro con Google")
class RegistroConGoogleTest {

    private static final String CORREO = "ana@brujula.local";

    private final UsuariosEnMemoria usuarios = new UsuariosEnMemoria();
    private final GoogleFalso google = new GoogleFalso();
    private final RegistroConGoogle registroConGoogle =
            new RegistroConGoogle(usuarios, google, new TokensFalsos(new RelojFijo()));

    @Test
    void entrega_un_token_de_registro_cuando_el_correo_todavia_no_tiene_cuenta() {
        RegistrarConGoogle.RegistroPendiente pendiente = registroConGoogle.iniciar(CORREO);

        assertNotNull(pendiente.registroToken());
        assertEquals(CORREO, pendiente.email());
        assertTrue(pendiente.simulado());
    }

    @Test
    void avisa_que_la_cuenta_ya_existe_en_vez_de_crear_una_segunda() {
        usuarios.agregar(Datos.estudiante(1L, CORREO, new CifradorFalso().cifrar("Estudiante.2026")));

        Conflicto error = assertThrows(Conflicto.class, () -> registroConGoogle.iniciar(CORREO));

        assertEquals("CUENTA_EXISTENTE", error.codigo());
    }

    @Test
    void propaga_la_falla_de_google_sin_crear_ninguna_cuenta() {
        google.caerse();

        assertThrows(ServicioNoDisponible.class, () -> registroConGoogle.iniciar(CORREO));
    }
}
