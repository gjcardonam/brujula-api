package co.edu.udea.brujula.aplicacion.banco;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.apoyo.dobles.EjerciciosEnMemoria;
import co.edu.udea.brujula.dominio.modelo.consulta.ComponentesDelBanco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Consulta de componentes del banco")
class ConsultaDeComponentesDelBancoTest {

    private final EjerciciosEnMemoria ejercicios = new EjerciciosEnMemoria();
    private final ConsultaDeComponentesDelBanco consulta = new ConsultaDeComponentesDelBanco(ejercicios);

    @Test
    void el_estudiante_solo_cuenta_los_ejercicios_activos() {
        ejercicios.agregar(Datos.ejercicioActivo(1L));
        ejercicios.agregar(Datos.ejercicioDesactivado(2L));

        ComponentesDelBanco componentes = consulta.componentes(false);

        assertEquals(1, componentes.total());
        assertEquals(1, componentes.componentes().get(0).cantidad());
    }

    @Test
    void el_administrador_cuenta_activos_y_desactivados() {
        ejercicios.agregar(Datos.ejercicioActivo(1L));
        ejercicios.agregar(Datos.ejercicioDesactivado(2L));

        ComponentesDelBanco componentes = consulta.componentes(true);

        assertEquals(2, componentes.total());
        assertEquals(2, componentes.componentes().get(0).cantidad());
    }

    @Test
    void no_devuelve_componentes_cuando_el_banco_esta_vacio() {
        ComponentesDelBanco componentes = consulta.componentes(false);

        assertEquals(0, componentes.total());
        assertTrue(componentes.componentes().isEmpty());
    }
}
