package co.edu.udea.brujula.apoyo.dobles;

import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.consulta.ComponentesDelBanco;
import co.edu.udea.brujula.dominio.puerto.salida.EjercicioRepositorio;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class EjerciciosEnMemoria implements EjercicioRepositorio {

    private final Map<Long, Ejercicio> porId = new LinkedHashMap<>();

    public Ejercicio agregar(Ejercicio ejercicio) {
        porId.put(ejercicio.id(), ejercicio);
        return ejercicio;
    }

    @Override
    public Optional<Ejercicio> porId(Long id) {
        return Optional.ofNullable(porId.get(id));
    }

    @Override
    public long cantidadDeActivos() {
        return porId.values().stream().filter(Ejercicio::estaActivo).count();
    }

    @Override
    public Optional<Long> siguienteParaSimulacro(Long idSimulacro) {
        return porId.values().stream().filter(Ejercicio::estaActivo).map(Ejercicio::id).findFirst();
    }

    @Override
    public Pagina<Ejercicio> buscar(Long idComponente, boolean soloActivos, int pagina, int tamano) {
        throw new UnsupportedOperationException("No hace falta en estas pruebas");
    }

    @Override
    public ComponentesDelBanco conteoPorComponente(boolean soloActivos) {
        throw new UnsupportedOperationException("No hace falta en estas pruebas");
    }

    @Override
    public boolean existeOtroConEnunciado(String enunciado, Long idExcluido) {
        return false;
    }

    @Override
    public Ejercicio guardar(Ejercicio ejercicio) {
        return agregar(ejercicio);
    }

    @Override
    public Optional<Long> siguienteParaPractica(Long idActual, Long idComponente, Long idEstudiante) {
        return porId.values().stream()
                .filter(Ejercicio::estaActivo)
                .filter(e -> !e.id().equals(idActual))
                .map(Ejercicio::id)
                .findFirst();
    }
}
