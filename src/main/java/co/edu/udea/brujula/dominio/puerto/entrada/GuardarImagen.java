package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.puerto.salida.AlmacenDeImagenes;

public interface GuardarImagen {

    AlmacenDeImagenes.Imagen guardar(byte[] contenido);
}
