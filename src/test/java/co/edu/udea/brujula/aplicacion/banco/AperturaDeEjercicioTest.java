package co.edu.udea.brujula.aplicacion.banco;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.apoyo.dobles.EjerciciosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.IntentosEnMemoria;
import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.excepcion.RecursoNoDisponible;
import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.modelo.Intento;
import co.edu.udea.brujula.dominio.puerto.entrada.AbrirEjercicio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Apertura de un ejercicio para practicar")
class AperturaDeEjercicioTest {

    private final EjerciciosEnMemoria ejercicios = new EjerciciosEnMemoria();
    private final IntentosEnMemoria intentos = new IntentosEnMemoria();
    private final AperturaDeEjercicio apertura = new AperturaDeEjercicio(ejercicios, intentos);

    @Test
    void entrega_el_ejercicio_con_sus_opciones_y_los_intentos_previos() {
        Ejercicio ejercicio = ejercicios.agregar(Datos.ejercicioActivo(1L));
        intentos.guardar(Intento.nuevo(7L, ejercicio, ejercicio.opciones().get(0), 4, null, Datos.AHORA));

        AbrirEjercicio.ParaPracticar abierto = apertura.abrir(7L, 1L);

        assertEquals(3, abierto.ejercicio().opciones().size());
        assertEquals(1, abierto.intentosPrevios());
    }

    @Test
    void no_abre_un_ejercicio_desactivado() {
        ejercicios.agregar(Datos.ejercicioDesactivado(1L));

        assertThrows(RecursoNoDisponible.class, () -> apertura.abrir(7L, 1L));
    }

    @Test
    void avisa_cuando_el_ejercicio_no_existe() {
        assertThrows(NoEncontrado.class, () -> apertura.abrir(7L, 99L));
    }
}
