package co.edu.udea.brujula.dominio.servicio;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.dominio.modelo.Intento;
import co.edu.udea.brujula.dominio.modelo.Recomendacion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Recomendaciones de estudio")
class GeneradorDeRecomendacionesTest {

    private static final int UMBRAL = 60;

    @Test
    void no_recomienda_nada_cuando_el_simulacro_quedo_vacio() {
        assertTrue(GeneradorDeRecomendaciones.generar(List.of(), UMBRAL).isEmpty());
    }

    @Test
    void recomienda_solo_las_areas_por_debajo_del_umbral() {
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
        assertEquals(Datos.GEOMETRIA.id(), recomendaciones.get(0).idComponente());
        assertEquals("Te recomendamos practicar ejercicios de Geometría.", recomendaciones.get(0).mensaje());
    }

    @Test
    void tambien_revisa_las_competencias() {
        List<Intento> intentos = List.of(
                Datos.intento(Datos.ALGEBRA, Datos.ARGUMENTACION, false),
                Datos.intento(Datos.ALGEBRA, Datos.INTERPRETACION, true),
                Datos.intento(Datos.ALGEBRA, Datos.INTERPRETACION, true));

        List<Recomendacion> recomendaciones = GeneradorDeRecomendaciones.generar(intentos, UMBRAL);

        assertEquals(1, recomendaciones.size());
        assertEquals("Argumentación", recomendaciones.get(0).area());
        assertEquals(Recomendacion.COMPETENCIA, recomendaciones.get(0).tipoDeArea());
        assertEquals(Datos.ARGUMENTACION.id(), recomendaciones.get(0).idCompetencia());
    }

    @Test
    void las_ordena_de_la_mas_floja_a_la_menos_floja() {
        List<Intento> intentos = List.of(
                Datos.intento(Datos.GEOMETRIA, Datos.ARGUMENTACION, false),
                Datos.intento(Datos.GEOMETRIA, Datos.ARGUMENTACION, false),
                Datos.intento(Datos.ALGEBRA, Datos.INTERPRETACION, false),
                Datos.intento(Datos.ALGEBRA, Datos.INTERPRETACION, true));

        List<Recomendacion> recomendaciones = GeneradorDeRecomendaciones.generar(intentos, UMBRAL);

        assertEquals(4, recomendaciones.size());
        for (int i = 0; i < recomendaciones.size(); i++) {
            assertEquals(i + 1, recomendaciones.get(i).orden());
            if (i > 0) {
                assertTrue(recomendaciones.get(i - 1).porcentaje()
                        .compareTo(recomendaciones.get(i).porcentaje()) <= 0);
            }
        }
        assertEquals(0, recomendaciones.get(0).porcentaje().intValue());
        assertEquals(50, recomendaciones.get(3).porcentaje().intValue());
    }

    @Test
    void a_igual_porcentaje_ordena_alfabeticamente_en_espanol() {
        List<Intento> intentos = List.of(
                Datos.intento(Datos.ALGEBRA, Datos.INTERPRETACION, false),
                Datos.intento(Datos.ALGEBRA, Datos.INTERPRETACION, true));

        List<Recomendacion> recomendaciones = GeneradorDeRecomendaciones.generar(intentos, UMBRAL);

        assertEquals(List.of("Álgebra y cálculo", "Interpretación y representación"),
                recomendaciones.stream().map(Recomendacion::area).toList());
    }

    @Test
    void sesenta_por_ciento_exacto_no_se_considera_area_por_reforzar() {
        List<Intento> intentos = new ArrayList<>();
        for (int i = 0; i < 3; i++) intentos.add(Datos.intento(Datos.ALGEBRA, Datos.INTERPRETACION, true));
        for (int i = 0; i < 2; i++) intentos.add(Datos.intento(Datos.ALGEBRA, Datos.INTERPRETACION, false));

        assertTrue(GeneradorDeRecomendaciones.generar(intentos, UMBRAL).isEmpty());
    }

    @Test
    void el_mensaje_general_distingue_buen_desempeno_de_simulacro_sin_respuestas() {
        Recomendacion una = GeneradorDeRecomendaciones.generar(
                List.of(Datos.intento(Datos.GEOMETRIA, Datos.ARGUMENTACION, false)), UMBRAL).get(0);

        assertEquals(GeneradorDeRecomendaciones.SIN_RESPUESTAS,
                GeneradorDeRecomendaciones.mensajeGeneral(0, List.of()));
        assertEquals(GeneradorDeRecomendaciones.BUEN_DESEMPENO,
                GeneradorDeRecomendaciones.mensajeGeneral(4, List.of()));
        assertNull(GeneradorDeRecomendaciones.mensajeGeneral(4, List.of(una)));
    }
}
