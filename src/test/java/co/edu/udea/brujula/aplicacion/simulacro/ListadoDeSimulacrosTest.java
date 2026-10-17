package co.edu.udea.brujula.aplicacion.simulacro;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.apoyo.dobles.IntentosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.ParametrosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.RelojFijo;
import co.edu.udea.brujula.apoyo.dobles.SimulacrosEnMemoria;
import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.Simulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.ResumenDeSimulacro;
import co.edu.udea.brujula.dominio.servicio.CierreDeSimulacros;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Listado de los simulacros finalizados")
class ListadoDeSimulacrosTest {

    private static final Long ESTUDIANTE = 7L;

    private final SimulacrosEnMemoria simulacros = new SimulacrosEnMemoria();
    private final IntentosEnMemoria intentos = new IntentosEnMemoria();
    private final RelojFijo reloj = new RelojFijo();
    private final CierreDeSimulacros cierre = new CierreDeSimulacros(simulacros, intentos, new ParametrosEnMemoria());
    private final ListadoDeSimulacros listado = new ListadoDeSimulacros(simulacros, intentos, cierre, reloj);

    @Test
    void lista_solo_los_finalizados_propios_con_su_resumen() {
        Simulacro propio = simulacros.guardar(Simulacro.iniciar(ESTUDIANTE, Datos.TREINTA_MINUTOS, reloj.ahora()));
        intentos.guardar(Datos.intento(ESTUDIANTE, propio.id(), Datos.ALGEBRA, Datos.INTERPRETACION, true, 3));
        intentos.guardar(Datos.intento(ESTUDIANTE, propio.id(), Datos.ALGEBRA, Datos.INTERPRETACION, false, 3));
        cierre.cerrar(propio, reloj.ahora().plus(Duration.ofMinutes(3)));
        simulacros.guardar(Simulacro.iniciar(ESTUDIANTE, Datos.TREINTA_MINUTOS, reloj.ahora()));
        Simulacro ajeno = simulacros.guardar(Simulacro.iniciar(8L, Datos.TREINTA_MINUTOS, reloj.ahora()));
        cierre.cerrar(ajeno, reloj.ahora());

        Pagina<ResumenDeSimulacro> pagina = listado.listar(ESTUDIANTE, 0);

        assertEquals(1, pagina.totalElementos());
        ResumenDeSimulacro resumen = pagina.contenido().get(0);
        assertEquals(propio.id(), resumen.id());
        assertEquals(2, resumen.respondidos());
        assertEquals(new BigDecimal("50.00"), resumen.porcentajeAciertos());
        assertEquals(180, resumen.tiempoUtilizadoSeg());
    }

    @Test
    void incluye_los_que_se_vencieron_sin_que_nadie_los_cerrara() {
        simulacros.guardar(Simulacro.iniciar(ESTUDIANTE, Datos.TREINTA_MINUTOS, reloj.ahora()));
        reloj.avanzar(Duration.ofMinutes(31));

        assertEquals(1, listado.listar(ESTUDIANTE, 0).totalElementos());
    }
}
