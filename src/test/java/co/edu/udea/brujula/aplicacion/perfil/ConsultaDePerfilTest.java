package co.edu.udea.brujula.aplicacion.perfil;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.apoyo.dobles.CifradorFalso;
import co.edu.udea.brujula.apoyo.dobles.UsuariosEnMemoria;
import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Consulta de perfil")
class ConsultaDePerfilTest {

    private final UsuariosEnMemoria usuarios = new UsuariosEnMemoria();
    private final ConsultaDePerfil consulta = new ConsultaDePerfil(usuarios);

    @Test
    void entrega_los_datos_de_la_cuenta() {
        usuarios.agregar(Datos.estudiante(1L, "ana@brujula.local", new CifradorFalso().cifrar("Estudiante.2026")));

        Usuario perfil = consulta.consultar(1L);

        assertEquals("ana@brujula.local", perfil.email());
        assertEquals("Ana María Pérez Gómez", perfil.nombreCompleto());
    }

    @Test
    void avisa_cuando_la_cuenta_no_existe() {
        assertThrows(NoEncontrado.class, () -> consulta.consultar(99L));
    }
}
