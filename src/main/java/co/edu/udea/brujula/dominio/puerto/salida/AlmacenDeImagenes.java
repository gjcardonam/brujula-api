package co.edu.udea.brujula.dominio.puerto.salida;

public interface AlmacenDeImagenes {

    record Imagen(String nombre, String url) {
    }

    Imagen guardar(byte[] contenido);

    boolean existe(String nombre);

    String nombreDe(String nombreOUrl);

    String urlDe(String nombre);
}
