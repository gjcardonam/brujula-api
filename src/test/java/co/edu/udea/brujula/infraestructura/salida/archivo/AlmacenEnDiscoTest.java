package co.edu.udea.brujula.infraestructura.salida.archivo;

import co.edu.udea.brujula.dominio.excepcion.ServicioNoDisponible;
import co.edu.udea.brujula.infraestructura.configuracion.BrujulaProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Copia de imagenes de ejemplo al directorio de subidas")
class AlmacenEnDiscoTest {

    private AlmacenEnDisco almacenEn(Path carpeta) {
        BrujulaProperties propiedades = new BrujulaProperties(null, null, null, false,
                carpeta.toString(), null, null, true);
        return new AlmacenEnDisco(propiedades);
    }

    @Test
    void escribe_la_imagen_conservando_su_nombre_cuando_no_existe(@TempDir Path carpeta) throws IOException {
        AlmacenEnDisco almacen = almacenEn(carpeta);
        byte[] contenido = "contenido de prueba".getBytes(StandardCharsets.UTF_8);

        almacen.copiarSiFalta("s1p01e.png", contenido);

        Path archivo = carpeta.resolve("s1p01e.png");
        assertArrayEquals(contenido, Files.readAllBytes(archivo));
    }

    @Test
    void no_sobrescribe_la_imagen_si_ya_existe_en_el_disco(@TempDir Path carpeta) throws IOException {
        AlmacenEnDisco almacen = almacenEn(carpeta);
        Path archivo = carpeta.resolve("s1p01e.png");
        Files.write(archivo, "version en disco tras un despliegue anterior".getBytes(StandardCharsets.UTF_8));

        almacen.copiarSiFalta("s1p01e.png", "version nueva del jar".getBytes(StandardCharsets.UTF_8));

        assertEquals("version en disco tras un despliegue anterior",
                Files.readString(archivo, StandardCharsets.UTF_8));
    }

    @Test
    void simula_el_disco_borrado_en_cada_despliegue_de_render(@TempDir Path carpeta) throws IOException {
        AlmacenEnDisco primerDespliegue = almacenEn(carpeta);
        byte[] contenido = "imagen de ejemplo".getBytes(StandardCharsets.UTF_8);
        primerDespliegue.copiarSiFalta("s1p11a.png", contenido);

        borrarTodoElContenidoDe(carpeta);

        AlmacenEnDisco segundoDespliegue = almacenEn(carpeta);
        segundoDespliegue.copiarSiFalta("s1p11a.png", contenido);

        assertArrayEquals(contenido, Files.readAllBytes(carpeta.resolve("s1p11a.png")));
    }

    @Test
    void rechaza_un_nombre_que_no_cumple_el_patron_permitido(@TempDir Path carpeta) {
        AlmacenEnDisco almacen = almacenEn(carpeta);

        assertThrows(IllegalArgumentException.class,
                () -> almacen.copiarSiFalta("../s1p01e.png", new byte[]{1}));
    }

    @Test
    void reporta_servicio_no_disponible_si_la_carpeta_de_subidas_desaparece(@TempDir Path carpeta) throws IOException {
        AlmacenEnDisco almacen = almacenEn(carpeta);
        borrarTodoElContenidoDe(carpeta);
        Files.writeString(carpeta, "esto convierte la carpeta en un archivo", StandardCharsets.UTF_8);

        assertThrows(ServicioNoDisponible.class,
                () -> almacen.copiarSiFalta("s1p01e.png", new byte[]{1}));
    }

    private void borrarTodoElContenidoDe(Path carpeta) throws IOException {
        try (var listado = Files.list(carpeta)) {
            for (Path archivo : listado.toList()) {
                Files.delete(archivo);
            }
        }
        Files.delete(carpeta);
    }
}
