package co.edu.udea.brujula.apoyo.dobles;

import co.edu.udea.brujula.dominio.modelo.Intento;
import co.edu.udea.brujula.dominio.modelo.Opcion;
import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeIntento;
import co.edu.udea.brujula.dominio.modelo.consulta.ResumenDeIntento;
import co.edu.udea.brujula.dominio.modelo.consulta.UsoDelEjercicio;
import co.edu.udea.brujula.dominio.puerto.salida.IntentoRepositorio;
import co.edu.udea.brujula.dominio.servicio.Porcentajes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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

    @Override
    public Intento guardar(Intento intento) {
        if (fallarAlGuardar) throw new IllegalStateException("No se pudo guardar el intento");
        Intento conId = new Intento(siguienteId++, intento.respondidoEn(), intento.correcto(),
                intento.nivelConfianza(), intento.tipoError(), intento.idEstudiante(), intento.ejercicio(),
                intento.opcionSeleccionada(), intento.tokenIdempotencia());
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
    public UsoDelEjercicio usoDe(Long idEjercicio) {
        long total = guardados.stream()
                .filter(i -> i.ejercicio().id().equals(idEjercicio))
                .count();
        long correctos = guardados.stream()
                .filter(i -> i.ejercicio().id().equals(idEjercicio))
                .filter(i -> i.correcto())
                .count();
        return new UsoDelEjercicio(total, correctos, Porcentajes.de(correctos, total), List.of());
    }

    @Override
    public java.util.Set<Long> opcionesUsadas(Long idEjercicio) {
        java.util.Set<Long> usadas = new java.util.HashSet<>();
        guardados.stream()
                .filter(i -> i.ejercicio().id().equals(idEjercicio))
                .forEach(i -> usadas.add(i.opcionSeleccionada().id()));
        return usadas;
    }

    @Override
    public List<Boolean> ultimosResultados(Long idEstudiante, Long idComponente, Long idCompetencia, int limite) {
        return guardados.stream()
                .filter(i -> i.idEstudiante().equals(idEstudiante))
                .sorted((a, b) -> b.respondidoEn().compareTo(a.respondidoEn()))
                .limit(Math.max(1, limite))
                .map(Intento::correcto)
                .toList();
    }

    @Override
    public co.edu.udea.brujula.dominio.modelo.Pagina<co.edu.udea.brujula.dominio.modelo.consulta.ResumenDeIntento> historialDe(Long idEstudiante, int pagina, int tamano) {
        java.util.List<co.edu.udea.brujula.dominio.modelo.consulta.ResumenDeIntento> filas = guardados.stream()
                .filter(i -> i.idEstudiante().equals(idEstudiante))
                .sorted((a, b) -> b.respondidoEn().compareTo(a.respondidoEn()))
                .map(i -> new co.edu.udea.brujula.dominio.modelo.consulta.ResumenDeIntento(i.id(), i.ejercicio().id(),
                        i.ejercicio().numero(), i.ejercicio().componente().nombre(),
                        i.ejercicio().competencia().nombre(), i.ejercicio().nivel().nombre(), i.correcto(),
                        i.respondidoEn(), i.ejercicio().estado()))
                .toList();
        int desde = pagina * tamano;
        int hasta = Math.min(desde + tamano, filas.size());
        java.util.List<co.edu.udea.brujula.dominio.modelo.consulta.ResumenDeIntento> paginaContenido = desde >= filas.size()
                ? List.of()
                : filas.subList(desde, hasta);
        return co.edu.udea.brujula.dominio.modelo.Pagina.de(paginaContenido, pagina, tamano, filas.size());
    }

    @Override
    public java.util.Optional<co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeIntento> detalle(Long idIntento, Long idEstudiante) {
        return guardados.stream()
                .filter(i -> i.id().equals(idIntento) && i.idEstudiante().equals(idEstudiante))
                .findFirst()
                .map(i -> {
                    java.util.List<co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeIntento.OpcionRevisada> revisadas = i.ejercicio().opciones().stream()
                            .map(o -> new co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeIntento.OpcionRevisada(
                                    o.id(), o.letra(), o.texto(), o.imagen(), o.id().equals(i.opcionSeleccionada().id()),
                                    o.correcta()))
                            .toList();
                    String retro = i.opcionSeleccionada().retroalimentacion();
                    if (retro == null || retro.isBlank()) retro = co.edu.udea.brujula.dominio.modelo.Opcion.SIN_RETROALIMENTACION;
                    return new co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeIntento(i.id(), i.ejercicio().id(),
                            i.ejercicio().numero(), i.ejercicio().enunciado(), i.ejercicio().imagen(),
                            i.ejercicio().componente().nombre(), i.ejercicio().competencia().nombre(),
                            i.ejercicio().nivel().nombre(), i.ejercicio().estado(), revisadas, i.correcto(),
                            i.nivelConfianza(), i.respondidoEn(), retro);
                });
    }
}
