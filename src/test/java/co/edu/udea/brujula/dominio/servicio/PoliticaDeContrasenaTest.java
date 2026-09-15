package co.edu.udea.brujula.dominio.servicio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Política de contraseña")
class PoliticaDeContrasenaTest {

    @Test
    void acepta_una_contrasena_que_cumple_todas_las_reglas() {
        assertTrue(PoliticaDeContrasena.revisar("Estudiante.26", "Estudiante.26").isEmpty());
    }

    @Test
    void exige_entre_ocho_y_quince_caracteres() {
        assertFalse(PoliticaDeContrasena.revisar("Ab.1", "Ab.1").isEmpty());
        assertFalse(PoliticaDeContrasena.revisar("Abcdefghijklmno.1", "Abcdefghijklmno.1").isEmpty());
    }

    @Test
    void exige_mayuscula_minuscula_numero_y_caracter_especial() {
        assertFalse(PoliticaDeContrasena.revisar("estudiante.26", "estudiante.26").isEmpty());
        assertFalse(PoliticaDeContrasena.revisar("ESTUDIANTE.26", "ESTUDIANTE.26").isEmpty());
        assertFalse(PoliticaDeContrasena.revisar("Estudiante.", "Estudiante.").isEmpty());
        assertFalse(PoliticaDeContrasena.revisar("Estudiante26", "Estudiante26").isEmpty());
    }

    @Test
    void exige_que_la_confirmacion_coincida() {
        List<String> errores = PoliticaDeContrasena.revisar("Estudiante.26", "Estudiante.27");

        assertEquals(1, errores.size());
        assertTrue(errores.get(0).contains("confirmación"));
    }

    @Test
    void exige_que_la_contrasena_venga() {
        assertEquals(1, PoliticaDeContrasena.revisar(null, null).size());
        assertEquals(1, PoliticaDeContrasena.revisar("", "").size());
    }
}
