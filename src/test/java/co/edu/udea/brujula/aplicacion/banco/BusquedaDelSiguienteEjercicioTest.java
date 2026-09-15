package co.edu.udea.brujula.aplicacion.banco;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.apoyo.dobles.EjerciciosEnMemoria;
import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.BuscarSiguienteEjercicio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Búsqueda del siguiente ejercicio")
class BusquedaDelSiguienteEjercicioTest {

    private final EjerciciosEnMemoria ejercicios = new EjerciciosEnMemoria();
    private final BusquedaDelSiguienteEjercicio busqueda = new BusquedaDelSiguienteEjercicio(ejercicios);

    @Test
    void entrega_otro_ejercicio_activo_distinto_del_actual() {
        ejercicios.agregar(Datos.ejercicioActivo(1L));
        ejercicios.agregar(Datos.ejercicioActivo(2L));

        BuscarSiguienteEjercicio.Siguiente siguiente = busqueda.buscar(7L, 1L, null);

        assertTrue(siguiente.hayMas());
        assertEquals(2L, siguiente.idEjercicio());
    }

    @Test
    void conserva_el_componente_del_filtro() {
        ejercicios.agregar(Datos.ejercicioActivo(1L));
        ejercicios.agregar(new Ejercicio(2L, 2, "Otro enunciado", null, Datos.BASICO, Datos.GEOMETRIA,
                Datos.INTERPRETACION, Ejercicio.ACTIVO, Datos.AHORA, 9L, "Carolina Gómez", List.of()));

        BuscarSiguienteEjercicio.Siguiente siguiente = busqueda.buscar(7L, 1L, Datos.GEOMETRIA.id());

        assertEquals(2L, siguiente.idEjercicio());
    }

    @Test
    void avisa_cuando_no_quedan_ejercicios_del_componente() {
        ejercicios.agregar(Datos.ejercicioActivo(1L));

        BuscarSiguienteEjercicio.Siguiente siguiente = busqueda.buscar(7L, 1L, Datos.GEOMETRIA.id());

        assertFalse(siguiente.hayMas());
        assertNotNull(siguiente.mensaje());
        assertTrue(siguiente.mensaje().contains("componente"));
    }

    @Test
    void avisa_cuando_no_quedan_ejercicios_en_todo_el_banco() {
        ejercicios.agregar(Datos.ejercicioActivo(1L));

        BuscarSiguienteEjercicio.Siguiente siguiente = busqueda.buscar(7L, 1L, null);

        assertFalse(siguiente.hayMas());
        assertNotNull(siguiente.mensaje());
    }
}
