package co.edu.udea.brujula.aplicacion.catalogo;

import co.edu.udea.brujula.apoyo.dobles.CatalogosEnMemoria;
import co.edu.udea.brujula.dominio.modelo.Competencia;
import co.edu.udea.brujula.dominio.modelo.Componente;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarCatalogos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Consulta de catálogos")
class ConsultaDeCatalogosTest {

    private final ConsultaDeCatalogos consulta = new ConsultaDeCatalogos(new CatalogosEnMemoria());

    @Test
    void entrega_solo_los_componentes_y_competencias_activos() {
        ConsultarCatalogos.Catalogos catalogos = consulta.todos();

        assertTrue(catalogos.componentes().stream().allMatch(Componente::estaActivo));
        assertTrue(catalogos.competencias().stream().allMatch(Competencia::estaActiva));
        assertEquals(2, catalogos.componentes().size());
        assertEquals(2, catalogos.competencias().size());
    }

    @Test
    void entrega_todos_los_niveles_de_dificultad() {
        assertEquals(2, consulta.todos().niveles().size());
    }
}
