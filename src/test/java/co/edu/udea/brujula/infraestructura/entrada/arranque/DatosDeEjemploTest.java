package co.edu.udea.brujula.infraestructura.entrada.arranque;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Nombres de imagenes de los datos de ejemplo")
class DatosDeEjemploTest {

    private DatosDeEjemplo.OpcionJson opcionConImagen(String imagen) {
        return new DatosDeEjemplo.OpcionJson(null, imagen, false, "Retroalimentacion.");
    }

    private DatosDeEjemplo.OpcionJson opcionSinImagen() {
        return new DatosDeEjemplo.OpcionJson("Una opcion de texto", null, true, "Retroalimentacion.");
    }

    @Test
    void junta_la_imagen_del_enunciado_y_las_de_las_opciones() {
        DatosDeEjemplo.EjercicioJson ejercicio = new DatosDeEjemplo.EjercicioJson("¿Cuanto es 2+2?", "enun.png",
                "Álgebra y cálculo", "Formulación y ejecución", "Básico",
                List.of(opcionConImagen("a.png"), opcionConImagen("b.png")));

        Set<String> nombres = DatosDeEjemplo.nombresDeImagenes(List.of(ejercicio));

        assertEquals(Set.of("enun.png", "a.png", "b.png"), nombres);
    }

    @Test
    void ignora_las_imagenes_nulas_de_enunciados_y_opciones_de_solo_texto() {
        DatosDeEjemplo.EjercicioJson ejercicio = new DatosDeEjemplo.EjercicioJson("¿Cuanto es 2+2?", null,
                "Álgebra y cálculo", "Formulación y ejecución", "Básico",
                List.of(opcionSinImagen(), opcionSinImagen()));

        Set<String> nombres = DatosDeEjemplo.nombresDeImagenes(List.of(ejercicio));

        assertEquals(Set.of(), nombres);
    }

    @Test
    void no_repite_una_imagen_compartida_por_varias_preguntas() {
        DatosDeEjemplo.EjercicioJson primero = new DatosDeEjemplo.EjercicioJson("Pregunta 1", "compartida.png",
                "Geometría", "Argumentación", "Avanzado", List.of(opcionSinImagen()));
        DatosDeEjemplo.EjercicioJson segundo = new DatosDeEjemplo.EjercicioJson("Pregunta 2", "compartida.png",
                "Geometría", "Argumentación", "Avanzado", List.of(opcionSinImagen()));

        Set<String> nombres = DatosDeEjemplo.nombresDeImagenes(List.of(primero, segundo));

        assertEquals(Set.of("compartida.png"), nombres);
    }
}
