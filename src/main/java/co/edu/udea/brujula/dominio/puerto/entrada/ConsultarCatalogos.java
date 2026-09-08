package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.*;

import java.util.List;

/** Listas que llenan los desplegables del front. */
public interface ConsultarCatalogos {

    record Catalogos(List<Componente> componentes, List<Competencia> competencias, List<NivelDificultad> niveles,
                     List<TipoError> tiposDeError, List<DuracionSimulacro> duraciones) {
    }

    Catalogos todos();

    List<Componente> componentesActivos();
}
