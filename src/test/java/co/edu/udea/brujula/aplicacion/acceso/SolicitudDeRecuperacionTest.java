package co.edu.udea.brujula.aplicacion.acceso;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.apoyo.dobles.CifradorFalso;
import co.edu.udea.brujula.apoyo.dobles.CorreoFalso;
import co.edu.udea.brujula.apoyo.dobles.ParametrosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.RelojFijo;
import co.edu.udea.brujula.apoyo.dobles.TokensDeRecuperacionEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.UsuariosEnMemoria;
import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.modelo.TokenRecuperacion;
import co.edu.udea.brujula.dominio.puerto.salida.ParametrosDelSistema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Solicitud de recuperación")
class SolicitudDeRecuperacionTest {

    private static final String CORREO = "ana@brujula.local";

    private final UsuariosEnMemoria usuarios = new UsuariosEnMemoria();
    private final TokensDeRecuperacionEnMemoria tokens = new TokensDeRecuperacionEnMemoria();
    private final CifradorFalso cifrador = new CifradorFalso();
    private final CorreoFalso correo = new CorreoFalso();
    private final RelojFijo reloj = new RelojFijo();
    private final ParametrosEnMemoria parametros = new ParametrosEnMemoria()
            .con(ParametrosDelSistema.MINUTOS_VIGENCIA_RECUPERACION, 30)
            .con(ParametrosDelSistema.MAX_SOLICITUDES_RECUPERACION_HORA, 3);
    private SolicitudDeRecuperacion solicitud;

    @BeforeEach
    void prepararLaCuenta() {
        usuarios.agregar(Datos.estudiante(1L, CORREO, cifrador.cifrar("Estudiante.2026")));
        solicitud = new SolicitudDeRecuperacion(usuarios, tokens, cifrador, correo, parametros, reloj);
    }

    @Test
    void envia_un_enlace_vigente_por_treinta_minutos_y_guarda_solo_su_resumen() {
        String mensaje = solicitud.solicitar(CORREO);

        assertEquals(SolicitudDeRecuperacion.RESPUESTA_NEUTRA, mensaje);
        assertEquals(1, correo.enviados().size());
        assertEquals(30, correo.enviados().get(0).minutosDeVigencia());
        TokenRecuperacion guardado = tokens.ultimo();
        assertEquals(reloj.ahora().plus(Duration.ofMinutes(30)), guardado.expiraEn());
        assertEquals(cifrador.resumen(correo.enviados().get(0).token()), guardado.hash());
    }

    @Test
    void responde_lo_mismo_cuando_el_correo_no_existe_y_no_envia_nada() {
        String mensaje = solicitud.solicitar("desconocido@brujula.local");

        assertEquals(SolicitudDeRecuperacion.RESPUESTA_NEUTRA, mensaje);
        assertTrue(correo.enviados().isEmpty());
    }

    @Test
    void admite_tres_solicitudes_por_hora_y_calla_la_cuarta() {
        solicitud.solicitar(CORREO);
        solicitud.solicitar(CORREO);
        solicitud.solicitar(CORREO);

        assertEquals(SolicitudDeRecuperacion.RESPUESTA_NEUTRA, solicitud.solicitar(CORREO));
        assertEquals(3, correo.enviados().size());
    }

    @Test
    void vuelve_a_enviar_cuando_pasa_la_hora() {
        solicitud.solicitar(CORREO);
        solicitud.solicitar(CORREO);
        solicitud.solicitar(CORREO);

        reloj.avanzar(Duration.ofHours(1).plusMinutes(1));
        solicitud.solicitar(CORREO);

        assertEquals(4, correo.enviados().size());
    }

    @Test
    void exige_el_correo() {
        assertThrows(DatosInvalidos.class, () -> solicitud.solicitar(" "));
    }
}
