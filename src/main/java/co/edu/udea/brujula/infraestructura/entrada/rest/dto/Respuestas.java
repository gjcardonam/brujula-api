package co.edu.udea.brujula.infraestructura.entrada.rest.dto;

import co.edu.udea.brujula.dominio.modelo.*;
import co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeEjercicio;
import co.edu.udea.brujula.dominio.modelo.consulta.ResultadoDeSimulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.SiguienteDelSimulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.UsoDelEjercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarCatalogos;
import co.edu.udea.brujula.dominio.puerto.salida.AlmacenDeImagenes;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Lo que la API devuelve. Está separado del dominio a propósito: aquí se decide qué se le muestra
 * a cada rol, sobre todo para no filtrarle al estudiante cuál es la respuesta correcta.
 */
public final class Respuestas {

    private Respuestas() {
    }

    public record Item(Long id, String nombre) {
    }

    public record UsuarioDto(Long id, String nombre, String apellido, String email, String rol) {
        public static UsuarioDto de(Usuario u) {
            return new UsuarioDto(u.id(), u.nombre(), u.apellido(), u.email(), u.rol().nombre());
        }
    }

    public record SesionDto(String token, Instant expiraEn, UsuarioDto usuario) {
        public static SesionDto de(SesionIniciada sesion) {
            return new SesionDto(sesion.token(), sesion.expiraEn(), UsuarioDto.de(sesion.usuario()));
        }
    }

    public record MensajeDto(String mensaje) {
    }

    public record PaginaDto<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {
        public static <T> PaginaDto<T> de(Pagina<T> pagina) {
            return new PaginaDto<>(pagina.contenido(), pagina.pagina(), pagina.tamano(),
                    pagina.totalElementos(), pagina.totalPaginas());
        }
    }

    public record DuracionDto(Long id, int minutos) {
    }

    public record CatalogosDto(List<Item> componentes, List<Item> competencias, List<Item> niveles,
                               List<Item> tiposError, List<DuracionDto> duraciones) {

        public static CatalogosDto de(ConsultarCatalogos.Catalogos catalogos) {
            return new CatalogosDto(
                    catalogos.componentes().stream().map(c -> new Item(c.id(), c.nombre())).toList(),
                    catalogos.competencias().stream().map(c -> new Item(c.id(), c.nombre())).toList(),
                    catalogos.niveles().stream().map(n -> new Item(n.id(), n.nombre())).toList(),
                    catalogos.tiposDeError().stream().map(t -> new Item(t.id(), t.nombre())).toList(),
                    catalogos.duraciones().stream().map(d -> new DuracionDto(d.id(), d.minutos())).toList());
        }
    }

    /** Opción como la ve el estudiante: sin decir cuál es la correcta ni su retroalimentación. */
    public record OpcionEstudianteDto(Long id, String letra, String descripcion, String imagen, int orden) {
    }

    public record EjercicioEstudianteDto(Long id, Integer numero, String enunciado, String imagenEnunciado,
                                         Item componente, Item competencia, String nivel, String estado,
                                         List<OpcionEstudianteDto> opciones, long intentosPrevios) {

        public static EjercicioEstudianteDto de(Ejercicio e, long intentosPrevios, AlmacenDeImagenes imagenes) {
            List<OpcionEstudianteDto> opciones = e.opciones().stream()
                    .map(o -> new OpcionEstudianteDto(o.id(), o.letra(), o.texto(), imagenes.urlDe(o.imagen()), o.orden()))
                    .toList();
            return new EjercicioEstudianteDto(e.id(), e.numero(), e.enunciado(), imagenes.urlDe(e.imagen()),
                    new Item(e.componente().id(), e.componente().nombre()),
                    new Item(e.competencia().id(), e.competencia().nombre()),
                    e.nivel().nombre(), e.estado(), opciones, intentosPrevios);
        }
    }

