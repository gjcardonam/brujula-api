package co.edu.udea.brujula.dominio.servicio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Política de nombres")
class PoliticaDeNombresTest {

    @Test
    void acepta_un_nombre_de_uno_a_treinta_y_un_apellido_de_dos_a_cincuenta() {
        assertTrue(PoliticaDeNombres.revisar("Ana María", "Pérez Gómez").isEmpty());
        assertTrue(PoliticaDeNombres.revisar("A", "Bc").isEmpty());
    }

    @Test
    void rechaza_un_nombre_vacio_o_demasiado_largo() {
        assertEquals(1, PoliticaDeNombres.revisar("   ", "Pérez").size());
        assertEquals(1, PoliticaDeNombres.revisar("N".repeat(31), "Pérez").size());
    }

    @Test
    void rechaza_un_apellido_de_menos_de_dos_o_mas_de_cincuenta_caracteres() {
        assertEquals(1, PoliticaDeNombres.revisar("Ana", "P").size());
        assertEquals(1, PoliticaDeNombres.revisar("Ana", "A".repeat(51)).size());
    }

    @Test
    void ignora_los_espacios_de_los_extremos() {
        assertTrue(PoliticaDeNombres.revisar("  Ana  ", "  Pérez  ").isEmpty());
    }
}
