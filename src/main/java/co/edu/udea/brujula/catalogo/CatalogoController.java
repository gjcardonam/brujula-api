package co.edu.udea.brujula.catalogo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catalogos")
public class CatalogoController {

    private final ComponenteRepository componentes;
    private final CompetenciaRepository competencias;
    private final NivelDificultadRepository niveles;
    private final TipoErrorRepository tiposError;
    private final DuracionSimulacroRepository duraciones;

    public CatalogoController(ComponenteRepository componentes, CompetenciaRepository competencias,
                              NivelDificultadRepository niveles, TipoErrorRepository tiposError,
                              DuracionSimulacroRepository duraciones) {
        this.componentes = componentes;
        this.competencias = competencias;
        this.niveles = niveles;
        this.tiposError = tiposError;
        this.duraciones = duraciones;
    }

    public record Item(Long id, String nombre) {}
    public record Duracion(Long id, int minutos) {}

    @GetMapping
    public Map<String, Object> catalogos() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("componentes", componentes.findByEstadoOrderByIdAsc("Activo").stream().map(c -> new Item(c.getId(), c.getNombre())).toList());
        m.put("competencias", competencias.findByEstadoOrderByIdAsc("Activo").stream().map(c -> new Item(c.getId(), c.getNombre())).toList());
        m.put("niveles", niveles.findAllByOrderByIdAsc().stream().map(n -> new Item(n.getId(), n.getNivel())).toList());
        m.put("tiposError", tiposError.findAllByOrderByIdAsc().stream().map(t -> new Item(t.getId(), t.getNombre())).toList());
        m.put("duraciones", duraciones.findAllByOrderByDuracionMinutosAsc().stream().map(d -> new Duracion(d.getId(), d.getDuracionMinutos())).toList());
        return m;
    }

    /** Lo mínimo que necesita la pantalla de acceso sin sesión. */
    @GetMapping("/publicos")
    public Map<String, Object> publicos() {
        return Map.of("componentes", componentes.findByEstadoOrderByIdAsc("Activo").stream().map(c -> new Item(c.getId(), c.getNombre())).toList());
    }
}
