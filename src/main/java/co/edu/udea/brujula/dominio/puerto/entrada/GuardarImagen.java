package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.puerto.salida.AlmacenDeImagenes;

/** HU-020 CA-09: imágenes del enunciado y de las opciones. */
public interface GuardarImagen {
    AlmacenDeImagenes.Imagen guardar(byte[] contenido);
}
