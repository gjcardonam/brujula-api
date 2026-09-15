package co.edu.udea.brujula.apoyo.dobles;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.dominio.modelo.Competencia;
import co.edu.udea.brujula.dominio.modelo.Componente;
import co.edu.udea.brujula.dominio.modelo.NivelDificultad;
import co.edu.udea.brujula.dominio.modelo.Rol;
import co.edu.udea.brujula.dominio.puerto.salida.CatalogoRepositorio;

import java.util.List;
import java.util.Optional;

public class CatalogosEnMemoria implements CatalogoRepositorio {

    private final List<Componente> componentes = List.of(Datos.ALGEBRA, Datos.GEOMETRIA,
            new Componente(9L, "Componente retirado", "Desactivado"));
    private final List<Competencia> competencias = List.of(Datos.INTERPRETACION, Datos.ARGUMENTACION,
            new Competencia(9L, "Competencia retirada", "Desactivado"));
    private final List<NivelDificultad> niveles = List.of(Datos.BASICO, new NivelDificultad(2L, "Intermedio"));

    @Override
    public Optional<Rol> rol(String nombre) {
        return Rol.ESTUDIANTE.equals(nombre) ? Optional.of(Datos.ESTUDIANTE) : Optional.of(Datos.ADMINISTRADOR);
    }

    @Override
    public List<Componente> componentes(boolean soloActivos) {
        return soloActivos ? componentes.stream().filter(Componente::estaActivo).toList() : componentes;
    }

    @Override
    public Optional<Componente> componente(Long id) {
        return componentes.stream().filter(c -> c.id().equals(id)).findFirst();
    }

    @Override
    public List<Competencia> competencias(boolean soloActivas) {
        return soloActivas ? competencias.stream().filter(Competencia::estaActiva).toList() : competencias;
    }

    @Override
    public Optional<Competencia> competencia(Long id) {
        return competencias.stream().filter(c -> c.id().equals(id)).findFirst();
    }

    @Override
    public List<NivelDificultad> niveles() {
        return niveles;
    }

    @Override
    public Optional<NivelDificultad> nivel(Long id) {
        return niveles.stream().filter(n -> n.id().equals(id)).findFirst();
    }
}
