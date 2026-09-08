package co.edu.udea.brujula.dominio.servicio;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.dominio.modelo.Intento;
import co.edu.udea.brujula.dominio.modelo.Recomendacion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Recomendaciones de estudio (HU-018)")
class GeneradorDeRecomendacionesTest {

    private static final int UMBRAL = 60;

    @Test
    void noRecomiendaNadaCuandoElSimulacroQuedoVacio() {
        assertTrue(GeneradorDeRecomendaciones.generar(List.of(), UMBRAL).isEmpty());
    }

    @Test
    void recomiendaSoloLasAreasPorDebajoDelUmbral() {
        // Geometría queda en 0 %, mientras que Álgebra y la competencia usada superan el umbral.
        List<Intento> intentos = new ArrayList<>(List.of(
                Datos.intento(Datos.GEOMETRIA, Datos.INTERPRETACION, false),
                Datos.intento(Datos.GEOMETRIA, Datos.INTERPRETACION, false)));
        for (int i = 0; i < 4; i++) {
            intentos.add(Datos.intento(Datos.ALGEBRA, Datos.INTERPRETACION, true));
        }

        List<Recomendacion> recomendaciones = GeneradorDeRecomendaciones.generar(intentos, UMBRAL);

        assertEquals(1, recomendaciones.size());
        assertEquals("Geometría", recomendaciones.get(0).area());
        assertEquals(Recomendacion.COMPONENTE, recomendaciones.get(0).tipoDeArea());
        assertTrue(recomendaciones.get(0).mensaje().contains("practicar ejercicios de Geometría"));
    }

    @Test
    void tambienRevisaLasCompetencias() {
        List<Intento> intentos = List.of(
                Datos.intento(Datos.ALGEBRA, Datos.ARGUMENTACION, false),
                Datos.intento(Datos.ALGEBRA, Datos.INTERPRETACION, true),
                Datos.intento(Datos.ALGEBRA, Datos.INTERPRETACION, true));

        List<Recomendacion> recomendaciones = GeneradorDeRecomendaciones.generar(intentos, UMBRAL);

        assertEquals(1, recomendaciones.size());
        assertEquals("Argumentación", recomendaciones.get(0).area());
        assertEquals(Recomendacion.COMPETENCIA, recomendaciones.get(0).tipoDeArea());
    }

    @Test
    void lasOrdenaDeLaMasFlojaALaMenosFloja() {
        List<Intento> intentos = List.of(
                Datos.intento(Datos.GEOMETRIA, Datos.ARGUMENTACION, false),
                Datos.intento(Datos.GEOMETRIA, Datos.ARGUMENTACION, false),
                Datos.intento(Datos.ALGEBRA, Datos.INTERPRETACION, false),
                Datos.intento(Datos.ALGEBRA, Datos.INTERPRETACION, true));

        List<Recomendacion> recomendaciones = GeneradorDeRecomendaciones.generar(intentos, UMBRAL);

        for (int i = 0; i < recomendaciones.size(); i++) {
            assertEquals(i + 1, recomendaciones.get(i).orden());
            if (i > 0) {
                assertTrue(recomendaciones.get(i - 1).porcentaje()
                        .compareTo(recomendaciones.get(i).porcentaje()) <= 0);
            }
        }
        assertEquals(0, recomendaciones.get(0).porcentaje().intValue());
        assertEquals(50, recomendaciones.get(recomendaciones.size() - 1).porcentaje().intValue());
    }

    @Test
    void noRecomiendaCuandoTodoEstaPorEncimaDelUmbral() {
        List<Intento> intentos = List.of(
                Datos.intento(Datos.GEOMETRIA, Datos.ARGUMENTACION, true),
                Datos.intento(Datos.ALGEBRA, Datos.INTERPRETACION, true));

        assertTrue(GeneradorDeRecomendaciones.generar(intentos, UMBRAL).isEmpty());
    }
}
