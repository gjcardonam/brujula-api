package co.edu.udea.brujula.infraestructura.entrada.rest.dto;

import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.SesionIniciada;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarCatalogos;
import co.edu.udea.brujula.dominio.puerto.salida.AlmacenDeImagenes;

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

    public record CatalogosDto(List<Item> componentes, List<Item> competencias, List<Item> niveles) {

        public static CatalogosDto de(ConsultarCatalogos.Catalogos catalogos) {
            return new CatalogosDto(
                    catalogos.componentes().stream().map(c -> new Item(c.id(), c.nombre())).toList(),
                    catalogos.competencias().stream().map(c -> new Item(c.id(), c.nombre())).toList(),
                    catalogos.niveles().stream().map(n -> new Item(n.id(), n.nombre())).toList());
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
}
