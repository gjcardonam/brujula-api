package co.edu.udea.brujula.dominio.puerto.salida;

import co.edu.udea.brujula.dominio.modelo.*;

import java.util.List;
import java.util.Optional;

/**
 * Catálogos del sistema. En PI1 se cargan directamente en la base de datos; administrarlos desde la
 * interfaz es alcance de PI II.
 */
public interface CatalogoRepositorio {

    Optional<Rol> rol(String nombre);

    List<Componente> componentes(boolean soloActivos);

    Optional<Componente> componente(Long id);

    List<Competencia> competencias(boolean soloActivas);

    Optional<Competencia> competencia(Long id);

    List<NivelDificultad> niveles();

    Optional<NivelDificultad> nivel(Long id);

    List<TipoError> tiposDeError();

    Optional<TipoError> tipoDeError(Long id);

    TipoError tipoDeError(String nombre);

    List<DuracionSimulacro> duraciones();

    Optional<DuracionSimulacro> duracion(Long id);
}
