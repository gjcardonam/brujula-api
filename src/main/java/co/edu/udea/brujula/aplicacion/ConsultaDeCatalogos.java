package co.edu.udea.brujula.aplicacion;

import co.edu.udea.brujula.dominio.modelo.Componente;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarCatalogos;
import co.edu.udea.brujula.dominio.puerto.salida.CatalogoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConsultaDeCatalogos implements ConsultarCatalogos {

    private final CatalogoRepositorio catalogos;

    public ConsultaDeCatalogos(CatalogoRepositorio catalogos) {
        this.catalogos = catalogos;
    }

    @Override
    @Transactional(readOnly = true)
    public Catalogos todos() {
        return new Catalogos(catalogos.componentes(true), catalogos.competencias(true), catalogos.niveles(),
                catalogos.tiposDeError(), catalogos.duraciones());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Componente> componentesActivos() {
        return catalogos.componentes(true);
    }
}
