package co.edu.udea.brujula.infraestructura.entrada.rest;

import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.puerto.entrada.GuardarImagen;
import co.edu.udea.brujula.dominio.puerto.salida.AlmacenDeImagenes;
import co.edu.udea.brujula.infraestructura.salida.archivo.AlmacenEnDisco;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/archivos")
public class ArchivoControlador {

    private final GuardarImagen guardarImagen;
    private final AlmacenEnDisco almacen;

    public ArchivoControlador(GuardarImagen guardarImagen, AlmacenEnDisco almacen) {
        this.guardarImagen = guardarImagen;
        this.almacen = almacen;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public AlmacenDeImagenes.Imagen subir(@RequestParam("archivo") MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new DatosInvalidos("ARCHIVO_INVALIDO", "No se recibió ninguna imagen.");
        }
        try {
            return guardarImagen.guardar(archivo.getBytes());
        } catch (IOException e) {
            throw new DatosInvalidos("ARCHIVO_INVALIDO", "No fue posible leer la imagen enviada.");
        }
    }

    @GetMapping("/{nombre}")
    public ResponseEntity<FileSystemResource> ver(@PathVariable String nombre) {
        Path ruta = almacen.rutaDe(nombre);
        if (ruta == null) throw new NoEncontrado("La imagen no existe.");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(almacen.tipoMime(nombre)))
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                .body(new FileSystemResource(ruta));
    }
}
