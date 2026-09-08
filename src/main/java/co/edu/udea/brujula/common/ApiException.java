package co.edu.udea.brujula.common;

import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

/** Error de negocio con código estable para que el front pueda reaccionar. */
public class ApiException extends RuntimeException {
    private final HttpStatus estado;
    private final String codigo;
    private final List<String> detalles;
    private final Map<String, Object> extra;

    public ApiException(HttpStatus estado, String codigo, String mensaje) {
        this(estado, codigo, mensaje, List.of(), Map.of());
    }

    public ApiException(HttpStatus estado, String codigo, String mensaje, List<String> detalles) {
        this(estado, codigo, mensaje, detalles, Map.of());
    }

    public ApiException(HttpStatus estado, String codigo, String mensaje, List<String> detalles, Map<String, Object> extra) {
        super(mensaje);
        this.estado = estado;
        this.codigo = codigo;
        this.detalles = detalles;
        this.extra = extra;
    }

    public static ApiException badRequest(String codigo, String mensaje) { return new ApiException(HttpStatus.BAD_REQUEST, codigo, mensaje); }
    public static ApiException validacion(List<String> detalles) {
        return new ApiException(HttpStatus.BAD_REQUEST, "VALIDACION", "Revisa los datos ingresados.", detalles);
    }
    public static ApiException noEncontrado(String mensaje) { return new ApiException(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", mensaje); }
    public static ApiException conflicto(String codigo, String mensaje) { return new ApiException(HttpStatus.CONFLICT, codigo, mensaje); }
    public static ApiException prohibido(String mensaje) { return new ApiException(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO", mensaje); }
    public static ApiException noDisponible(String mensaje) { return new ApiException(HttpStatus.GONE, "NO_DISPONIBLE", mensaje); }

    public HttpStatus getEstado() { return estado; }
    public String getCodigo() { return codigo; }
    public List<String> getDetalles() { return detalles; }
    public Map<String, Object> getExtra() { return extra; }
}
