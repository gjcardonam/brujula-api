package co.edu.udea.brujula.aplicacion.administracion;

import co.edu.udea.brujula.dominio.puerto.entrada.GuardarImagen;
import co.edu.udea.brujula.dominio.puerto.salida.AlmacenDeImagenes;
import org.springframework.stereotype.Service;

@Service
public class GuardadoDeImagen implements GuardarImagen {

    private final AlmacenDeImagenes imagenes;

    public GuardadoDeImagen(AlmacenDeImagenes imagenes) {
        this.imagenes = imagenes;
    }

    @Override
    public AlmacenDeImagenes.Imagen guardar(byte[] contenido) {
        return imagenes.guardar(contenido);
    }
}
