package co.edu.udea.brujula.dominio.servicio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Política de contraseñas (HU-001 CA-05 y CA-06)")
class PoliticaDeContrasenaTest {

    @Test
    void aceptaUnaContrasenaQueCumpleTodo() {
        assertTrue(PoliticaDeContrasena.revisar("Clave.2026", "Clave.2026").isEmpty());
    }

    @Test
    void exigeEntreOchoYQuinceCaracteres() {
        assertFalse(PoliticaDeContrasena.revisar("Ab.1", "Ab.1").isEmpty());
        assertFalse(PoliticaDeContrasena.revisar("Abcdefgh.1234567", "Abcdefgh.1234567").isEmpty());
    }

    @Test
    void exigeMayusculaMinusculaNumeroYEspecial() {
        assertTrue(contiene(PoliticaDeContrasena.revisar("clave.2026", "clave.2026"), "mayúscula"));
        assertTrue(contiene(PoliticaDeContrasena.revisar("CLAVE.2026", "CLAVE.2026"), "minúscula"));
        assertTrue(contiene(PoliticaDeContrasena.revisar("Clave.abcd", "Clave.abcd"), "número"));
        assertTrue(contiene(PoliticaDeContrasena.revisar("Clave2026x", "Clave2026x"), "especial"));
    }

    @Test
    void soloAceptaLosCaracteresEspecialesDeLaHistoria() {
        assertTrue(contiene(PoliticaDeContrasena.revisar("Clave#2026", "Clave#2026"), "especial"));
    }

    @Test
    void exigeQueLaConfirmacionCoincida() {
        assertTrue(contiene(PoliticaDeContrasena.revisar("Clave.2026", "Clave.2027"), "coinciden"));
    }

    @Test
    void validaLaLongitudDeNombreYApellido() {
        assertTrue(PoliticaDeContrasena.revisarNombres("Ana", "Pérez").isEmpty());
        assertFalse(PoliticaDeContrasena.revisarNombres("", "Pérez").isEmpty());
        assertFalse(PoliticaDeContrasena.revisarNombres("Ana", "P").isEmpty());
        assertFalse(PoliticaDeContrasena.revisarNombres("a".repeat(31), "Pérez").isEmpty());
    }

    private boolean contiene(List<String> errores, String texto) {
        return errores.stream().anyMatch(e -> e.contains(texto));
    }
}
