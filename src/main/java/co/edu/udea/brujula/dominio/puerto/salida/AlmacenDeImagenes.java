package co.edu.udea.brujula.dominio.puerto.salida;

public interface AlmacenDeImagenes {

    record Imagen(String nombre, String url) {
    }

    /** Guarda la imagen validando formato y tamaño (HU-020 CA-09). */
    Imagen guardar(byte[] contenido);

    boolean existe(String nombre);

    /** Acepta el nombre o la URL completa y devuelve solo el nombre del archivo. */
    String nombreDe(String nombreOUrl);

    String urlDe(String nombre);
}
