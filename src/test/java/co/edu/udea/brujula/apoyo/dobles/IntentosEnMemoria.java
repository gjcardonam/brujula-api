package co.edu.udea.brujula.apoyo.dobles;

import co.edu.udea.brujula.dominio.modelo.Intento;
import co.edu.udea.brujula.dominio.puerto.salida.IntentoRepositorio;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class IntentosEnMemoria implements IntentoRepositorio {

    private final List<Intento> guardados = new ArrayList<>();
    private long siguienteId = 1;
    private boolean fallarAlGuardar;

    public List<Intento> todos() {
        return guardados;
    }

    public void fallarAlGuardar() {
        fallarAlGuardar = true;
    }

    public Set<Long> ejerciciosRespondidosEn(Long idSimulacro) {
        return guardados.stream()
                .filter(intento -> idSimulacro.equals(intento.idSimulacro()))
                .map(intento -> intento.ejercicio().id())
                .collect(Collectors.toSet());
    }

    @Override
    public Intento guardar(Intento intento) {
        if (fallarAlGuardar) throw new IllegalStateException("No se pudo guardar el intento");
        Intento conId = new Intento(siguienteId++, intento.respondidoEn(), intento.correcto(),
                intento.nivelConfianza(), intento.idEstudiante(), intento.ejercicio(), intento.opcionSeleccionada(),
                intento.tokenIdempotencia(), intento.idSimulacro());
        guardados.add(conId);
        return conId;
    }

    @Override
    public Optional<Intento> porToken(UUID token) {
        return guardados.stream().filter(intento -> token.equals(intento.tokenIdempotencia())).findFirst();
    }

    @Override
    public long cantidadDeIntentos(Long idEstudiante, Long idEjercicio) {
        return guardados.stream()
                .filter(intento -> intento.idEstudiante().equals(idEstudiante))
                .filter(intento -> intento.ejercicio().id().equals(idEjercicio))
                .count();
    }

    @Override
    public boolean yaRespondidoEnSimulacro(Long idSimulacro, Long idEjercicio) {
        return ejerciciosRespondidosEn(idSimulacro).contains(idEjercicio);
    }

    @Override
    public List<Intento> delSimulacro(Long idSimulacro) {
        return guardados.stream().filter(intento -> idSimulacro.equals(intento.idSimulacro())).toList();
    }

    @Override
    public long respondidosEn(Long idSimulacro) {
        return delSimulacro(idSimulacro).size();
    }

    @Override
    public long correctasEn(Long idSimulacro) {
        return delSimulacro(idSimulacro).stream().filter(Intento::correcto).count();
    }

    @Override
    public List<Intento> deEstudiante(Long idEstudiante) {
        return guardados.stream().filter(intento -> intento.idEstudiante().equals(idEstudiante)).toList();
    }
}
