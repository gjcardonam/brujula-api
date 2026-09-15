package co.edu.udea.brujula.aplicacion.perfil;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.apoyo.dobles.CifradorFalso;
import co.edu.udea.brujula.apoyo.dobles.UsuariosEnMemoria;
import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Actualización del perfil")
class ActualizacionDelPerfilTest {

    private final UsuariosEnMemoria usuarios = new UsuariosEnMemoria();
    private final ActualizacionDelPerfil actualizacion = new ActualizacionDelPerfil(usuarios);

    @BeforeEach
    void prepararLaCuenta() {
        usuarios.agregar(Datos.estudiante(1L, "ana@brujula.local", new CifradorFalso().cifrar("Estudiante.2026")));
    }

    @Test
    void cambia_nombre_y_apellido_sin_tocar_el_correo() {
        Usuario actualizado = actualizacion.actualizar(1L, "  Ana Lucía ", " Restrepo ");

        assertEquals("Ana Lucía", actualizado.nombre());
        assertEquals("Restrepo", actualizado.apellido());
        assertEquals("ana@brujula.local", actualizado.email());
    }

    @Test
    void rechaza_un_nombre_vacio_o_un_apellido_demasiado_corto() {
        assertThrows(DatosInvalidos.class, () -> actualizacion.actualizar(1L, " ", "Restrepo"));
        assertThrows(DatosInvalidos.class, () -> actualizacion.actualizar(1L, "Ana", "R"));
    }

    @Test
    void avisa_cuando_la_cuenta_no_existe() {
        assertThrows(NoEncontrado.class, () -> actualizacion.actualizar(99L, "Ana", "Restrepo"));
    }
}
