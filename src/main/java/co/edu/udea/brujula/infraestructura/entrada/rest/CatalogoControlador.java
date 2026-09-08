package co.edu.udea.brujula.infraestructura.entrada.rest;

import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarCatalogos;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Respuestas.CatalogosDto;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Respuestas.Item;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catalogos")
public class CatalogoControlador {

    private final ConsultarCatalogos catalogos;

    public CatalogoControlador(ConsultarCatalogos catalogos) {
        this.catalogos = catalogos;
    }

    @GetMapping
    public CatalogosDto todos() {
        return CatalogosDto.de(catalogos.todos());
    }

    /** Lo poco que necesita la pantalla de acceso, antes de iniciar sesión. */
    @GetMapping("/publicos")
    public Map<String, Object> publicos() {
        List<Item> componentes = catalogos.componentesActivos().stream()
                .map(c -> new Item(c.id(), c.nombre())).toList();
        return Map.of("componentes", componentes);
    }
}
