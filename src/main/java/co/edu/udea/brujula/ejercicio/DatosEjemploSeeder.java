package co.edu.udea.brujula.ejercicio;

import co.edu.udea.brujula.catalogo.*;
import co.edu.udea.brujula.config.BrujulaProperties;
import co.edu.udea.brujula.usuario.Usuario;
import co.edu.udea.brujula.usuario.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Carga un banco de ejemplo (datos-ejemplo/ejercicios.json) y un estudiante de prueba cuando
 * brujula.datos-ejemplo=true y el banco está vacío. Pensado para desarrollo y demostraciones.
 */
@Component
@Order(2)
public class DatosEjemploSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosEjemploSeeder.class);

    public record OpcionJson(String descripcion, boolean esCorrecta, String retroalimentacion, String tipoError) {}
    public record EjercicioJson(String enunciado, String componente, String competencia, String nivel, List<OpcionJson> opciones) {}

    private final BrujulaProperties props;
    private final EjercicioRepository ejercicios;
    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final ComponenteRepository componentes;
    private final CompetenciaRepository competencias;
    private final NivelDificultadRepository niveles;
    private final TipoErrorRepository tiposError;
    private final PasswordEncoder encoder;
    private final ObjectMapper json;

    public DatosEjemploSeeder(BrujulaProperties props, EjercicioRepository ejercicios, UsuarioRepository usuarios, RolRepository roles,
                              ComponenteRepository componentes, CompetenciaRepository competencias, NivelDificultadRepository niveles,
                              TipoErrorRepository tiposError, PasswordEncoder encoder, ObjectMapper json) {
        this.props = props;
        this.ejercicios = ejercicios;
        this.usuarios = usuarios;
        this.roles = roles;
        this.componentes = componentes;
        this.competencias = competencias;
        this.niveles = niveles;
        this.tiposError = tiposError;
        this.encoder = encoder;
        this.json = json;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (!props.datosEjemplo()) return;
        if (!usuarios.existsByEmailIgnoreCase("estudiante@brujula.local")) {
            Usuario e = new Usuario();
            e.setNombre("Ana María");
            e.setApellido("Pérez Gómez");
            e.setEmail("estudiante@brujula.local");
            e.setPasswordHash(encoder.encode("Estudiante.2026"));
            e.setAceptoTerminos(true);
            e.setFechaAceptacionTerminos(Instant.now());
            e.setRol(roles.findByNombreRol("Estudiante").orElseThrow());
            usuarios.save(e);
            log.info("Estudiante de prueba creado: estudiante@brujula.local / Estudiante.2026");
        }
        if (ejercicios.count() > 0) return;
        Usuario admin = usuarios.findAll().stream().filter(u -> "Administrador".equals(u.getRol().getNombreRol())).findFirst().orElse(null);
        if (admin == null) {
            log.warn("No hay administrador; no se cargan ejercicios de ejemplo.");
            return;
        }
        Map<String, Componente> comps = componentes.findAll().stream().collect(Collectors.toMap(Componente::getNombre, Function.identity()));
        Map<String, Competencia> compes = competencias.findAll().stream().collect(Collectors.toMap(Competencia::getNombre, Function.identity()));
        Map<String, NivelDificultad> nivs = niveles.findAll().stream().collect(Collectors.toMap(NivelDificultad::getNivel, Function.identity()));
        Map<String, TipoError> tipos = tiposError.findAll().stream().collect(Collectors.toMap(TipoError::getNombre, Function.identity()));

        List<EjercicioJson> lista;
        try (InputStream in = new ClassPathResource("datos-ejemplo/ejercicios.json").getInputStream()) {
            lista = json.readValue(in, json.getTypeFactory().constructCollectionType(List.class, EjercicioJson.class));
        }
        int n = 0;
        for (EjercicioJson ej : lista) {
            Ejercicio e = new Ejercicio();
            e.setEnunciado(ej.enunciado());
            e.setComponente(requerir(comps, ej.componente(), "componente"));
            e.setCompetencia(requerir(compes, ej.competencia(), "competencia"));
            e.setNivelDificultad(requerir(nivs, ej.nivel(), "nivel"));
            e.setCreador(admin);
            e.setEstado(Ejercicio.ACTIVO);
            int orden = 1;
            for (OpcionJson o : ej.opciones()) {
                OpcionRespuesta op = new OpcionRespuesta();
                op.setEjercicio(e);
                op.setDescripcion(o.descripcion());
                op.setEsCorrecta(o.esCorrecta());
                op.setRetroalimentacion(o.retroalimentacion());
                op.setTipoError(o.esCorrecta() || o.tipoError() == null ? null : requerir(tipos, o.tipoError(), "tipo de error"));
                op.setOrden(orden++);
                e.getOpciones().add(op);
            }
            ejercicios.save(e);
            n++;
        }
        log.info("Se cargaron {} ejercicios de ejemplo", n);
    }

    private static <T> T requerir(Map<String, T> mapa, String clave, String que) {
        T v = mapa.get(clave);
        if (v == null) throw new IllegalStateException("Datos de ejemplo: " + que + " desconocido '" + clave + "'");
        return v;
    }
}
