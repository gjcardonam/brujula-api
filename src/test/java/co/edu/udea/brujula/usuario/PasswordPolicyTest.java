package co.edu.udea.brujula.usuario;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** HU-001 CA-05 y CA-06. */
class PasswordPolicyTest {

    @Test
    void aceptaUnaContrasenaValida() {
        assertTrue(PasswordPolicy.validar("Clave.2026", "Clave.2026").isEmpty());
    }

    @Test
    void rechazaLongitudFueraDeRango() {
        assertFalse(PasswordPolicy.validar("Ab.1", "Ab.1").isEmpty());
        assertFalse(PasswordPolicy.validar("Abcdefgh.1234567", "Abcdefgh.1234567").isEmpty());
    }

    @Test
    void exigeMayusculaMinusculaNumeroYEspecial() {
        assertTrue(PasswordPolicy.validar("clave.2026", "clave.2026").stream().anyMatch(m -> m.contains("mayúscula")));
        assertTrue(PasswordPolicy.validar("CLAVE.2026", "CLAVE.2026").stream().anyMatch(m -> m.contains("minúscula")));
        assertTrue(PasswordPolicy.validar("Clave.abcd", "Clave.abcd").stream().anyMatch(m -> m.contains("número")));
        assertTrue(PasswordPolicy.validar("Clave2026x", "Clave2026x").stream().anyMatch(m -> m.contains("especial")));
    }

    @Test
    void soloAceptaLosEspecialesDefinidos() {
        List<String> errores = PasswordPolicy.validar("Clave#2026", "Clave#2026");
        assertTrue(errores.stream().anyMatch(m -> m.contains("especial")));
    }

    @Test
    void exigeQueLaConfirmacionCoincida() {
        assertTrue(PasswordPolicy.validar("Clave.2026", "Clave.2027").stream().anyMatch(m -> m.contains("coinciden")));
    }

    @Test
    void validaLongitudDeNombres() {
        assertTrue(PasswordPolicy.validarNombres("Ana", "Pérez").isEmpty());
        assertFalse(PasswordPolicy.validarNombres("", "Pérez").isEmpty());
        assertFalse(PasswordPolicy.validarNombres("Ana", "P").isEmpty());
        assertFalse(PasswordPolicy.validarNombres("a".repeat(31), "Pérez").isEmpty());
    }
}
