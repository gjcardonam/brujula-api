package co.edu.udea.brujula.aplicacion.acceso;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.apoyo.dobles.CifradorFalso;
import co.edu.udea.brujula.apoyo.dobles.ParametrosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.RelojFijo;
import co.edu.udea.brujula.apoyo.dobles.TokensFalsos;
import co.edu.udea.brujula.apoyo.dobles.UsuariosEnMemoria;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Inicio de sesión")
class InicioDeSesionTest {

    private static final String CORREO = "ana@brujula.local";
    private static final String CLAVE = "Estudiante.2026";

    private final UsuariosEnMemoria usuarios = new UsuariosEnMemoria();
    private final CifradorFalso cifrador = new CifradorFalso();
    private final RelojFijo reloj = new RelojFijo();
    private final ParametrosEnMemoria parametros = new ParametrosEnMemoria()
            .con(ParametrosDelSistema.MAX_INTENTOS_LOGIN, 5)
            .con(ParametrosDelSistema.MINUTOS_BLOQUEO_LOGIN, 10);
    private InicioDeSesion inicioDeSesion;
    private Usuario usuario;

    @BeforeEach
    void prepararLaCuenta() {
        usuario = usuarios.agregar(Datos.estudiante(1L, CORREO, cifrador.cifrar(CLAVE)));
        inicioDeSesion = new InicioDeSesion(usuarios, cifrador, new TokensFalsos(reloj), parametros, reloj);
    }

    @Test
    void entrega_una_sesion_y_registra_el_ultimo_ingreso_con_credenciales_validas() {
        SesionIniciada sesion = inicioDeSesion.iniciar(CORREO, CLAVE);

        assertNotNull(sesion.token());
        assertEquals(reloj.ahora(), usuario.ultimoLoginEn());
    }

    @Test
    void rechaza_con_el_mismo_error_un_correo_inexistente_y_una_clave_equivocada() {
        assertThrows(CredencialesInvalidas.class, () -> inicioDeSesion.iniciar("otra@brujula.local", CLAVE));
        assertThrows(CredencialesInvalidas.class, () -> inicioDeSesion.iniciar(CORREO, "Otra.Clave1"));
    }

    @Test
    void exige_el_correo_y_la_contrasena() {
        assertThrows(DatosInvalidos.class, () -> inicioDeSesion.iniciar("  ", ""));
    }

    @Test
    void bloquea_la_cuenta_al_quinto_intento_fallido() {
        for (int intento = 1; intento <= 4; intento++) {
            assertThrows(CredencialesInvalidas.class, () -> inicioDeSesion.iniciar(CORREO, "Mala.Clave1"));
        }
        assertThrows(CuentaBloqueada.class, () -> inicioDeSesion.iniciar(CORREO, "Mala.Clave1"));
        assertEquals(reloj.ahora().plus(Duration.ofMinutes(10)), usuario.bloqueadoHasta());
    }

    @Test
    void rechaza_la_clave_correcta_mientras_dure_el_bloqueo() {
        agotarLosIntentos();

        reloj.avanzar(Duration.ofMinutes(9));

        assertThrows(CuentaBloqueada.class, () -> inicioDeSesion.iniciar(CORREO, CLAVE));
    }

    @Test
    void deja_entrar_y_reinicia_el_contador_cuando_pasan_los_diez_minutos() {
        agotarLosIntentos();

        reloj.avanzar(Duration.ofMinutes(10));
        SesionIniciada sesion = inicioDeSesion.iniciar(CORREO, CLAVE);

        assertNotNull(sesion.token());
        assertEquals(0, usuario.intentosFallidos());
    }

    @Test
    void un_ingreso_exitoso_antes_del_limite_reinicia_el_contador() {
        assertThrows(CredencialesInvalidas.class, () -> inicioDeSesion.iniciar(CORREO, "Mala.Clave1"));
        inicioDeSesion.iniciar(CORREO, CLAVE);

        assertEquals(0, usuario.intentosFallidos());
    }

    private void agotarLosIntentos() {
        for (int intento = 1; intento <= 5; intento++) {
            assertThrows(RuntimeException.class, () -> inicioDeSesion.iniciar(CORREO, "Mala.Clave1"));
        }
    }
}
