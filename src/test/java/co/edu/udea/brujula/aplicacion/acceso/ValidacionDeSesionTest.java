package co.edu.udea.brujula.aplicacion.acceso;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.apoyo.dobles.CifradorFalso;
import co.edu.udea.brujula.apoyo.dobles.RelojFijo;
import co.edu.udea.brujula.apoyo.dobles.SesionesEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.TokensFalsos;
import co.edu.udea.brujula.apoyo.dobles.UsuariosEnMemoria;
import co.edu.udea.brujula.dominio.modelo.SesionRevocada;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.puerto.entrada.ValidarSesion;
import co.edu.udea.brujula.dominio.puerto.salida.ProveedorDeTokens;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Validación de sesión")
class ValidacionDeSesionTest {

    private final SesionesEnMemoria sesiones = new SesionesEnMemoria();
    private final UsuariosEnMemoria usuarios = new UsuariosEnMemoria();
    private final RelojFijo reloj = new RelojFijo();
    private final TokensFalsos tokens = new TokensFalsos(reloj);
    private final ValidacionDeSesion validacion = new ValidacionDeSesion(sesiones, usuarios, tokens);

    private Usuario usuario;
    private ProveedorDeTokens.Sesion sesion;

    @BeforeEach
    void abrirUnaSesion() {
        usuario = usuarios.agregar(Datos.estudiante(1L, "ana@brujula.local",
                new CifradorFalso().cifrar("Estudiante.2026")));
        sesion = tokens.emitirSesion(usuario);
    }

    @Test
    void reconoce_al_usuario_de_un_token_valido() {
        Optional<ValidarSesion.Autenticado> autenticado = validacion.validar(sesion.token());

        assertTrue(autenticado.isPresent());
        assertEquals("ana@brujula.local", autenticado.get().email());
        assertFalse(autenticado.get().esAdministrador());
    }

    @Test
    void rechaza_un_token_desconocido() {
        assertTrue(validacion.validar("token-inventado").isEmpty());
    }

    @Test
    void rechaza_un_token_revocado() {
        sesiones.revocar(new SesionRevocada(sesion.jti(), 1L, reloj.ahora(), sesion.expiraEn()));

        assertTrue(validacion.validar(sesion.token()).isEmpty());
    }

    @Test
    void rechaza_la_sesion_abierta_antes_del_ultimo_cambio_de_contrasena() {
        reloj.avanzar(Duration.ofMinutes(5));
        usuario.cambiarPassword("hash:Nueva.Clave1", reloj.ahora());

        assertTrue(validacion.validar(sesion.token()).isEmpty());
    }
}
