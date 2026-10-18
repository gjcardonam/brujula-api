package co.edu.udea.brujula.infraestructura.salida.persistencia.adaptador;

import co.edu.udea.brujula.dominio.modelo.Competencia;
import co.edu.udea.brujula.dominio.modelo.Componente;
import co.edu.udea.brujula.dominio.modelo.NivelDificultad;
import co.edu.udea.brujula.dominio.modelo.Rol;
import co.edu.udea.brujula.dominio.puerto.salida.CatalogoRepositorio;
import co.edu.udea.brujula.infraestructura.salida.persistencia.Mapeador;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.CompetenciaJpa;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.ComponenteJpa;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.NivelDificultadJpa;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.RolJpa;
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

    public CatalogoAdaptador(RolJpa roles, ComponenteJpa componentes, CompetenciaJpa competencias,
                             NivelDificultadJpa niveles) {
        this.roles = roles;
        this.componentes = componentes;
        this.competencias = competencias;
        this.niveles = niveles;
    }

    @Override
    public Optional<Rol> rol(String nombre) {
        return roles.findByNombre(nombre).map(Mapeador::aDominio);
    }

    @Override
    public List<Componente> componentes(boolean soloActivos) {
        var encontrados = soloActivos
                ? componentes.findByEstadoOrderByIdAsc(ACTIVO)
                : componentes.findAllByOrderByIdAsc();
        return encontrados.stream().map(Mapeador::aDominio).toList();
    }

    @Override
    public Optional<Componente> componente(Long id) {
        return componentes.findById(id).map(Mapeador::aDominio);
    }

    @Override
    public List<Competencia> competencias(boolean soloActivas) {
        var encontradas = soloActivas
                ? competencias.findByEstadoOrderByIdAsc(ACTIVO)
                : competencias.findAllByOrderByIdAsc();
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
}
