package co.edu.udea.brujula.ejercicio;

import co.edu.udea.brujula.catalogo.CatalogoController.Item;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class EjercicioDtos {
    private EjercicioDtos() {}

    /** Tarjeta del banco (HU-006 CA-04/CA-06). `intentos` solo se llena para el administrador. */
    public record Tarjeta(Long id, Integer numero, String componente, String competencia, String nivel, String estado, Long intentos) {}

    public record ComponenteConteo(Long id, String nombre, long cantidad) {}

    public record Componentes(long total, List<ComponenteConteo> componentes) {}

    /** Opción tal como la ve el estudiante antes de responder (HU-009 CA-05, HU-020 CA-15). */
    public record OpcionEstudiante(Long id, String letra, String descripcion, String imagen, int orden) {}

    public record EjercicioEstudiante(Long id, Integer numero, String enunciado, String imagenEnunciado,
                                      Item componente, Item competencia, String nivel, String estado,
                                      List<OpcionEstudiante> opciones, long intentosPrevios) {}

    /** Opción completa, solo para el administrador (HU-022). */
    public record OpcionAdmin(Long id, String letra, String descripcion, String imagen, boolean esCorrecta,
                              String retroalimentacion, Item tipoError, int orden, boolean usadaEnIntentos) {}

    public record DistribucionError(String tipo, long cantidad, BigDecimal porcentaje) {}

    public record EstadisticasUso(long intentos, long correctos, BigDecimal porcentajeAciertos, List<DistribucionError> errores) {}

    public record EjercicioAdmin(Long id, Integer numero, String enunciado, String imagenEnunciado,
                                 Item componente, Item competencia, Item nivel, String estado,
                                 String creadoPor, Instant creadoEn, List<OpcionAdmin> opciones,
                                 EstadisticasUso uso, boolean tieneIntentos) {}

    public record OpcionRequest(Long id, String descripcion, String imagen, Boolean esCorrecta,
                                String retroalimentacion, Long idTipoError) {}

    public record EjercicioRequest(String enunciado, String imagenEnunciado, Long idComponente, Long idCompetencia,
                                   Long idNivelDificultad, List<OpcionRequest> opciones) {}

    public record CambioEstadoRequest(String estado) {}
}
