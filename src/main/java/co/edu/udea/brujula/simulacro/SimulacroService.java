package co.edu.udea.brujula.simulacro;

import co.edu.udea.brujula.catalogo.*;
import co.edu.udea.brujula.common.ApiException;
import co.edu.udea.brujula.common.PageResponse;
import co.edu.udea.brujula.common.Porcentajes;
import co.edu.udea.brujula.ejercicio.Ejercicio;
import co.edu.udea.brujula.ejercicio.EjercicioRepository;
import co.edu.udea.brujula.ejercicio.EjercicioService;
import co.edu.udea.brujula.practica.Intento;
import co.edu.udea.brujula.practica.IntentoDtos.IntentoDetalle;
import co.edu.udea.brujula.practica.IntentoMapper;
import co.edu.udea.brujula.practica.IntentoRepository;
import co.edu.udea.brujula.simulacro.SimulacroDtos.*;
import co.edu.udea.brujula.usuario.UsuarioRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Service
public class SimulacroService {

    public static final int TAMANO_PAGINA = 20;
    public static final String MSG_BUEN_DESEMPENO = "Tienes un buen desempeño general. Sigue practicando para mantenerlo.";
    public static final String MSG_SIN_RESPUESTAS = "No hay resultados suficientes: el simulacro finalizó sin respuestas registradas.";

    private final SimulacroRepository simulacros;
    private final IntentoRepository intentos;
    private final EjercicioRepository ejercicios;
    private final EjercicioService ejercicioService;
    private final RecomendacionEstudioRepository recomendaciones;
    private final DuracionSimulacroRepository duraciones;
    private final UsuarioRepository usuarios;
    private final ParametrosService parametros;
    private final IntentoMapper intentoMapper;

    public SimulacroService(SimulacroRepository simulacros, IntentoRepository intentos, EjercicioRepository ejercicios,
                            EjercicioService ejercicioService, RecomendacionEstudioRepository recomendaciones,
                            DuracionSimulacroRepository duraciones, UsuarioRepository usuarios, ParametrosService parametros,
                            IntentoMapper intentoMapper) {
        this.simulacros = simulacros;
        this.intentos = intentos;
        this.ejercicios = ejercicios;
        this.ejercicioService = ejercicioService;
        this.recomendaciones = recomendaciones;
        this.duraciones = duraciones;
        this.usuarios = usuarios;
        this.parametros = parametros;
        this.intentoMapper = intentoMapper;
    }

    // ---------- HU-013: configurar e iniciar ----------

    @Transactional
    public Estado crear(Long idUsuario, Long idDuracion) {
        if (idDuracion == null) throw ApiException.validacion(List.of("Debes seleccionar la duración del simulacro."));
        DuracionSimulacro d = duraciones.findById(idDuracion)
                .orElseThrow(() -> ApiException.badRequest("DURACION_INVALIDA", "La duración seleccionada no está disponible."));
        Instant ahora = Instant.now();
        for (Simulacro s : simulacros.findByUsuario_IdAndEstado(idUsuario, Simulacro.EN_CURSO)) {
            if (s.tiempoAgotado(ahora)) {
                finalizar(s, ahora);                                                   // HU-016 CA-05
            } else {
                throw new ApiException(HttpStatus.CONFLICT, "SIMULACRO_EN_CURSO",
                        "Ya tienes un simulacro en curso. Continúalo o finalízalo antes de iniciar otro.",
                        List.of(), Map.of("idSimulacro", s.getId()));
            }
        }
        if (ejercicios.countByEstado(Ejercicio.ACTIVO) == 0) {
            throw ApiException.conflicto("SIN_EJERCICIOS", "Aún no hay ejercicios disponibles para hacer un simulacro.");
        }
        Simulacro s = new Simulacro();
        s.setUsuario(usuarios.getReferenceById(idUsuario));
        s.setDuracion(d);
        s.setFechaInicio(ahora);                                                       // CA-04
        s.setEstado(Simulacro.EN_CURSO);
        simulacros.save(s);
        return estado(s, ahora);
    }

    @Transactional
    public Optional<Estado> enCurso(Long idUsuario) {
        Instant ahora = Instant.now();
        for (Simulacro s : simulacros.findByUsuario_IdAndEstado(idUsuario, Simulacro.EN_CURSO)) {
            if (s.tiempoAgotado(ahora)) {
                finalizar(s, ahora);
            } else {
                return Optional.of(estado(s, ahora));
            }
        }
        return Optional.empty();
    }

    @Transactional
    public Estado estado(Long idUsuario, Long idSimulacro) {
        Simulacro s = propio(idUsuario, idSimulacro);
        Instant ahora = Instant.now();
        if (s.enCurso() && s.tiempoAgotado(ahora)) finalizar(s, ahora);
        return estado(s, ahora);
    }

