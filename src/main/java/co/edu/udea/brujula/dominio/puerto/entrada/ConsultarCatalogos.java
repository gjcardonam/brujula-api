package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.Competencia;
import co.edu.udea.brujula.dominio.modelo.Componente;
import co.edu.udea.brujula.dominio.modelo.NivelDificultad;
import co.edu.udea.brujula.dominio.modelo.TipoError;

import java.util.List;

public interface ConsultarCatalogos {

    record Catalogos(List<Componente> componentes, List<Competencia> competencias, List<NivelDificultad> niveles,
                     List<TipoError> tiposDeError) {
    }

    Catalogos todos();
}
