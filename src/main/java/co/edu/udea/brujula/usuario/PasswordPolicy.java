package co.edu.udea.brujula.usuario;

import java.util.ArrayList;
import java.util.List;

/** Reglas de HU-001 CA-05 y CA-06, reutilizadas por HU-003 y HU-005. */
public final class PasswordPolicy {
    private PasswordPolicy() {}

    public static final String ESPECIALES = ".,+-*_@";

    public static List<String> validar(String password, String confirmacion) {
        List<String> errores = new ArrayList<>();
        if (password == null || password.isEmpty()) {
            errores.add("La contraseña es obligatoria.");
            return errores;
        }
        if (password.length() < 8 || password.length() > 15) errores.add("La contraseña debe tener entre 8 y 15 caracteres.");
        if (!password.chars().anyMatch(Character::isUpperCase)) errores.add("La contraseña debe incluir al menos una letra mayúscula.");
        if (!password.chars().anyMatch(Character::isLowerCase)) errores.add("La contraseña debe incluir al menos una letra minúscula.");
        if (!password.chars().anyMatch(Character::isDigit)) errores.add("La contraseña debe incluir al menos un número.");
        if (password.chars().noneMatch(c -> ESPECIALES.indexOf(c) >= 0)) errores.add("La contraseña debe incluir al menos un carácter especial (. , + - * _ @).");
        if (!password.equals(confirmacion)) errores.add("La contraseña y su confirmación no coinciden.");
        return errores;
    }

    public static List<String> validarNombres(String nombre, String apellido) {
        List<String> errores = new ArrayList<>();
        String n = nombre == null ? "" : nombre.trim();
        String a = apellido == null ? "" : apellido.trim();
        if (n.isEmpty() || n.length() > 30) errores.add("El nombre debe tener entre 1 y 30 caracteres.");
        if (a.length() < 2 || a.length() > 50) errores.add("El apellido debe tener entre 2 y 50 caracteres.");
        return errores;
    }
}
