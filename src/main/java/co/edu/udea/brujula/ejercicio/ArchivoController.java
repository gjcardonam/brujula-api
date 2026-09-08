package co.edu.udea.brujula.ejercicio;

import co.edu.udea.brujula.common.ApiException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/archivos")
public class ArchivoController {

    private final ArchivoService archivos;

    public ArchivoController(ArchivoService archivos) {
        this.archivos = archivos;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ArchivoService.Guardado subir(@RequestParam("archivo") MultipartFile archivo) {
        return archivos.guardar(archivo);
    }

    @GetMapping("/{nombre}")
    public ResponseEntity<FileSystemResource> ver(@PathVariable String nombre) {
        Path p = archivos.ruta(nombre);
        if (p == null) throw ApiException.noEncontrado("La imagen no existe.");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivos.tipoMime(nombre)))
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                .body(new FileSystemResource(p));
    }
}
