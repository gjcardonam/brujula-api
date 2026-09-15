package co.edu.udea.brujula.apoyo.dobles;

import co.edu.udea.brujula.dominio.puerto.salida.AlmacenDeImagenes;

import java.util.LinkedHashSet;
import java.util.Set;

public class ImagenesEnMemoria implements AlmacenDeImagenes {

    private final Set<String> archivos = new LinkedHashSet<>();
    private int secuencia;

    public String precargar(String nombre) {
        archivos.add(nombre);
        return nombre;
    }

    @Override
    public Imagen guardar(byte[] contenido) {
        String nombre = "imagen" + (++secuencia) + ".png";
        archivos.add(nombre);
        return new Imagen(nombre, urlDe(nombre));
    }

    @Override
    public boolean existe(String nombre) {
        return archivos.contains(nombre);
    }

    @Override
    public String nombreDe(String nombreOUrl) {
        if (nombreOUrl == null) return "";
        int ultimaBarra = nombreOUrl.lastIndexOf('/');
        return ultimaBarra >= 0 ? nombreOUrl.substring(ultimaBarra + 1) : nombreOUrl;
    }

    @Override
    public String urlDe(String nombre) {
        return nombre == null || nombre.isBlank() ? null : "/api/archivos/" + nombre;
    }
}
