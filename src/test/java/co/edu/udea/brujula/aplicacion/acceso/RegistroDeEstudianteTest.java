package co.edu.udea.brujula.aplicacion.acceso;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.apoyo.dobles.CatalogosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.CifradorFalso;
import co.edu.udea.brujula.apoyo.dobles.RelojFijo;
import co.edu.udea.brujula.apoyo.dobles.TokensFalsos;
import co.edu.udea.brujula.apoyo.dobles.UsuariosEnMemoria;
import co.edu.udea.brujula.dominio.excepcion.Conflicto;
import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.modelo.Rol;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.puerto.entrada.RegistrarEstudiante;
import co.edu.udea.brujula.dominio.puerto.salida.ProveedorDeTokens;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Registro de estudiante")
class RegistroDeEstudianteTest {

    private static final String CORREO = "ana@brujula.local";
    private static final String CLAVE = "Estudiante.2026";

    private final UsuariosEnMemoria usuarios = new UsuariosEnMemoria();
    private final CifradorFalso cifrador = new CifradorFalso();
    private final RelojFijo reloj = new RelojFijo();
    private final TokensFalsos tokens = new TokensFalsos(reloj);
    private final RegistroDeEstudiante registro =
            new RegistroDeEstudiante(usuarios, new CatalogosEnMemoria(), tokens, cifrador, reloj);

    private String tokenDeRegistro() {
        return tokens.emitirRegistro(new ProveedorDeTokens.RegistroPendiente("google-1", CORREO, "Ana", "Pérez"));
    }

    private RegistrarEstudiante.Datos datos(String nombre, String apellido, String clave, String confirmacion,
                                            Boolean terminos) {
        return new RegistrarEstudiante.Datos(tokenDeRegistro(), nombre, apellido, clave, confirmacion, terminos);
    }

    @Test
    void crea_la_cuenta_con_rol_estudiante_y_la_contrasena_cifrada() {
        Usuario creado = registro.registrar(datos("Ana María", "Pérez Gómez", CLAVE, CLAVE, true));

        assertEquals(Rol.ESTUDIANTE, creado.rol().nombre());
        assertEquals(CORREO, creado.email());
        assertNotEquals(CLAVE, creado.passwordHash());
        assertEquals(reloj.ahora(), creado.terminosAceptadosEn());
    }

    @Test
    void no_registra_si_el_token_de_google_expiro() {
        RegistrarEstudiante.Datos vencidos = new RegistrarEstudiante.Datos("registro-viejo", "Ana", "Pérez",
                CLAVE, CLAVE, true);

        DatosInvalidos error = assertThrows(DatosInvalidos.class, () -> registro.registrar(vencidos));

        assertEquals("REGISTRO_EXPIRADO", error.codigo());
    }

    @Test
    void no_registra_si_no_se_aceptan_los_terminos() {
        assertThrows(DatosInvalidos.class, () -> registro.registrar(datos("Ana", "Pérez", CLAVE, CLAVE, false)));
    }

    @Test
    void no_registra_si_la_confirmacion_no_coincide_o_los_nombres_no_cumplen() {
        assertThrows(DatosInvalidos.class, () -> registro.registrar(datos("Ana", "Pérez", CLAVE, "Otra.1", true)));
        assertThrows(DatosInvalidos.class, () -> registro.registrar(datos("", "P", CLAVE, CLAVE, true)));
    }

    @Test
    void no_crea_una_segunda_cuenta_para_el_mismo_correo() {
        usuarios.agregar(Datos.estudiante(1L, CORREO, cifrador.cifrar(CLAVE)));

        Conflicto error = assertThrows(Conflicto.class,
                () -> registro.registrar(datos("Ana", "Pérez", CLAVE, CLAVE, true)));

        assertEquals("CUENTA_EXISTENTE", error.codigo());
    }
}
