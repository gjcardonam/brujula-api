package co.edu.udea.brujula.ejercicio;

import co.edu.udea.brujula.common.ApiException;
import co.edu.udea.brujula.config.BrujulaProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

/** Imágenes de enunciados y opciones (HU-020 CA-09): JPG, PNG o WEBP de máximo 5 MB. */
@Service
public class ArchivoService {

    public static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Map<String, String> TIPOS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp");

    private final Path dir;

    public ArchivoService(BrujulaProperties props) {
        this.dir = Path.of(props.uploadsDir() == null ? "./uploads" : props.uploadsDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new IllegalStateException("No fue posible crear la carpeta de imágenes " + dir, e);
        }
    }

    public record Guardado(String nombre, String url) {}

    public Guardado guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) throw ApiException.badRequest("ARCHIVO_INVALIDO", "No se recibió ninguna imagen.");
        if (archivo.getSize() > MAX_BYTES) throw ApiException.badRequest("ARCHIVO_INVALIDO", "La imagen supera el tamaño máximo de 5 MB.");
        String tipo = detectarTipo(archivo);
        String ext = TIPOS.get(tipo);
        if (ext == null) throw ApiException.badRequest("ARCHIVO_INVALIDO", "Solo se aceptan imágenes JPG, PNG o WEBP.");
        String nombre = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        try (InputStream in = archivo.getInputStream()) {
            Files.copy(in, dir.resolve(nombre), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ApiException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "ARCHIVO_ERROR", "No fue posible guardar la imagen.");
        }
        return new Guardado(nombre, url(nombre));
    }

    /** Se valida por los bytes iniciales, no por la extensión ni por lo que declare el navegador. */
    private String detectarTipo(MultipartFile archivo) {
        try (InputStream in = archivo.getInputStream()) {
            byte[] b = in.readNBytes(16);
            if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) return "image/jpeg";
            if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') return "image/png";
            if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F' && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') return "image/webp";
            return "desconocido";
        } catch (IOException e) {
            return "desconocido";
        }
    }

    public Path ruta(String nombre) {
        if (nombre == null || !nombre.matches("[A-Za-z0-9]+\\.(jpg|png|webp)")) return null;
        Path p = dir.resolve(nombre).normalize();
        return p.startsWith(dir) && Files.isRegularFile(p) ? p : null;
    }

    public boolean existe(String nombre) {
        return ruta(nombre) != null;
    }

    /** Acepta el nombre o la URL completa y devuelve solo el nombre del archivo. */
    public String nombre(String nombreOUrl) {
        if (nombreOUrl == null) return "";
        int i = nombreOUrl.lastIndexOf('/');
        return i >= 0 ? nombreOUrl.substring(i + 1) : nombreOUrl;
    }

    public String url(String nombre) {
        return nombre == null || nombre.isBlank() ? null : "/api/archivos/" + nombre;
    }

    public String tipoMime(String nombre) {
        if (nombre.endsWith(".png")) return "image/png";
        if (nombre.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }
}
