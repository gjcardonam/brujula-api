package co.edu.udea.brujula.infraestructura.salida.archivo;

import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.excepcion.ServicioNoDisponible;
import co.edu.udea.brujula.dominio.puerto.salida.AlmacenDeImagenes;
import co.edu.udea.brujula.infraestructura.configuracion.BrujulaProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Component
public class AlmacenEnDisco implements AlmacenDeImagenes {

    public static final long TAMANO_MAXIMO = 5L * 1024 * 1024;
    private static final String RUTA_PUBLICA = "/api/archivos/";

    private final Path carpeta;

    public AlmacenEnDisco(BrujulaProperties propiedades) {
        String configurada = propiedades.uploadsDir() == null ? "./uploads" : propiedades.uploadsDir();
        this.carpeta = Path.of(configurada).toAbsolutePath().normalize();
        try {
            Files.createDirectories(carpeta);
        } catch (IOException e) {
            throw new IllegalStateException("No fue posible crear la carpeta de imágenes " + carpeta, e);
        }
    }

    @Override
    public Imagen guardar(byte[] contenido) {
        if (contenido == null || contenido.length == 0) {
            throw new DatosInvalidos("ARCHIVO_INVALIDO", "No se recibió ninguna imagen.");
        }
        if (contenido.length > TAMANO_MAXIMO) {
            throw new DatosInvalidos("ARCHIVO_INVALIDO", "La imagen supera el tamaño máximo de 5 MB.");
        }
        String extension = extensionSegunContenido(contenido);
        if (extension == null) {
            throw new DatosInvalidos("ARCHIVO_INVALIDO", "Solo se aceptan imágenes JPG, PNG o WEBP.");
        }
        String nombre = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        try {
            Files.write(carpeta.resolve(nombre), contenido, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            throw new ServicioNoDisponible("ARCHIVO_ERROR", "No fue posible guardar la imagen.");
        }
        return new Imagen(nombre, urlDe(nombre));
    }

    private String extensionSegunContenido(byte[] bytes) {
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (bytes.length >= 8 && (bytes[0] & 0xFF) == 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G') {
            return "png";
        }
        if (bytes.length >= 12 && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return "webp";
        }
        return null;
    }

    public Path rutaDe(String nombre) {
        if (nombre == null || !nombre.matches("[A-Za-z0-9]+\\.(jpg|png|webp)")) return null;
        Path ruta = carpeta.resolve(nombre).normalize();
        return ruta.startsWith(carpeta) && Files.isRegularFile(ruta) ? ruta : null;
    }

    @Override
    public boolean existe(String nombre) {
        return rutaDe(nombre) != null;
    }

    @Override
    public String nombreDe(String nombreOUrl) {
        if (nombreOUrl == null) return "";
        int ultimaBarra = nombreOUrl.lastIndexOf('/');
        return ultimaBarra >= 0 ? nombreOUrl.substring(ultimaBarra + 1) : nombreOUrl;
    }

    @Override
    public String urlDe(String nombre) {
        return nombre == null || nombre.isBlank() ? null : RUTA_PUBLICA + nombre;
    }

    public String tipoMime(String nombre) {
        if (nombre.endsWith(".png")) return "image/png";
        if (nombre.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }
}
