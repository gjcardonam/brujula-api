package co.edu.udea.brujula.dominio.puerto.salida;

import co.edu.udea.brujula.dominio.modelo.Competencia;
import co.edu.udea.brujula.dominio.modelo.Componente;
import co.edu.udea.brujula.dominio.modelo.NivelDificultad;
import co.edu.udea.brujula.dominio.modelo.Rol;

import java.util.List;
import java.util.Optional;

public interface CatalogoRepositorio {

    Optional<Rol> rol(String nombre);

    List<Componente> componentes(boolean soloActivos);

    Optional<Componente> componente(Long id);

    List<Competencia> competencias(boolean soloActivas);

    Optional<Competencia> competencia(Long id);

    List<NivelDificultad> niveles();

    Optional<NivelDificultad> nivel(Long id);
}
