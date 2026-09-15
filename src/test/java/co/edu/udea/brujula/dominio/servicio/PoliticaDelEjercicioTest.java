package co.edu.udea.brujula.dominio.servicio;

import co.edu.udea.brujula.dominio.puerto.entrada.comando.DatosDeEjercicio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Política del ejercicio")
class PoliticaDelEjercicioTest {

    private static DatosDeEjercicio con(List<DatosDeEjercicio.DatosDeOpcion> opciones) {
        return new DatosDeEjercicio("¿Cuánto es 2 + 2?", null, 1L, 1L, 1L, opciones);
    }

    private static DatosDeEjercicio.DatosDeOpcion opcion(String texto, boolean correcta, String retroalimentacion) {
        return new DatosDeEjercicio.DatosDeOpcion(texto, null, correcta, retroalimentacion);
    }

    @Test
    void acepta_un_ejercicio_completo() {
        assertTrue(PoliticaDelEjercicio.revisar(con(List.of(
                opcion("Cuatro", true, "Correcto."),
                opcion("Cinco", false, "Revisa la suma.")))).isEmpty());
    }

    @Test
    void exige_al_menos_dos_opciones() {
        assertFalse(PoliticaDelEjercicio.revisar(con(List.of(opcion("Cuatro", true, "Correcto.")))).isEmpty());
        assertFalse(PoliticaDelEjercicio.revisar(con(List.of())).isEmpty());
    }

    @Test
    void admite_como_maximo_seis_opciones() {
        List<DatosDeEjercicio.DatosDeOpcion> siete = List.of(
                opcion("A", true, "Bien."), opcion("B", false, "Mal."), opcion("C", false, "Mal."),
                opcion("D", false, "Mal."), opcion("E", false, "Mal."), opcion("F", false, "Mal."),
                opcion("G", false, "Mal."));

        assertFalse(PoliticaDelEjercicio.revisar(con(siete)).isEmpty());
    }

    @Test
    void exige_exactamente_una_opcion_correcta() {
        assertFalse(PoliticaDelEjercicio.revisar(con(List.of(
                opcion("Cuatro", false, "Mal."), opcion("Cinco", false, "Mal.")))).isEmpty());
        assertFalse(PoliticaDelEjercicio.revisar(con(List.of(
                opcion("Cuatro", true, "Bien."), opcion("Cinco", true, "Bien.")))).isEmpty());
    }

    @Test
    void rechaza_opciones_repetidas_sin_importar_mayusculas_ni_espacios() {
        assertFalse(PoliticaDelEjercicio.revisar(con(List.of(
                opcion("Cuatro", true, "Bien."), opcion(" cuatro ", false, "Mal.")))).isEmpty());
    }

    @Test
    void exige_contenido_y_retroalimentacion_en_cada_opcion() {
        assertFalse(PoliticaDelEjercicio.revisar(con(List.of(
                opcion("Cuatro", true, "Bien."), opcion("  ", false, "Mal.")))).isEmpty());
        assertFalse(PoliticaDelEjercicio.revisar(con(List.of(
                opcion("Cuatro", true, "Bien."), opcion("Cinco", false, null)))).isEmpty());
    }

    @Test
    void exige_enunciado_componente_competencia_y_nivel() {
        DatosDeEjercicio incompleto = new DatosDeEjercicio("  ", null, null, null, null, List.of(
                new DatosDeEjercicio.DatosDeOpcion("Cuatro", null, true, "Bien."),
                new DatosDeEjercicio.DatosDeOpcion("Cinco", null, false, "Mal.")));

        assertFalse(PoliticaDelEjercicio.revisar(incompleto).isEmpty());
    }
}
