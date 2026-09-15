package co.edu.udea.brujula.aplicacion.administracion;

import co.edu.udea.brujula.apoyo.dobles.ImagenesEnMemoria;
import co.edu.udea.brujula.dominio.puerto.salida.AlmacenDeImagenes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Guardado de una imagen")
class GuardadoDeImagenTest {

    private final ImagenesEnMemoria imagenes = new ImagenesEnMemoria();
    private final GuardadoDeImagen guardado = new GuardadoDeImagen(imagenes);

    @Test
    void deja_la_imagen_disponible_y_devuelve_su_url() {
        AlmacenDeImagenes.Imagen imagen = guardado.guardar(new byte[]{1, 2, 3});

        assertTrue(imagenes.existe(imagen.nombre()));
        assertEquals("/api/archivos/" + imagen.nombre(), imagen.url());
    }
}