    private Estado estado(Simulacro s, Instant ahora) {
        long respondidos = intentos.countBySimulacro_Id(s.getId());
        long correctas = respondidos == 0 ? 0 : intentos.countBySimulacro_IdAndEsCorrectoTrue(s.getId());
        return new Estado(s.getId(), s.getEstado(), s.getDuracion().getDuracionMinutos(), s.getFechaInicio(), s.finPrevisto(),
                ahora, s.enCurso() ? s.segundosRestantes(ahora) : 0, respondidos, correctas);
    }

    // ---------- HU-014: siguiente ejercicio ----------

    @Transactional
    public Siguiente siguiente(Long idUsuario, Long idSimulacro) {
        Simulacro s = propio(idUsuario, idSimulacro);
        Instant ahora = Instant.now();
        if (!s.enCurso()) {
            return new Siguiente(true, "FINALIZADO", null, estado(s, ahora));
        }
        if (s.tiempoAgotado(ahora)) {
            finalizar(s, ahora);                                                       // HU-015 CA-01
            return new Siguiente(true, "TIEMPO", null, estado(s, ahora));
        }
        Optional<Long> id = ejercicios.siguienteParaSimulacro(s.getId());
        if (id.isEmpty()) {
            finalizar(s, ahora);                                                       // HU-015 CA-03
            return new Siguiente(true, "SIN_EJERCICIOS", null, estado(s, ahora));
        }
        Ejercicio e = ejercicios.buscarCompleto(id.get()).orElseThrow();
        return new Siguiente(false, null, ejercicioService.vistaEstudiante(e, 0), estado(s, ahora));
    }

    /** Usado por IntentoService antes de registrar una respuesta dentro de un simulacro. */
    @Transactional
    public Simulacro validarParaResponder(Long idUsuario, Long idSimulacro) {
        Simulacro s = propio(idUsuario, idSimulacro);
        Instant ahora = Instant.now();
        if (s.enCurso() && s.tiempoAgotado(ahora)) finalizar(s, ahora);
        if (!s.enCurso()) {
            throw new ApiException(HttpStatus.CONFLICT, "SIMULACRO_FINALIZADO",
                    "El simulacro ya finalizó; no es posible registrar más respuestas.", List.of(), Map.of("idSimulacro", s.getId()));
        }
        return s;
    }

    // ---------- HU-015: finalizar ----------

    @Transactional
    public Resultado finalizarVoluntario(Long idUsuario, Long idSimulacro) {
        Simulacro s = propio(idUsuario, idSimulacro);
        if (s.enCurso()) finalizar(s, Instant.now());
        return resultado(s);
    }

    private void finalizar(Simulacro s, Instant ahora) {
        Instant fin = ahora.isAfter(s.finPrevisto()) ? s.finPrevisto() : ahora;
        s.setFechaFin(fin);
        s.setTiempoUtilizadoSeg((int) Math.min(Duration.between(s.getFechaInicio(), fin).getSeconds(),
                (long) s.getDuracion().getDuracionMinutos() * 60));
        s.setEstado(Simulacro.FINALIZADO);
        simulacros.save(s);
        generarRecomendaciones(s);                                                     // HU-018 CA-01
    }

    private void generarRecomendaciones(Simulacro s) {
        List<Intento> lista = intentos.findBySimulacro_IdOrderByFechaHoraAscIdAsc(s.getId());
        if (lista.isEmpty()) return;
        int umbral = parametros.entero(ParametrosService.UMBRAL_RECOMENDACION_PCT, 60);
        List<DesempenoArea> comps = agrupar(lista, i -> i.getEjercicio().getComponente().getId(), i -> i.getEjercicio().getComponente().getNombre());
        List<DesempenoArea> competencias = agrupar(lista, i -> i.getEjercicio().getCompetencia().getId(), i -> i.getEjercicio().getCompetencia().getNombre());

        record Area(String tipo, DesempenoArea d) {}
        List<Area> debiles = new ArrayList<>();
        comps.stream().filter(d -> d.porcentaje().compareTo(BigDecimal.valueOf(umbral)) < 0).forEach(d -> debiles.add(new Area("Componente", d)));
        competencias.stream().filter(d -> d.porcentaje().compareTo(BigDecimal.valueOf(umbral)) < 0).forEach(d -> debiles.add(new Area("Competencia", d)));
        debiles.sort(Comparator.comparing((Area a) -> a.d().porcentaje()).thenComparing(a -> a.d().nombre()));   // CA-03

        int orden = 1;
        for (Area a : debiles) {
            RecomendacionEstudio r = new RecomendacionEstudio();
            r.setSimulacro(s);
            r.setOrdenPrioridad(orden++);
            r.setPorcentajeAciertos(a.d().porcentaje());
            r.setMensaje("Te recomendamos practicar ejercicios de " + a.d().nombre() + ".");   // CA-04
            if ("Componente".equals(a.tipo())) {
                Componente c = new Componente();
                c.setId(a.d().id());
                r.setComponente(c);
            } else {
                Competencia c = new Competencia();
                c.setId(a.d().id());
                r.setCompetencia(c);
            }
            recomendaciones.save(r);
        }
    }

