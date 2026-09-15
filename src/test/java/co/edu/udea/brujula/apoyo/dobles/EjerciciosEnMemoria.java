package co.edu.udea.brujula.apoyo.dobles;

import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.consulta.ComponentesDelBanco;
import co.edu.udea.brujula.dominio.puerto.salida.EjercicioRepositorio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EjerciciosEnMemoria implements EjercicioRepositorio {

    private final Map<Long, Ejercicio> porId = new LinkedHashMap<>();
    private long siguienteId = 1;

    public Ejercicio agregar(Ejercicio ejercicio) {
        porId.put(ejercicio.id(), ejercicio);
        return ejercicio;
    }

    public List<Ejercicio> todos() {
        return List.copyOf(porId.values());
    }

    @Override
    public Optional<Ejercicio> porId(Long id) {
        return Optional.ofNullable(porId.get(id));
    }

    @Override
    public Pagina<Ejercicio> buscar(Long idComponente, boolean soloActivos, int pagina, int tamano) {
        List<Ejercicio> encontrados = porId.values().stream()
                .filter(ejercicio -> !soloActivos || ejercicio.estaActivo())
                .filter(ejercicio -> idComponente == null || ejercicio.componente().id().equals(idComponente))
                .toList();
        int desde = Math.min(pagina * tamano, encontrados.size());
        int hasta = Math.min(desde + tamano, encontrados.size());
        return Pagina.de(new ArrayList<>(encontrados.subList(desde, hasta)), pagina, tamano, encontrados.size());
    }

    @Override
    public ComponentesDelBanco conteoPorComponente(boolean soloActivos) {
        List<Ejercicio> visibles = porId.values().stream()
                .filter(ejercicio -> !soloActivos || ejercicio.estaActivo())
                .toList();
        Map<Long, ComponentesDelBanco.Conteo> conteos = new LinkedHashMap<>();
        visibles.forEach(ejercicio -> conteos.merge(ejercicio.componente().id(),
                new ComponentesDelBanco.Conteo(ejercicio.componente().id(), ejercicio.componente().nombre(), 1),
                (previo, nuevo) -> new ComponentesDelBanco.Conteo(previo.id(), previo.nombre(),
                        previo.cantidad() + 1)));
        return new ComponentesDelBanco(visibles.size(), List.copyOf(conteos.values()));
    }

    @Override
    public boolean existeConEnunciado(String enunciado) {
        return porId.values().stream()
                .anyMatch(ejercicio -> ejercicio.enunciado().trim().equalsIgnoreCase(enunciado.trim()));
    }

    @Override
    public Ejercicio guardar(Ejercicio ejercicio) {
        long id = ejercicio.id() == null ? siguienteId++ : ejercicio.id();
        Ejercicio conId = new Ejercicio(id, (int) id, ejercicio.enunciado(), ejercicio.imagen(), ejercicio.nivel(),
                ejercicio.componente(), ejercicio.competencia(), ejercicio.estado(), ejercicio.creadoEn(),
                ejercicio.idCreador(), ejercicio.nombreCreador(), ejercicio.opciones());
        return agregar(conId);
    }

    @Override
    public Optional<Long> siguienteParaPractica(Long idActual, Long idComponente, Long idEstudiante) {
        return porId.values().stream()
                .filter(Ejercicio::estaActivo)
                .filter(ejercicio -> !ejercicio.id().equals(idActual))
                .filter(ejercicio -> idComponente == null || ejercicio.componente().id().equals(idComponente))
                .map(Ejercicio::id)
                .findFirst();
    }
}