    public record OpcionAdminDto(Long id, String letra, String descripcion, String imagen, boolean esCorrecta,
                                 String retroalimentacion, Item tipoError, int orden, boolean usadaEnIntentos) {
    }

    public record EjercicioAdminDto(Long id, Integer numero, String enunciado, String imagenEnunciado,
                                    Item componente, Item competencia, Item nivel, String estado, String creadoPor,
                                    Instant creadoEn, List<OpcionAdminDto> opciones, UsoDelEjercicio uso,
                                    boolean tieneIntentos) {

        public static EjercicioAdminDto de(DetalleDeEjercicio detalle, AlmacenDeImagenes imagenes) {
            Ejercicio e = detalle.ejercicio();
            Set<Long> usadas = detalle.opcionesUsadas();
            List<OpcionAdminDto> opciones = e.opciones().stream()
                    .map(o -> new OpcionAdminDto(o.id(), o.letra(), o.texto(), imagenes.urlDe(o.imagen()),
                            o.correcta(), o.retroalimentacion(),
                            o.tipoError() == null ? null : new Item(o.tipoError().id(), o.tipoError().nombre()),
                            o.orden(), usadas.contains(o.id())))
                    .toList();
            return new EjercicioAdminDto(e.id(), e.numero(), e.enunciado(), imagenes.urlDe(e.imagen()),
                    new Item(e.componente().id(), e.componente().nombre()),
                    new Item(e.competencia().id(), e.competencia().nombre()),
                    new Item(e.nivel().id(), e.nivel().nombre()),
                    e.estado(), e.nombreCreador(), e.creadoEn(), opciones, detalle.uso(), detalle.tieneIntentos());
        }
    }

    public record RecomendacionDto(int orden, String area, String tipo, Long idComponente, Long idCompetencia,
                                   java.math.BigDecimal porcentaje, String mensaje) {

        public static RecomendacionDto de(Recomendacion r) {
            return new RecomendacionDto(r.orden(), r.area(), r.tipoDeArea(), r.idComponente(), r.idCompetencia(),
                    r.porcentaje(), r.mensaje());
        }
    }

    public record ResultadoSimulacroDto(Long id, Instant fechaInicio, Instant fechaFin, int duracionMinutos,
                                        Integer tiempoUtilizadoSeg, long respondidos, long correctas, long incorrectas,
                                        java.math.BigDecimal porcentajeAciertos,
                                        List<co.edu.udea.brujula.dominio.modelo.consulta.DesempenoPorArea> porComponente,
                                        List<co.edu.udea.brujula.dominio.modelo.consulta.DesempenoPorArea> porCompetencia,
                                        List<RecomendacionDto> recomendaciones, String mensajeRecomendacion,
                                        List<co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeIntento> detalle) {

        public static ResultadoSimulacroDto de(ResultadoDeSimulacro r) {
            return new ResultadoSimulacroDto(r.id(), r.fechaInicio(), r.fechaFin(), r.duracionMinutos(),
                    r.tiempoUtilizadoSeg(), r.respondidos(), r.correctas(), r.incorrectas(), r.porcentajeAciertos(),
                    r.porComponente(), r.porCompetencia(),
                    r.recomendaciones().stream().map(RecomendacionDto::de).toList(),
                    r.mensajeRecomendacion(), r.detalle());
        }
    }

    public record SiguienteSimulacroDto(boolean finalizado, String motivo, EjercicioEstudianteDto ejercicio,
                                        co.edu.udea.brujula.dominio.modelo.consulta.EstadoDelSimulacro estado) {

        public static SiguienteSimulacroDto de(SiguienteDelSimulacro siguiente, AlmacenDeImagenes imagenes) {
            EjercicioEstudianteDto ejercicio = siguiente.ejercicio() == null ? null
                    : EjercicioEstudianteDto.de(siguiente.ejercicio(), 0, imagenes);
            return new SiguienteSimulacroDto(siguiente.finalizado(), siguiente.motivo(), ejercicio, siguiente.estado());
        }
    }
}
