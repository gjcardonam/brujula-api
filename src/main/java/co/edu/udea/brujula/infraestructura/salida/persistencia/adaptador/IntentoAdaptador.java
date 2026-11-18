package co.edu.udea.brujula.infraestructura.salida.persistencia.adaptador;

import co.edu.udea.brujula.dominio.excepcion.Conflicto;
import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.modelo.Intento;
import co.edu.udea.brujula.dominio.modelo.Opcion;
import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeIntento;
import co.edu.udea.brujula.dominio.modelo.consulta.DistribucionDeError;
import co.edu.udea.brujula.dominio.modelo.consulta.ResumenDeIntento;
import co.edu.udea.brujula.dominio.modelo.consulta.UsoDelEjercicio;
import co.edu.udea.brujula.dominio.puerto.salida.AlmacenDeImagenes;
import co.edu.udea.brujula.dominio.puerto.salida.IntentoRepositorio;
import co.edu.udea.brujula.dominio.servicio.LimiteDeReintentos;
import co.edu.udea.brujula.dominio.servicio.Porcentajes;
import co.edu.udea.brujula.infraestructura.salida.persistencia.Mapeador;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.EjercicioEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.IntentoEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.OpcionEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.CompetenciaJpa;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.ComponenteJpa;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.EjercicioJpa;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.IntentoJpa;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.NivelDificultadJpa;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.OpcionJpa;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.TipoErrorJpa;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class IntentoAdaptador implements IntentoRepositorio {

    private final IntentoJpa intentos;
    private final EjercicioJpa ejercicios;
    private final OpcionJpa opciones;
    private final TipoErrorJpa tiposDeError;
    private final ComponenteJpa componentes;
    private final CompetenciaJpa competencias;
    private final NivelDificultadJpa niveles;
    private final AlmacenDeImagenes imagenes;

    public IntentoAdaptador(IntentoJpa intentos, EjercicioJpa ejercicios, OpcionJpa opciones,
                            TipoErrorJpa tiposDeError, ComponenteJpa componentes, CompetenciaJpa competencias,
                            NivelDificultadJpa niveles, AlmacenDeImagenes imagenes) {
        this.intentos = intentos;
        this.ejercicios = ejercicios;
        this.opciones = opciones;
        this.tiposDeError = tiposDeError;
        this.componentes = componentes;
        this.competencias = competencias;
        this.niveles = niveles;
        this.imagenes = imagenes;
    }

    @Override
    public Intento guardar(Intento intento) {
        IntentoEntidad entidad = new IntentoEntidad();
        entidad.setRespondidoEn(intento.respondidoEn());
        entidad.setCorrecto(intento.correcto());
        entidad.setNivelConfianza((short) intento.nivelConfianza());
        entidad.setIdUsuario(intento.idEstudiante());
        entidad.setEjercicio(ejercicios.getReferenceById(intento.ejercicio().id()));
        entidad.setOpcionSeleccionada(opciones.getReferenceById(intento.opcionSeleccionada().id()));
        entidad.setTokenIdempotencia(intento.tokenIdempotencia());
        entidad.setTiempoRespuestaMs(intento.tiempoRespuestaMs());
        if (intento.tipoError() != null) {
            entidad.setTipoError(tiposDeError.getReferenceById(intento.tipoError().id()));
        }
        entidad.setIdSimulacro(intento.idSimulacro());
        Ejercicio ejercicio = intento.ejercicio();
        entidad.setComponente(componentes.getReferenceById(ejercicio.componente().id()));
        entidad.setCompetencia(competencias.getReferenceById(ejercicio.competencia().id()));
        entidad.setNivel(niveles.getReferenceById(ejercicio.nivel().id()));
        entidad.setEnunciado(ejercicio.enunciado());
        entidad.setImagenEnunciado(ejercicio.imagen());
        entidad.setRetroalimentacion(intento.opcionSeleccionada().retroalimentacion());
        try {
            IntentoEntidad guardado = intentos.saveAndFlush(entidad);
            return new Intento(guardado.getId(), intento.respondidoEn(), intento.correcto(),
                    intento.nivelConfianza(), intento.tipoError(), intento.idEstudiante(), intento.ejercicio(),
                    intento.opcionSeleccionada(), intento.tokenIdempotencia(), intento.tiempoRespuestaMs(),
                    intento.idSimulacro());
        } catch (DataIntegrityViolationException e) {
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
    public LimiteDeReintentos.Practica practicaDe(Long idEstudiante, Long idEjercicio) {
        Object[] fila = intentos.practicaDe(idEstudiante, idEjercicio).get(0);
        return new LimiteDeReintentos.Practica((Long) fila[0], (Instant) fila[1]);
    }

    @Override
    public UsoDelEjercicio usoDe(Long idEjercicio) {
        long total = intentos.countByEjercicio_Id(idEjercicio);
        long correctos = total == 0 ? 0 : intentos.countByEjercicio_IdAndCorrectoTrue(idEjercicio);
        long incorrectos = total - correctos;
        List<co.edu.udea.brujula.dominio.modelo.consulta.DistribucionDeError> errores =
                intentos.erroresPorEjercicio(idEjercicio).stream()
                        .map(fila -> new co.edu.udea.brujula.dominio.modelo.consulta.DistribucionDeError(
                                (String) fila[0], (Long) fila[1], Porcentajes.de((Long) fila[1], incorrectos)))
                        .toList();
        return new UsoDelEjercicio(total, correctos, Porcentajes.de(correctos, total), errores);
    }

    @Override
    public Set<Long> opcionesUsadas(Long idEjercicio) {
        return new HashSet<>(intentos.opcionesUsadas(idEjercicio));
    }

    @Override
    public List<Boolean> ultimosResultados(Long idEstudiante, Long idComponente, Long idCompetencia, int limite) {
        return intentos.ultimosResultados(idEstudiante, idComponente, idCompetencia,
                PageRequest.of(0, Math.max(1, limite)));
    }

    @Override
    public Pagina<co.edu.udea.brujula.dominio.modelo.consulta.ResumenDeIntento> historialDe(Long idEstudiante,
                                                                                           int pagina, int tamano) {
        Page<IntentoEntidad> encontrados = intentos.findByIdUsuarioOrderByRespondidoEnDescIdDesc(
                idEstudiante, PageRequest.of(pagina, tamano));
        List<co.edu.udea.brujula.dominio.modelo.consulta.ResumenDeIntento> filas =
                encontrados.getContent().stream().map(intento -> {
                    EjercicioEntidad ejercicio = intento.getEjercicio();
                    return new co.edu.udea.brujula.dominio.modelo.consulta.ResumenDeIntento(intento.getId(),
                            ejercicio.getId(), ejercicio.getNumero(), intento.getComponente().getNombre(),
                            intento.getCompetencia().getNombre(), intento.getNivel().getNivel(),
                            intento.isCorrecto(), intento.getRespondidoEn(), ejercicio.getEstado());
                }).toList();
        return Pagina.de(filas, encontrados.getNumber(), encontrados.getSize(), encontrados.getTotalElements());
    }

    @Override
    public Optional<co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeIntento> detalle(Long idIntento,
                                                                                          Long idEstudiante) {
        return intentos.findByIdAndIdUsuario(idIntento, idEstudiante).map(this::aDetalle);
    }

    private co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeIntento aDetalle(IntentoEntidad intento) {
        EjercicioEntidad ejercicio = intento.getEjercicio();
        OpcionEntidad elegida = intento.getOpcionSeleccionada();
        List<co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeIntento.OpcionRevisada> revisadas =
                ejercicio.getOpciones().stream()
                        .sorted(Comparator.comparingInt(OpcionEntidad::getOrden))
                        .map(opcion -> new co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeIntento.OpcionRevisada(
                                opcion.getId(),
                                String.valueOf((char) ('A' + Math.max(0, opcion.getOrden() - 1))),
                                opcion.getTexto(), imagenes.urlDe(opcion.getImagen()),
                                opcion.getId().equals(elegida.getId()), opcion.isCorrecta()))
                        .toList();
        String retroalimentacion = intento.getRetroalimentacion() == null || intento.getRetroalimentacion().isBlank()
                ? Opcion.SIN_RETROALIMENTACION
                : intento.getRetroalimentacion();
        return new co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeIntento(intento.getId(), ejercicio.getId(),
                ejercicio.getNumero(), intento.getEnunciado(), imagenes.urlDe(intento.getImagenEnunciado()),
                intento.getComponente().getNombre(), intento.getCompetencia().getNombre(),
                intento.getNivel().getNivel(), ejercicio.getEstado(), revisadas, intento.isCorrecto(),
                intento.getNivelConfianza(), intento.getRespondidoEn(), retroalimentacion,
                intento.getTipoError() == null ? null : intento.getTipoError().getNombre(),
                intento.getIdSimulacro());
    }

    @Override
    public boolean yaRespondidoEnSimulacro(Long idSimulacro, Long idEjercicio) {
        return intentos.existsByIdSimulacroAndEjercicio_Id(idSimulacro, idEjercicio);
    }

    @Override
    public List<Intento> delSimulacro(Long idSimulacro) {
        return intentos.findByIdSimulacroOrderByRespondidoEnAscIdAsc(idSimulacro).stream()
                .map(Mapeador::aDominioConOpciones).toList();
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
