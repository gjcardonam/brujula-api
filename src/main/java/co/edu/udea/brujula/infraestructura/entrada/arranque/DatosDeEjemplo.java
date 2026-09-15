package co.edu.udea.brujula.infraestructura.entrada.arranque;

import co.edu.udea.brujula.dominio.modelo.Competencia;
import co.edu.udea.brujula.dominio.modelo.Componente;
import co.edu.udea.brujula.dominio.modelo.NivelDificultad;
import co.edu.udea.brujula.dominio.modelo.Rol;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarCatalogos;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarComponentesDelBanco;
import co.edu.udea.brujula.dominio.puerto.entrada.CrearEjercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.comando.DatosDeEjercicio;
import co.edu.udea.brujula.dominio.puerto.salida.CatalogoRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.CifradorDeContrasenas;
import co.edu.udea.brujula.dominio.puerto.salida.Reloj;
import co.edu.udea.brujula.dominio.puerto.salida.UsuarioRepositorio;
import co.edu.udea.brujula.infraestructura.configuracion.BrujulaProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Order(2)
public class DatosDeEjemplo implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosDeEjemplo.class);
    private static final String CORREO_DE_PRUEBA = "estudiante@brujula.local";
    private static final String CLAVE_DE_PRUEBA = "Estudiante.2026";
    private static final String ARCHIVO = "datos-ejemplo/ejercicios.json";

    public record EjercicioJson(String enunciado, String componente, String competencia, String nivel,
                                List<OpcionJson> opciones) {
    }

    public record OpcionJson(String descripcion, boolean esCorrecta, String retroalimentacion) {
    }

    private final BrujulaProperties propiedades;
    private final ConsultarComponentesDelBanco banco;
    private final ConsultarCatalogos catalogos;
    private final CrearEjercicio creacion;
    private final UsuarioRepositorio usuarios;
    private final CatalogoRepositorio catalogoRepositorio;
    private final CifradorDeContrasenas cifrador;
    private final Reloj reloj;
    private final ObjectMapper json;

    public DatosDeEjemplo(BrujulaProperties propiedades, ConsultarComponentesDelBanco banco,
                          ConsultarCatalogos catalogos, CrearEjercicio creacion, UsuarioRepositorio usuarios,
                          CatalogoRepositorio catalogoRepositorio, CifradorDeContrasenas cifrador, Reloj reloj,
                          ObjectMapper json) {
        this.propiedades = propiedades;
        this.banco = banco;
        this.catalogos = catalogos;
        this.creacion = creacion;
        this.usuarios = usuarios;
        this.catalogoRepositorio = catalogoRepositorio;
        this.cifrador = cifrador;
        this.reloj = reloj;
        this.json = json;
    }

    @Override
    public void run(ApplicationArguments argumentos) throws Exception {
        if (!propiedades.datosEjemplo()) return;

        crearEstudianteDePrueba();

        if (banco.componentes(true).total() > 0) return;
        Usuario administrador = usuarios.porEmail(propiedades.admin().email()).orElse(null);
        if (administrador == null || !administrador.esAdministrador()) {
            log.warn("No hay administrador; no se cargan ejercicios de ejemplo.");
            return;
        }
        cargarEjercicios(administrador.id());
    }

    private void crearEstudianteDePrueba() {
        if (usuarios.existeConEmail(CORREO_DE_PRUEBA)) return;
        Rol estudiante = catalogoRepositorio.rol(Rol.ESTUDIANTE).orElseThrow();
        usuarios.guardar(Usuario.crear("Ana María", "Pérez Gómez", CORREO_DE_PRUEBA, null,
                cifrador.cifrar(CLAVE_DE_PRUEBA), estudiante, reloj.ahora()));
        log.info("Estudiante de prueba creado: {} / {}", CORREO_DE_PRUEBA, CLAVE_DE_PRUEBA);
    }

    private void cargarEjercicios(Long idAdministrador) throws Exception {
        ConsultarCatalogos.Catalogos listas = catalogos.todos();
        Map<String, Long> componentes = indexar(listas.componentes(), Componente::nombre, Componente::id);
        Map<String, Long> competencias = indexar(listas.competencias(), Competencia::nombre, Competencia::id);
        Map<String, Long> niveles = indexar(listas.niveles(), NivelDificultad::nombre, NivelDificultad::id);

        List<EjercicioJson> ejercicios = leerArchivo();
        for (EjercicioJson ejercicio : ejercicios) {
            List<DatosDeEjercicio.DatosDeOpcion> opciones = ejercicio.opciones().stream()
                    .map(opcion -> new DatosDeEjercicio.DatosDeOpcion(opcion.descripcion(), null,
                            opcion.esCorrecta(), opcion.retroalimentacion()))
                    .toList();
            creacion.crear(idAdministrador, new DatosDeEjercicio(ejercicio.enunciado(), null,
                    requerir(componentes, ejercicio.componente(), "componente"),
                    requerir(competencias, ejercicio.competencia(), "competencia"),
                    requerir(niveles, ejercicio.nivel(), "nivel"), opciones));
        }
        log.info("Se cargaron {} ejercicios de ejemplo", ejercicios.size());
    }

    private List<EjercicioJson> leerArchivo() throws Exception {
        try (InputStream archivo = new ClassPathResource(ARCHIVO).getInputStream()) {
            return json.readValue(archivo,
                    json.getTypeFactory().constructCollectionType(List.class, EjercicioJson.class));
        }
    }

    private static <T> Map<String, Long> indexar(List<T> elementos, Function<T, String> nombre,
                                                 Function<T, Long> id) {
        return elementos.stream().collect(Collectors.toMap(nombre, id));
    }

    private static Long requerir(Map<String, Long> catalogo, String nombre, String queEs) {
        Long id = catalogo.get(nombre);
        if (id == null) {
            throw new IllegalStateException("Datos de ejemplo: " + queEs + " desconocido '" + nombre + "'");
        }
        return id;
    }
}
