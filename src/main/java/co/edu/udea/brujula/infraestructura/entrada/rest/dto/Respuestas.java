package co.edu.udea.brujula.infraestructura.entrada.rest.dto;

import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.Recomendacion;
import co.edu.udea.brujula.dominio.modelo.SesionIniciada;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.modelo.consulta.DesempenoPorArea;
import co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeIntento;
import co.edu.udea.brujula.dominio.modelo.consulta.EstadoDelSimulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.ResultadoDeIntento;
import co.edu.udea.brujula.dominio.modelo.consulta.ResultadoDeSimulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.SiguienteDelSimulacro;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarCatalogos;
import co.edu.udea.brujula.dominio.puerto.salida.AlmacenDeImagenes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class Respuestas {

    private Respuestas() {
    }

    public record Item(Long id, String nombre) {
    }

    public record MensajeDto(String mensaje) {
    }

    public record UsuarioDto(Long id, String nombre, String apellido, String email, String rol) {

        public static UsuarioDto de(Usuario usuario) {
            return new UsuarioDto(usuario.id(), usuario.nombre(), usuario.apellido(), usuario.email(),
                    usuario.rol().nombre());
        }
    }

    public record SesionDto(String token, Instant expiraEn, UsuarioDto usuario) {

        public static SesionDto de(SesionIniciada sesion) {
            return new SesionDto(sesion.token(), sesion.expiraEn(), UsuarioDto.de(sesion.usuario()));
        }
    }

    public record PaginaDto<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

        public static <T> PaginaDto<T> de(Pagina<T> pagina) {
            return new PaginaDto<>(pagina.contenido(), pagina.pagina(), pagina.tamano(), pagina.totalElementos(),
                    pagina.totalPaginas());
        }
    }

    public record DuracionDto(Long id, int minutos) {
    }

    public record CatalogosDto(List<Item> componentes, List<Item> competencias, List<Item> niveles,
                               List<DuracionDto> duraciones) {

        public static CatalogosDto de(ConsultarCatalogos.Catalogos catalogos) {
            return new CatalogosDto(
                    catalogos.componentes().stream().map(c -> new Item(c.id(), c.nombre())).toList(),
                    catalogos.competencias().stream().map(c -> new Item(c.id(), c.nombre())).toList(),
                    catalogos.niveles().stream().map(n -> new Item(n.id(), n.nombre())).toList(),
                    catalogos.duraciones().stream().map(d -> new DuracionDto(d.id(), d.minutos())).toList());
        }
    }

    public record OpcionEstudianteDto(Long id, String letra, String descripcion, String imagen, int orden) {
    }

    public record EjercicioEstudianteDto(Long id, Integer numero, String enunciado, String imagenEnunciado,
                                         Item componente, Item competencia, String nivel, String estado,
                                         List<OpcionEstudianteDto> opciones, long intentosPrevios) {

        public static EjercicioEstudianteDto de(Ejercicio ejercicio, long intentosPrevios,
                                                AlmacenDeImagenes imagenes) {
            List<OpcionEstudianteDto> opciones = ejercicio.opciones().stream()
                    .map(o -> new OpcionEstudianteDto(o.id(), o.letra(), o.texto(), imagenes.urlDe(o.imagen()),
                            o.orden()))
                    .toList();
            return new EjercicioEstudianteDto(ejercicio.id(), ejercicio.numero(), ejercicio.enunciado(),
                    imagenes.urlDe(ejercicio.imagen()),
                    new Item(ejercicio.componente().id(), ejercicio.componente().nombre()),
                    new Item(ejercicio.competencia().id(), ejercicio.competencia().nombre()),
                    ejercicio.nivel().nombre(), ejercicio.estado(), opciones, intentosPrevios);
        }
    }

    public record OpcionAdminDto(Long id, String letra, String descripcion, String imagen, boolean esCorrecta,
                                 String retroalimentacion, int orden) {
    }

    public record EjercicioAdminDto(Long id, Integer numero, String enunciado, String imagenEnunciado,
                                    Item componente, Item competencia, Item nivel, String estado, String creadoPor,
                                    Instant creadoEn, List<OpcionAdminDto> opciones) {

        public static EjercicioAdminDto de(Ejercicio ejercicio, AlmacenDeImagenes imagenes) {
            List<OpcionAdminDto> opciones = ejercicio.opciones().stream()
                    .map(o -> new OpcionAdminDto(o.id(), o.letra(), o.texto(), imagenes.urlDe(o.imagen()),
                            o.correcta(), o.retroalimentacion(), o.orden()))
                    .toList();
            return new EjercicioAdminDto(ejercicio.id(), ejercicio.numero(), ejercicio.enunciado(),
                    imagenes.urlDe(ejercicio.imagen()),
                    new Item(ejercicio.componente().id(), ejercicio.componente().nombre()),
                    new Item(ejercicio.competencia().id(), ejercicio.competencia().nombre()),
                    new Item(ejercicio.nivel().id(), ejercicio.nivel().nombre()),
                    ejercicio.estado(), ejercicio.nombreCreador(), ejercicio.creadoEn(), opciones);
        }
    }

    public record ResultadoDeIntentoDto(Long idIntento, boolean esCorrecto, Long idOpcionSeleccionada,
                                        Long idOpcionCorrecta, String retroalimentacion,
                                        boolean retroalimentacionDisponible, int nivelConfianza,
                                        Instant respondidoEn, Instant fechaHora, long numeroIntento,
                                        boolean repetido, Long idSimulacro) {

        public static ResultadoDeIntentoDto de(ResultadoDeIntento resultado) {
            return new ResultadoDeIntentoDto(resultado.idIntento(), resultado.esCorrecto(),
                    resultado.idOpcionSeleccionada(), resultado.idOpcionCorrecta(), resultado.retroalimentacion(),
                    resultado.retroalimentacionDisponible(), resultado.nivelConfianza(), resultado.respondidoEn(),
                    resultado.respondidoEn(), resultado.numeroIntento(), resultado.repetido(),
                    resultado.idSimulacro());
        }
    }

    public record SiguienteSimulacroDto(boolean finalizado, String motivo, EjercicioEstudianteDto ejercicio,
                                        EstadoDelSimulacro estado) {

        public static SiguienteSimulacroDto de(SiguienteDelSimulacro siguiente, AlmacenDeImagenes imagenes) {
            EjercicioEstudianteDto ejercicio = siguiente.ejercicio() == null ? null
                    : EjercicioEstudianteDto.de(siguiente.ejercicio(), 0, imagenes);
            return new SiguienteSimulacroDto(siguiente.finalizado(), siguiente.motivo(), ejercicio,
                    siguiente.estado());
        }
    }

    public record RecomendacionDto(int orden, String area, String tipo, Long idComponente, Long idCompetencia,
                                   BigDecimal porcentaje, String mensaje) {

        public static RecomendacionDto de(Recomendacion recomendacion) {
            return new RecomendacionDto(recomendacion.orden(), recomendacion.area(), recomendacion.tipoDeArea(),
                    recomendacion.idComponente(), recomendacion.idCompetencia(), recomendacion.porcentaje(),
                    recomendacion.mensaje());
        }
    }

    public record ResultadoSimulacroDto(Long id, Instant fechaInicio, Instant fechaFin, int duracionMinutos,
                                        Integer tiempoUtilizadoSeg, long respondidos, long correctas,
                                        long incorrectas, BigDecimal porcentajeAciertos,
                                        List<DesempenoPorArea> porComponente, List<DesempenoPorArea> porCompetencia,
                                        List<RecomendacionDto> recomendaciones, String mensajeRecomendacion,
                                        List<DetalleDeIntento> detalle) {

        public static ResultadoSimulacroDto de(ResultadoDeSimulacro resultado, AlmacenDeImagenes imagenes) {
            return new ResultadoSimulacroDto(resultado.id(), resultado.fechaInicio(), resultado.fechaFin(),
                    resultado.duracionMinutos(), resultado.tiempoUtilizadoSeg(), resultado.respondidos(),
                    resultado.correctas(), resultado.incorrectas(), resultado.porcentajeAciertos(),
                    resultado.porComponente(), resultado.porCompetencia(),
                    resultado.recomendaciones().stream().map(RecomendacionDto::de).toList(),
                    resultado.mensajeRecomendacion(),
                    resultado.detalle().stream().map(detalle -> conImagenes(detalle, imagenes)).toList());
        }

        private static DetalleDeIntento conImagenes(DetalleDeIntento detalle, AlmacenDeImagenes imagenes) {
            List<DetalleDeIntento.OpcionRevisada> opciones = detalle.opciones().stream()
                    .map(o -> new DetalleDeIntento.OpcionRevisada(o.id(), o.letra(), o.descripcion(),
                            imagenes.urlDe(o.imagen()), o.seleccionada(), o.esCorrecta()))
                    .toList();
            return new DetalleDeIntento(detalle.id(), detalle.idEjercicio(), detalle.numeroEjercicio(),
                    detalle.enunciado(), imagenes.urlDe(detalle.imagenEnunciado()), detalle.componente(),
                    detalle.competencia(), detalle.nivel(), detalle.estadoEjercicio(), opciones,
                    detalle.esCorrecto(), detalle.nivelConfianza(), detalle.fechaHora(),
                    detalle.retroalimentacion(), detalle.idSimulacro());
        }
    }
}
