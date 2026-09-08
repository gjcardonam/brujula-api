package co.edu.udea.brujula.infraestructura.salida.persistencia.adaptador;

import co.edu.udea.brujula.dominio.modelo.*;
import co.edu.udea.brujula.dominio.puerto.salida.CatalogoRepositorio;
import co.edu.udea.brujula.infraestructura.salida.persistencia.Mapeador;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CatalogoAdaptador implements CatalogoRepositorio {

    private static final String ACTIVO = "Activo";

    private final RolJpa roles;
    private final ComponenteJpa componentes;
    private final CompetenciaJpa competencias;
    private final NivelDificultadJpa niveles;
    private final TipoErrorJpa tiposDeError;
    private final DuracionSimulacroJpa duraciones;

    public CatalogoAdaptador(RolJpa roles, ComponenteJpa componentes, CompetenciaJpa competencias,
                             NivelDificultadJpa niveles, TipoErrorJpa tiposDeError, DuracionSimulacroJpa duraciones) {
        this.roles = roles;
        this.componentes = componentes;
        this.competencias = competencias;
        this.niveles = niveles;
        this.tiposDeError = tiposDeError;
        this.duraciones = duraciones;
    }

    @Override
    public Optional<Rol> rol(String nombre) {
        return roles.findByNombre(nombre).map(Mapeador::aDominio);
    }

    @Override
    public List<Componente> componentes(boolean soloActivos) {
        var encontrados = soloActivos ? componentes.findByEstadoOrderByIdAsc(ACTIVO) : componentes.findAllByOrderByIdAsc();
        return encontrados.stream().map(Mapeador::aDominio).toList();
    }

    @Override
    public Optional<Componente> componente(Long id) {
        return componentes.findById(id).map(Mapeador::aDominio);
    }

    @Override
    public List<Competencia> competencias(boolean soloActivas) {
        var encontradas = soloActivas ? competencias.findByEstadoOrderByIdAsc(ACTIVO) : competencias.findAllByOrderByIdAsc();
        return encontradas.stream().map(Mapeador::aDominio).toList();
    }

    @Override
    public Optional<Competencia> competencia(Long id) {
        return competencias.findById(id).map(Mapeador::aDominio);
    }

    @Override
    public List<NivelDificultad> niveles() {
        return niveles.findAllByOrderByIdAsc().stream().map(Mapeador::aDominio).toList();
    }

    @Override
    public Optional<NivelDificultad> nivel(Long id) {
        return niveles.findById(id).map(Mapeador::aDominio);
    }

    @Override
    public List<TipoError> tiposDeError() {
        return tiposDeError.findAllByOrderByIdAsc().stream().map(Mapeador::aDominio).toList();
    }

    @Override
    public Optional<TipoError> tipoDeError(Long id) {
        return tiposDeError.findById(id).map(Mapeador::aDominio);
    }

    @Override
    public TipoError tipoDeError(String nombre) {
        return tiposDeError.findByNombre(nombre).map(Mapeador::aDominio)
                .orElseThrow(() -> new IllegalStateException("Falta el tipo de error '" + nombre + "' en la base de datos"));
    }

    @Override
    public List<DuracionSimulacro> duraciones() {
        return duraciones.findAllByOrderByMinutosAsc().stream().map(Mapeador::aDominio).toList();
    }

    @Override
    public Optional<DuracionSimulacro> duracion(Long id) {
        return duraciones.findById(id).map(Mapeador::aDominio);
    }
}
