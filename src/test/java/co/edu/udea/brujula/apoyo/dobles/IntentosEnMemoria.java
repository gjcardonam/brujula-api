package co.edu.udea.brujula.apoyo.dobles;

import co.edu.udea.brujula.dominio.modelo.Intento;
import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeIntento;
import co.edu.udea.brujula.dominio.modelo.consulta.ResumenDeIntento;
import co.edu.udea.brujula.dominio.modelo.consulta.UsoDelEjercicio;
import co.edu.udea.brujula.dominio.puerto.salida.IntentoRepositorio;

import java.util.*;

/**
 * Solo implementa lo que usan los casos de uso bajo prueba; el resto avisa si alguien lo llama sin
 * darse cuenta.
 */
public class IntentosEnMemoria implements IntentoRepositorio {

    private final List<Intento> guardados = new ArrayList<>();
    private long siguienteId = 1;

    public List<Intento> todos() {
        return guardados;
    }

    public void precargar(Intento intento) {
        guardados.add(intento);
    }

    @Override
    public Intento guardar(Intento intento) {
        Intento conId = new Intento(siguienteId++, intento.fechaHora(), intento.correcto(), intento.nivelConfianza(),
                intento.tipoError(), intento.idEstudiante(), intento.ejercicio(), intento.opcionSeleccionada(),
                intento.idSimulacro(), intento.tokenIdempotencia());
        guardados.add(conId);
        return conId;
    }

    @Override
    public Optional<Intento> porToken(UUID token) {
        return guardados.stream().filter(i -> token.equals(i.tokenIdempotencia())).findFirst();
    }

    @Override
    public long cantidadDeIntentos(Long idEstudiante, Long idEjercicio) {
        return guardados.stream()
                .filter(i -> i.idEstudiante().equals(idEstudiante) && i.ejercicio().id().equals(idEjercicio))
                .count();
    }

    @Override
    public List<Boolean> ultimosResultados(Long idEstudiante, Long idComponente, Long idCompetencia, int limite) {
        return guardados.stream()
                .filter(i -> i.idEstudiante().equals(idEstudiante))
                .filter(i -> i.ejercicio().componente().id().equals(idComponente)
                        || i.ejercicio().competencia().id().equals(idCompetencia))
                .sorted(Comparator.comparing(Intento::fechaHora).reversed())
                .limit(limite)
                .map(Intento::correcto)
                .toList();
    }

    @Override
    public boolean yaRespondidoEnSimulacro(Long idSimulacro, Long idEjercicio) {
        return guardados.stream()
                .anyMatch(i -> idSimulacro.equals(i.idSimulacro()) && i.ejercicio().id().equals(idEjercicio));
    }

    @Override
    public long respondidosEn(Long idSimulacro) {
        return guardados.stream().filter(i -> idSimulacro.equals(i.idSimulacro())).count();
    }

    @Override
    public long correctasEn(Long idSimulacro) {
        return guardados.stream().filter(i -> idSimulacro.equals(i.idSimulacro()) && i.correcto()).count();
    }

    @Override
    public List<Intento> delSimulacro(Long idSimulacro) {
        return guardados.stream().filter(i -> idSimulacro.equals(i.idSimulacro())).toList();
    }

    @Override
    public List<Intento> deEstudiante(Long idEstudiante) {
        return guardados.stream().filter(i -> i.idEstudiante().equals(idEstudiante)).toList();
    }

    @Override
    public Map<Long, Long> conteoPorEjercicio(List<Long> idsDeEjercicios) {
        throw new UnsupportedOperationException("No hace falta en estas pruebas");
    }

    @Override
    public UsoDelEjercicio usoDe(Long idEjercicio) {
        throw new UnsupportedOperationException("No hace falta en estas pruebas");
    }

    @Override
    public Set<Long> opcionesUsadas(Long idEjercicio) {
        return Set.of();
    }

    @Override
    public Pagina<ResumenDeIntento> historialDe(Long idEstudiante, int pagina, int tamano) {
        throw new UnsupportedOperationException("No hace falta en estas pruebas");
    }

    @Override
    public Optional<DetalleDeIntento> detalle(Long idIntento, Long idEstudiante) {
        throw new UnsupportedOperationException("No hace falta en estas pruebas");
    }

    @Override
    public List<DetalleDeIntento> detalleDelSimulacro(Long idSimulacro) {
        return List.of();
    }
}
