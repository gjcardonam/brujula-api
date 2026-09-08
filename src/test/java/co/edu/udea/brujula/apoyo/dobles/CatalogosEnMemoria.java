package co.edu.udea.brujula.apoyo.dobles;

import co.edu.udea.brujula.dominio.modelo.*;
import co.edu.udea.brujula.dominio.puerto.salida.CatalogoRepositorio;

import java.util.List;
import java.util.Optional;

public class CatalogosEnMemoria implements CatalogoRepositorio {

    private static final List<TipoError> TIPOS = List.of(
            new TipoError(1L, TipoError.COGNITIVO),
            new TipoError(2L, TipoError.HABITO),
            new TipoError(3L, TipoError.ANSIEDAD));

    private final List<DuracionSimulacro> duraciones = List.of(
            new DuracionSimulacro(1L, 30), new DuracionSimulacro(3L, 60));

    @Override
    public TipoError tipoDeError(String nombre) {
        return TIPOS.stream().filter(t -> t.nombre().equals(nombre)).findFirst().orElseThrow();
    }

    @Override
    public Optional<TipoError> tipoDeError(Long id) {
        return TIPOS.stream().filter(t -> t.id().equals(id)).findFirst();
    }

    @Override
    public List<TipoError> tiposDeError() {
        return TIPOS;
    }

    @Override
    public Optional<DuracionSimulacro> duracion(Long id) {
        return duraciones.stream().filter(d -> d.id().equals(id)).findFirst();
    }

    @Override
    public List<DuracionSimulacro> duraciones() {
        return duraciones;
    }

    @Override
    public Optional<Rol> rol(String nombre) {
        return Optional.of(Rol.ESTUDIANTE.equals(nombre) ? new Rol(2L, Rol.ESTUDIANTE) : new Rol(1L, Rol.ADMINISTRADOR));
    }

    @Override
    public List<Componente> componentes(boolean soloActivos) {
        return List.of();
    }

    @Override
    public Optional<Componente> componente(Long id) {
        return Optional.empty();
    }

    @Override
    public List<Competencia> competencias(boolean soloActivas) {
        return List.of();
    }

    @Override
    public Optional<Competencia> competencia(Long id) {
        return Optional.empty();
    }

    @Override
    public List<NivelDificultad> niveles() {
        return List.of();
    }

    @Override
    public Optional<NivelDificultad> nivel(Long id) {
        return Optional.empty();
    }
}
