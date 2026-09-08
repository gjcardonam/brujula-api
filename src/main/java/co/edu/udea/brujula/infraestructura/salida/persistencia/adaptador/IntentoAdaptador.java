package co.edu.udea.brujula.infraestructura.salida.persistencia.adaptador;

import co.edu.udea.brujula.dominio.excepcion.Conflicto;
import co.edu.udea.brujula.dominio.modelo.Intento;
import co.edu.udea.brujula.dominio.modelo.Opcion;
import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.consulta.*;
import co.edu.udea.brujula.dominio.puerto.salida.AlmacenDeImagenes;
import co.edu.udea.brujula.dominio.puerto.salida.IntentoRepositorio;
import co.edu.udea.brujula.dominio.servicio.Porcentajes;
import co.edu.udea.brujula.infraestructura.salida.persistencia.Mapeador;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.EjercicioEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.IntentoEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.OpcionEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.EjercicioJpa;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.IntentoJpa;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.OpcionJpa;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.TipoErrorJpa;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class IntentoAdaptador implements IntentoRepositorio {

    private final IntentoJpa intentos;
    private final EjercicioJpa ejercicios;
    private final OpcionJpa opciones;
    private final TipoErrorJpa tiposDeError;
    private final AlmacenDeImagenes imagenes;

    public IntentoAdaptador(IntentoJpa intentos, EjercicioJpa ejercicios, OpcionJpa opciones,
                            TipoErrorJpa tiposDeError, AlmacenDeImagenes imagenes) {
        this.intentos = intentos;
        this.ejercicios = ejercicios;
        this.opciones = opciones;
        this.tiposDeError = tiposDeError;
        this.imagenes = imagenes;
    }

    @Override
    public Intento guardar(Intento intento) {
        IntentoEntidad entidad = new IntentoEntidad();
        entidad.setFechaHora(intento.fechaHora());
        entidad.setCorrecto(intento.correcto());
        entidad.setNivelConfianza(intento.nivelConfianza());
        entidad.setIdUsuario(intento.idEstudiante());
        entidad.setEjercicio(ejercicios.getReferenceById(intento.ejercicio().id()));
        entidad.setOpcionSeleccionada(opciones.getReferenceById(intento.opcionSeleccionada().id()));
        entidad.setIdSimulacro(intento.idSimulacro());
        entidad.setTokenIdempotencia(intento.tokenIdempotencia());
        if (intento.tipoError() != null) {
            entidad.setTipoError(tiposDeError.getReferenceById(intento.tipoError().id()));
        }
        try {
            IntentoEntidad guardado = intentos.saveAndFlush(entidad);
            return new Intento(guardado.getId(), intento.fechaHora(), intento.correcto(), intento.nivelConfianza(),
                    intento.tipoError(), intento.idEstudiante(), intento.ejercicio(), intento.opcionSeleccionada(),
                    intento.idSimulacro(), intento.tokenIdempotencia());
        } catch (DataIntegrityViolationException e) {
            // Dos envíos simultáneos con el mismo token: gana el primero y se devuelve ese.
            if (intento.tokenIdempotencia() != null) {
                Optional<Intento> yaGuardado = porToken(intento.tokenIdempotencia());
                if (yaGuardado.isPresent()) return yaGuardado.get();
            }
            throw new Conflicto("INTENTO_DUPLICADO", "No fue posible registrar la respuesta. Intenta de nuevo.");
        }
    }

    @Override
    public Optional<Intento> porToken(UUID token) {
        return intentos.findByTokenIdempotencia(token).map(Mapeador::aDominio);
    }

    @Override
    public long cantidadDeIntentos(Long idEstudiante, Long idEjercicio) {
        return intentos.countByIdUsuarioAndEjercicio_Id(idEstudiante, idEjercicio);
    }

    @Override
    public Map<Long, Long> conteoPorEjercicio(List<Long> idsDeEjercicios) {
        if (idsDeEjercicios.isEmpty()) return Map.of();
        return intentos.contarPorEjercicio(idsDeEjercicios).stream()
                .collect(Collectors.toMap(fila -> (Long) fila[0], fila -> (Long) fila[1]));
    }

    @Override
    public UsoDelEjercicio usoDe(Long idEjercicio) {
        long total = intentos.countByEjercicio_Id(idEjercicio);
        long correctos = total == 0 ? 0 : intentos.countByEjercicio_IdAndCorrectoTrue(idEjercicio);
        long incorrectos = total - correctos;
        List<DistribucionDeError> errores = intentos.erroresPorEjercicio(idEjercicio).stream()
                .map(fila -> new DistribucionDeError((String) fila[0], (Long) fila[1],
                        Porcentajes.de((Long) fila[1], incorrectos)))
                .toList();
        return new UsoDelEjercicio(total, correctos, Porcentajes.de(correctos, total), errores);
    }

    @Override
    public Set<Long> opcionesUsadas(Long idEjercicio) {
        return new HashSet<>(intentos.opcionesUsadas(idEjercicio));
    }

    @Override
    public boolean yaRespondidoEnSimulacro(Long idSimulacro, Long idEjercicio) {
        return intentos.existsByIdSimulacroAndEjercicio_Id(idSimulacro, idEjercicio);
    }

    @Override
    public List<Boolean> ultimosResultados(Long idEstudiante, Long idComponente, Long idCompetencia, int limite) {
        return intentos.ultimosResultados(idEstudiante, idComponente, idCompetencia, PageRequest.of(0, Math.max(1, limite)));
    }

    @Override
    public Pagina<ResumenDeIntento> historialDe(Long idEstudiante, int pagina, int tamano) {
        Page<IntentoEntidad> encontrados = intentos.findByIdUsuarioOrderByFechaHoraDescIdDesc(
                idEstudiante, PageRequest.of(pagina, tamano));
        List<ResumenDeIntento> filas = encontrados.getContent().stream().map(i -> {
            EjercicioEntidad e = i.getEjercicio();
            return new ResumenDeIntento(i.getId(), e.getId(), e.getNumero(), e.getComponente().getNombre(),
                    e.getCompetencia().getNombre(), e.getNivel().getNivel(), i.isCorrecto(), i.getFechaHora(),
                    e.getEstado(), i.getIdSimulacro() != null);
        }).toList();
        return Pagina.de(filas, encontrados.getNumber(), encontrados.getSize(), encontrados.getTotalElements());
    }

    @Override
    public Optional<DetalleDeIntento> detalle(Long idIntento, Long idEstudiante) {
        return intentos.findByIdAndIdUsuario(idIntento, idEstudiante).map(this::aDetalle);
    }

    @Override
    public List<DetalleDeIntento> detalleDelSimulacro(Long idSimulacro) {
        return intentos.findByIdSimulacroOrderByFechaHoraAscIdAsc(idSimulacro).stream().map(this::aDetalle).toList();
    }

    private DetalleDeIntento aDetalle(IntentoEntidad intento) {
        EjercicioEntidad ejercicio = intento.getEjercicio();
        OpcionEntidad elegida = intento.getOpcionSeleccionada();
        List<DetalleDeIntento.OpcionRevisada> revisadas = ejercicio.getOpciones().stream()
                .sorted(Comparator.comparingInt(OpcionEntidad::getOrden))
                .map(o -> new DetalleDeIntento.OpcionRevisada(o.getId(),
                        String.valueOf((char) ('A' + Math.max(0, o.getOrden() - 1))),
                        o.getTexto(), imagenes.urlDe(o.getImagen()),
                        o.getId().equals(elegida.getId()), o.isCorrecta()))
                .toList();
        String retroalimentacion = elegida.getRetroalimentacion() == null || elegida.getRetroalimentacion().isBlank()
                ? Opcion.SIN_RETROALIMENTACION
                : elegida.getRetroalimentacion();
        return new DetalleDeIntento(intento.getId(), ejercicio.getId(), ejercicio.getNumero(), ejercicio.getEnunciado(),
                imagenes.urlDe(ejercicio.getImagen()), ejercicio.getComponente().getNombre(),
                ejercicio.getCompetencia().getNombre(), ejercicio.getNivel().getNivel(), ejercicio.getEstado(),
                revisadas, intento.isCorrecto(), intento.getNivelConfianza(), intento.getFechaHora(),
                retroalimentacion, intento.getIdSimulacro());
    }

    @Override
    public List<Intento> delSimulacro(Long idSimulacro) {
        return intentos.findByIdSimulacroOrderByFechaHoraAscIdAsc(idSimulacro).stream()
                .map(Mapeador::aDominio).toList();
    }

    @Override
    public long respondidosEn(Long idSimulacro) {
        return intentos.countByIdSimulacro(idSimulacro);
    }

    @Override
    public long correctasEn(Long idSimulacro) {
        return intentos.countByIdSimulacroAndCorrectoTrue(idSimulacro);
    }

    @Override
    public List<Intento> deEstudiante(Long idEstudiante) {
        return intentos.findByIdUsuario(idEstudiante).stream().map(Mapeador::aDominio).toList();
    }
}
