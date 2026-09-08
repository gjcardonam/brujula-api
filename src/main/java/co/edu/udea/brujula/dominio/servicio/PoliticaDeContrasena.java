package co.edu.udea.brujula.dominio.servicio;

import java.util.ArrayList;
import java.util.List;

/**
 * Reglas de la contraseña y de los nombres (HU-001 CA-05 y CA-06). Se reutilizan en el registro, en
 * el restablecimiento y en el cambio desde el perfil, así que viven en un solo lugar.
 */
public final class PoliticaDeContrasena {

    public static final String CARACTERES_ESPECIALES = ".,+-*_@";

    private PoliticaDeContrasena() {
    }

    public static List<String> revisar(String contrasena, String confirmacion) {
        List<String> errores = new ArrayList<>();
        if (contrasena == null || contrasena.isEmpty()) {
            errores.add("La contraseña es obligatoria.");
            return errores;
        }
        if (contrasena.length() < 8 || contrasena.length() > 15) {
            errores.add("La contraseña debe tener entre 8 y 15 caracteres.");
        }
        if (contrasena.chars().noneMatch(Character::isUpperCase)) {
            errores.add("La contraseña debe incluir al menos una letra mayúscula.");
        }
        if (contrasena.chars().noneMatch(Character::isLowerCase)) {
            errores.add("La contraseña debe incluir al menos una letra minúscula.");
        }
        if (contrasena.chars().noneMatch(Character::isDigit)) {
            errores.add("La contraseña debe incluir al menos un número.");
        }
        if (contrasena.chars().noneMatch(c -> CARACTERES_ESPECIALES.indexOf(c) >= 0)) {
            errores.add("La contraseña debe incluir al menos un carácter especial (. , + - * _ @).");
        }
        if (!contrasena.equals(confirmacion)) {
            errores.add("La contraseña y su confirmación no coinciden.");
        }
        return errores;
    }

    public static List<String> revisarNombres(String nombre, String apellido) {
        List<String> errores = new ArrayList<>();
        String n = nombre == null ? "" : nombre.trim();
        String a = apellido == null ? "" : apellido.trim();
        if (n.isEmpty() || n.length() > 30) errores.add("El nombre debe tener entre 1 y 30 caracteres.");
        if (a.length() < 2 || a.length() > 50) errores.add("El apellido debe tener entre 2 y 50 caracteres.");
        return errores;
    }
}
