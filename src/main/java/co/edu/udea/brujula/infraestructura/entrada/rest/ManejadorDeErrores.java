package co.edu.udea.brujula.infraestructura.entrada.rest;

import co.edu.udea.brujula.dominio.excepcion.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Traduce los errores del dominio a respuestas HTTP. Es el único punto donde se decide qué código
 * corresponde a cada situación, para que el dominio no tenga que saber de HTTP.
 */
@RestControllerAdvice
public class ManejadorDeErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorDeErrores.class);

    @ExceptionHandler(ErrorDeNegocio.class)
    public ResponseEntity<Map<String, Object>> negocio(ErrorDeNegocio error) {
        HttpStatus estado = estadoPara(error);
        Map<String, Object> cuerpo = cuerpo(estado, error.codigo(), error.getMessage(), error.detalles());
        cuerpo.putAll(error.datos());
        return ResponseEntity.status(estado).body(cuerpo);
    }

    private HttpStatus estadoPara(ErrorDeNegocio error) {
        if (error instanceof CredencialesInvalidas) return HttpStatus.UNAUTHORIZED;
        if (error instanceof CuentaBloqueada) return HttpStatus.LOCKED;
        if (error instanceof AccesoDenegado) return HttpStatus.FORBIDDEN;
        if (error instanceof NoEncontrado) return HttpStatus.NOT_FOUND;
        if (error instanceof Conflicto) return HttpStatus.CONFLICT;
        if (error instanceof RecursoNoDisponible) return HttpStatus.GONE;
        if (error instanceof ServicioNoDisponible) return HttpStatus.SERVICE_UNAVAILABLE;
        return HttpStatus.BAD_REQUEST;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacionDeFormato(MethodArgumentNotValidException e) {
        List<String> detalles = e.getBindingResult().getAllErrors().stream()
                .map(error -> error instanceof FieldError campo
                        ? campo.getField() + ": " + campo.getDefaultMessage()
                        : error.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest()
                .body(cuerpo(HttpStatus.BAD_REQUEST, "VALIDACION", "Revisa los datos ingresados.", detalles));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> cuerpoIlegible(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(cuerpo(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA",
                "El cuerpo de la solicitud no es válido.", List.of()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> archivoMuyGrande(MaxUploadSizeExceededException e) {
        return ResponseEntity.badRequest().body(cuerpo(HttpStatus.BAD_REQUEST, "ARCHIVO_INVALIDO",
                "La imagen supera el tamaño máximo de 5 MB.", List.of()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> permisos(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(cuerpo(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO",
                "No tienes permisos para realizar esta acción.", List.of()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> rutaInexistente(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(cuerpo(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", "Recurso no encontrado.", List.of()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> inesperado(Exception e) {
        log.error("Error no controlado", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(cuerpo(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO",
                        "Ocurrió un error inesperado. Intenta de nuevo.", List.of()));
    }

    private Map<String, Object> cuerpo(HttpStatus estado, String codigo, String mensaje, List<String> detalles) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("estado", estado.value());
        cuerpo.put("codigo", codigo);
        cuerpo.put("mensaje", mensaje);
        cuerpo.put("detalles", detalles);
        return cuerpo;
    }
}
