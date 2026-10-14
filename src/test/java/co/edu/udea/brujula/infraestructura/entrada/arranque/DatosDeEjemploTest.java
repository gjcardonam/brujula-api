package co.edu.udea.brujula.infraestructura.entrada.arranque;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Datos de ejemplo del banco de ejercicios")
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

    @Test
    void solo_deja_los_ejercicios_cuyo_enunciado_no_esta_en_el_banco() {
        DatosDeEjemplo.EjercicioJson cargado = new DatosDeEjemplo.EjercicioJson("Pregunta del simulacro 1 ", null,
                "Geometría", "Argumentación", "Avanzado", List.of(opcionSinImagen()));
        DatosDeEjemplo.EjercicioJson nuevo = new DatosDeEjemplo.EjercicioJson("Pregunta del simulacro 2", null,
                "Geometría", "Argumentación", "Avanzado", List.of(opcionSinImagen()));

        List<DatosDeEjemplo.EjercicioJson> faltantes = DatosDeEjemplo.faltantes(List.of(cargado, nuevo),
                "Pregunta del simulacro 1"::equals);

        assertEquals(List.of(nuevo), faltantes);
    }

    @Test
    void cada_archivo_de_ejemplo_se_lee_y_sus_imagenes_existen() throws Exception {
        ObjectMapper json = new ObjectMapper();
        for (String ruta : DatosDeEjemplo.ARCHIVOS) {
            List<DatosDeEjemplo.EjercicioJson> ejercicios;
            try (InputStream archivo = new ClassPathResource(ruta).getInputStream()) {
                ejercicios = json.readValue(archivo,
                        json.getTypeFactory().constructCollectionType(List.class, DatosDeEjemplo.EjercicioJson.class));
            }
            assertFalse(ejercicios.isEmpty(), ruta);
            for (String imagen : DatosDeEjemplo.nombresDeImagenes(ejercicios)) {
                assertTrue(new ClassPathResource("datos-ejemplo/imagenes/" + imagen).exists(), ruta + ": " + imagen);
            }
        }
    }
}
