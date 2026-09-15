package co.edu.udea.brujula.dominio.servicio;

import java.util.ArrayList;
import java.util.List;

public final class PoliticaDeContrasena {

    public static final String CARACTERES_ESPECIALES = ".,+-*_@";
    public static final int LONGITUD_MINIMA = 8;
    public static final int LONGITUD_MAXIMA = 15;

    private PoliticaDeContrasena() {
    }

    public static List<String> revisar(String contrasena, String confirmacion) {
        List<String> errores = new ArrayList<>();
        if (contrasena == null || contrasena.isEmpty()) {
            errores.add("La contraseña es obligatoria.");
            return errores;
        }
        if (contrasena.length() < LONGITUD_MINIMA || contrasena.length() > LONGITUD_MAXIMA) {
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
        if (contrasena.chars().noneMatch(caracter -> CARACTERES_ESPECIALES.indexOf(caracter) >= 0)) {
            errores.add("La contraseña debe incluir al menos un carácter especial (. , + - * _ @).");
        }
        if (!contrasena.equals(confirmacion)) {
            errores.add("La contraseña y su confirmación no coinciden.");
        }
        return errores;
    }
}
