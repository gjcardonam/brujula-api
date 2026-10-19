package co.edu.udea.brujula.infraestructura.salida.persistencia.adaptador;

import co.edu.udea.brujula.dominio.excepcion.Conflicto;
import co.edu.udea.brujula.dominio.modelo.Intento;
import co.edu.udea.brujula.dominio.modelo.Opcion;
import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeIntento;
import co.edu.udea.brujula.dominio.modelo.consulta.DistribucionDeError;
import co.edu.udea.brujula.dominio.modelo.consulta.ResumenDeIntento;
import co.edu.udea.brujula.dominio.modelo.consulta.UsoDelEjercicio;
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
        entidad.setRespondidoEn(intento.respondidoEn());
        entidad.setCorrecto(intento.correcto());
        entidad.setNivelConfianza((short) intento.nivelConfianza());
        entidad.setIdUsuario(intento.idEstudiante());
        entidad.setEjercicio(ejercicios.getReferenceById(intento.ejercicio().id()));
        entidad.setOpcionSeleccionada(opciones.getReferenceById(intento.opcionSeleccionada().id()));
        entidad.setTokenIdempotencia(intento.tokenIdempotencia());
        if (intento.tipoError() != null) {
            entidad.setTipoError(tiposDeError.getReferenceById(intento.tipoError().id()));
        }
        try {
            IntentoEntidad guardado = intentos.saveAndFlush(entidad);
            return new Intento(guardado.getId(), intento.respondidoEn(), intento.correcto(),
                    intento.nivelConfianza(), intento.tipoError(), intento.idEstudiante(), intento.ejercicio(),
                    intento.opcionSeleccionada(), intento.tokenIdempotencia());
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
                            ejercicio.getId(), ejercicio.getNumero(), ejercicio.getComponente().getNombre(),
                            ejercicio.getCompetencia().getNombre(), ejercicio.getNivel().getNivel(),
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
        String retroalimentacion = elegida.getRetroalimentacion() == null || elegida.getRetroalimentacion().isBlank()
                ? Opcion.SIN_RETROALIMENTACION
                : elegida.getRetroalimentacion();
        return new co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeIntento(intento.getId(), ejercicio.getId(),
                ejercicio.getNumero(), ejercicio.getEnunciado(), imagenes.urlDe(ejercicio.getImagen()),
                ejercicio.getComponente().getNombre(), ejercicio.getCompetencia().getNombre(),
                ejercicio.getNivel().getNivel(), ejercicio.getEstado(), revisadas, intento.isCorrecto(),
                intento.getNivelConfianza(), intento.getRespondidoEn(), retroalimentacion);
    }
}
