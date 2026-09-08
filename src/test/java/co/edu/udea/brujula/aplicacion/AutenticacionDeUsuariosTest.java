package co.edu.udea.brujula.aplicacion;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.apoyo.dobles.*;
import co.edu.udea.brujula.dominio.excepcion.CredencialesInvalidas;
import co.edu.udea.brujula.dominio.excepcion.CuentaBloqueada;
import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.modelo.SesionIniciada;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.puerto.salida.ParametrosDelSistema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * El caso de uso se prueba con adaptadores en memoria: no hace falta levantar Spring ni la base de
 * datos, que es justamente para lo que sirven los puertos.
 */
@DisplayName("Autenticación de usuarios (HU-002)")
class AutenticacionDeUsuariosTest {

    private static final String CORREO = "ana.perez@correo.com";
    private static final String CLAVE = "Clave.2026";

    private UsuariosEnMemoria usuarios;
    private RelojFijo reloj;
    private AutenticacionDeUsuarios autenticacion;
    private Usuario ana;

    @BeforeEach
    void prepararEscenario() {
        usuarios = new UsuariosEnMemoria();
        reloj = new RelojFijo();
        CifradorFalso cifrador = new CifradorFalso();
        var parametros = new ParametrosEnMemoria()
                .con(ParametrosDelSistema.MAX_INTENTOS_LOGIN, 5)
                .con(ParametrosDelSistema.MINUTOS_BLOQUEO_LOGIN, 10);
        ana = usuarios.agregar(Datos.estudiante(null, CORREO, cifrador.cifrar(CLAVE)));
        autenticacion = new AutenticacionDeUsuarios(usuarios, cifrador, new TokensFalsos(), parametros, reloj);
    }

    @Test
    void entregaUnaSesionYRegistraElUltimoIngreso() {
        SesionIniciada sesion = autenticacion.autenticar(CORREO, CLAVE);

        assertNotNull(sesion.token());
        assertEquals(CORREO, sesion.usuario().email());
        assertEquals(reloj.ahora(), ana.ultimoLogin());
    }

    @Test
    void rechazaLaContrasenaEquivocada() {
        assertThrows(CredencialesInvalidas.class, () -> autenticacion.autenticar(CORREO, "Otra.2026"));
    }

    @Test
    void usaElMismoErrorCuandoElCorreoNiSiquieraExiste() {
        // Si el mensaje fuera distinto se podría averiguar qué correos están registrados (CA-04).
        assertThrows(CredencialesInvalidas.class, () -> autenticacion.autenticar("nadie@correo.com", CLAVE));
    }

    @Test
    void avisaCuandoFaltanDatos() {
        DatosInvalidos error = assertThrows(DatosInvalidos.class, () -> autenticacion.autenticar("", ""));
        assertEquals(2, error.detalles().size());
    }

    @Test
    void bloqueaLaCuentaAlQuintoIntentoFallido() {
        for (int intento = 1; intento <= 4; intento++) {
            assertThrows(CredencialesInvalidas.class, () -> autenticacion.autenticar(CORREO, "mala"));
        }
        assertThrows(CuentaBloqueada.class, () -> autenticacion.autenticar(CORREO, "mala"));
        // Ya bloqueada, ni siquiera la contraseña correcta sirve.
        assertThrows(CuentaBloqueada.class, () -> autenticacion.autenticar(CORREO, CLAVE));
    }

    @Test
    void despuesDeLosDiezMinutosVuelveAPermitirElIngreso() {
        for (int intento = 1; intento <= 5; intento++) {
            assertThrows(RuntimeException.class, () -> autenticacion.autenticar(CORREO, "mala"));
        }
        reloj.avanzar(Duration.ofMinutes(11));

        assertNotNull(autenticacion.autenticar(CORREO, CLAVE).token());
        assertEquals(0, ana.intentosFallidos());
    }

    @Test
    void unIngresoExitosoAntesDelLimiteReiniciaElContador() {
        assertThrows(CredencialesInvalidas.class, () -> autenticacion.autenticar(CORREO, "mala"));
        autenticacion.autenticar(CORREO, CLAVE);

        assertEquals(0, ana.intentosFallidos());
    }
}