    // ---------- HU-017 / HU-018: resultado ----------

    @Transactional
    public Resultado resultado(Long idUsuario, Long idSimulacro) {
        Simulacro s = propio(idUsuario, idSimulacro);                                   // HU-017 CA-08
        Instant ahora = Instant.now();
        if (s.enCurso() && s.tiempoAgotado(ahora)) finalizar(s, ahora);
        if (s.enCurso()) {
            throw new ApiException(HttpStatus.CONFLICT, "SIMULACRO_EN_CURSO", "El simulacro aún está en curso.",
                    List.of(), Map.of("idSimulacro", s.getId()));
        }
        return resultado(s);
    }

    private Resultado resultado(Simulacro s) {
        List<Intento> lista = intentos.findBySimulacro_IdOrderByFechaHoraAscIdAsc(s.getId());   // CA-09: solo los de este simulacro
        long total = lista.size();
        long correctas = lista.stream().filter(Intento::isEsCorrecto).count();
        List<DesempenoArea> comps = agrupar(lista, i -> i.getEjercicio().getComponente().getId(), i -> i.getEjercicio().getComponente().getNombre());
        List<DesempenoArea> competencias = agrupar(lista, i -> i.getEjercicio().getCompetencia().getId(), i -> i.getEjercicio().getCompetencia().getNombre());
        List<Recomendacion> recos = recomendaciones.findBySimulacro_IdOrderByOrdenPrioridadAsc(s.getId()).stream()
                .map(r -> new Recomendacion(r.getOrdenPrioridad(),
                        r.getComponente() != null ? r.getComponente().getNombre() : r.getCompetencia().getNombre(),
                        r.getComponente() != null ? "Componente" : "Competencia",
                        r.getComponente() != null ? r.getComponente().getId() : null,
                        r.getCompetencia() != null ? r.getCompetencia().getId() : null,
                        r.getPorcentajeAciertos(), r.getMensaje()))
                .toList();
        String mensaje = total == 0 ? MSG_SIN_RESPUESTAS : (recos.isEmpty() ? MSG_BUEN_DESEMPENO : null);   // CA-05, HU-026 CA-05
        List<IntentoDetalle> detalle = lista.stream().map(intentoMapper::detalle).toList();
        return new Resultado(s.getId(), s.getFechaInicio(), s.getFechaFin(), s.getDuracion().getDuracionMinutos(),
                s.getTiempoUtilizadoSeg(), total, correctas, total - correctas, Porcentajes.de(correctas, total),
                comps, competencias, recos, mensaje, detalle);
    }

    private List<DesempenoArea> agrupar(List<Intento> lista, Function<Intento, Long> id, Function<Intento, String> nombre) {
        Map<Long, long[]> acc = new LinkedHashMap<>();
        Map<Long, String> nombres = new HashMap<>();
        for (Intento i : lista) {
            Long k = id.apply(i);
            nombres.putIfAbsent(k, nombre.apply(i));
            long[] a = acc.computeIfAbsent(k, x -> new long[2]);
            a[0]++;
            if (i.isEsCorrecto()) a[1]++;
        }
        return acc.entrySet().stream()
                .map(e -> new DesempenoArea(e.getKey(), nombres.get(e.getKey()), e.getValue()[0], e.getValue()[1], Porcentajes.de(e.getValue()[1], e.getValue()[0])))
                .sorted(Comparator.comparing(DesempenoArea::id))
                .toList();
    }

    // ---------- HU-026: historial ----------

    @Transactional
    public PageResponse<Resumen> historial(Long idUsuario, int pagina) {
        Instant ahora = Instant.now();
        simulacros.findByUsuario_IdAndEstado(idUsuario, Simulacro.EN_CURSO).stream()
                .filter(s -> s.tiempoAgotado(ahora)).forEach(s -> finalizar(s, ahora));
        var page = simulacros.findByUsuario_IdAndEstadoOrderByFechaInicioDesc(idUsuario, Simulacro.FINALIZADO,
                PageRequest.of(Math.max(0, pagina), TAMANO_PAGINA));
        return PageResponse.de(page, s -> {
            long total = intentos.countBySimulacro_Id(s.getId());
            long correctas = total == 0 ? 0 : intentos.countBySimulacro_IdAndEsCorrectoTrue(s.getId());
            return new Resumen(s.getId(), s.getFechaInicio(), s.getFechaFin(), s.getDuracion().getDuracionMinutos(),
                    s.getTiempoUtilizadoSeg(), total, correctas, total - correctas, Porcentajes.de(correctas, total));
        });
    }

    private Simulacro propio(Long idUsuario, Long idSimulacro) {
        return simulacros.findByIdAndUsuario_Id(idSimulacro, idUsuario)
                .orElseThrow(() -> ApiException.noEncontrado("El simulacro no existe."));
    }
}
