package co.edu.udea.brujula.ejercicio;

import co.edu.udea.brujula.catalogo.*;
import co.edu.udea.brujula.catalogo.CatalogoController.Item;
import co.edu.udea.brujula.common.ApiException;
import co.edu.udea.brujula.common.PageResponse;
import co.edu.udea.brujula.common.Porcentajes;
import co.edu.udea.brujula.config.UsuarioPrincipal;
import co.edu.udea.brujula.ejercicio.EjercicioDtos.*;
import co.edu.udea.brujula.practica.IntentoRepository;
import co.edu.udea.brujula.usuario.Usuario;
import co.edu.udea.brujula.usuario.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EjercicioService {

    private final EjercicioRepository ejercicios;
    private final IntentoRepository intentos;
    private final ComponenteRepository componentes;
    private final CompetenciaRepository competencias;
    private final NivelDificultadRepository niveles;
    private final TipoErrorRepository tiposError;
    private final UsuarioRepository usuarios;
    private final AuditoriaEjercicioRepository auditoria;
    private final ParametrosService parametros;
    private final ArchivoService archivos;

    public EjercicioService(EjercicioRepository ejercicios, IntentoRepository intentos, ComponenteRepository componentes,
                            CompetenciaRepository competencias, NivelDificultadRepository niveles, TipoErrorRepository tiposError,
                            UsuarioRepository usuarios, AuditoriaEjercicioRepository auditoria, ParametrosService parametros,
                            ArchivoService archivos) {
        this.ejercicios = ejercicios;
        this.intentos = intentos;
        this.componentes = componentes;
        this.competencias = competencias;
        this.niveles = niveles;
        this.tiposError = tiposError;
        this.usuarios = usuarios;
        this.auditoria = auditoria;
        this.parametros = parametros;
        this.archivos = archivos;
    }

    // ---------- HU-006, HU-007, HU-008: banco ----------

    @Transactional(readOnly = true)
    public PageResponse<Tarjeta> listar(UsuarioPrincipal p, Long idComponente, int pagina) {
        boolean soloActivos = !p.esAdministrador();
        int tamano = parametros.entero(ParametrosService.TAMANO_PAGINA_BANCO, 20);
        Page<Ejercicio> page = ejercicios.listar(soloActivos, idComponente, PageRequest.of(Math.max(0, pagina), tamano));
        Map<Long, Long> conteos = Map.of();
        if (p.esAdministrador() && !page.isEmpty()) {
            conteos = intentos.contarPorEjercicio(page.getContent().stream().map(Ejercicio::getId).toList()).stream()
                    .collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));
        }
        final Map<Long, Long> c = conteos;
        return PageResponse.de(page, e -> new Tarjeta(e.getId(), e.getNumero(), e.getComponente().getNombre(),
                e.getCompetencia().getNombre(), e.getNivelDificultad().getNivel(), e.getEstado(),
                p.esAdministrador() ? c.getOrDefault(e.getId(), 0L) : null));
    }

    @Transactional(readOnly = true)
    public Componentes componentesConConteo(UsuarioPrincipal p) {
        boolean soloActivos = !p.esAdministrador();
        List<ComponenteConteo> lista = ejercicios.contarPorComponente(soloActivos).stream()
                .map(r -> new ComponenteConteo((Long) r[0], (String) r[1], (Long) r[2])).toList();
        return new Componentes(ejercicios.contarTotal(soloActivos), lista);
    }

    // ---------- HU-009: abrir ejercicio (estudiante) ----------

    @Transactional(readOnly = true)
    public EjercicioEstudiante paraEstudiante(Long idUsuario, Long idEjercicio) {
        Ejercicio e = ejercicios.buscarCompleto(idEjercicio).orElseThrow(() -> ApiException.noEncontrado("El ejercicio no existe."));
        if (!e.estaActivo()) {
            throw ApiException.noDisponible("Este ejercicio ya no está disponible.");          // HU-009 CA-03
        }
        return vistaEstudiante(e, intentos.countByUsuario_IdAndEjercicio_Id(idUsuario, idEjercicio));
    }

    public EjercicioEstudiante vistaEstudiante(Ejercicio e, long intentosPrevios) {
        List<OpcionEstudiante> ops = e.getOpciones().stream()
                .map(o -> new OpcionEstudiante(o.getId(), o.getLetra(), o.getDescripcion(), archivos.url(o.getImagen()), o.getOrden()))
                .toList();
        return new EjercicioEstudiante(e.getId(), e.getNumero(), e.getEnunciado(), archivos.url(e.getImagenEnunciado()),
                new Item(e.getComponente().getId(), e.getComponente().getNombre()),
                new Item(e.getCompetencia().getId(), e.getCompetencia().getNombre()),
                e.getNivelDificultad().getNivel(), e.getEstado(), ops, intentosPrevios);
    }

    /** HU-010 CA-07/CA-08/CA-09: siguiente ejercicio en práctica libre. */
    @Transactional(readOnly = true)
    public Map<String, Object> siguiente(Long idUsuario, Long idActual, Long idComponente) {
        Optional<Long> id = ejercicios.siguienteParaPractica(idActual, idComponente, idUsuario);
        if (id.isEmpty()) {
            return Map.of("hayMas", false, "mensaje", idComponente == null
                    ? "No hay más ejercicios disponibles por ahora."
                    : "No hay más ejercicios disponibles para este componente. Puedes volver al banco o cambiar el filtro.");
        }
        return Map.of("hayMas", true, "idEjercicio", id.get());
    }

    // ---------- HU-022: detalle (administrador) ----------

    @Transactional(readOnly = true)
    public EjercicioAdmin paraAdministrador(Long idEjercicio) {
        Ejercicio e = ejercicios.buscarCompleto(idEjercicio).orElseThrow(() -> ApiException.noEncontrado("El ejercicio no existe."));
        return vistaAdmin(e);
    }

    private EjercicioAdmin vistaAdmin(Ejercicio e) {
        Set<Long> usadas = new HashSet<>(intentos.opcionesUsadas(e.getId()));
        List<OpcionAdmin> ops = e.getOpciones().stream().map(o -> new OpcionAdmin(o.getId(), o.getLetra(), o.getDescripcion(),
                archivos.url(o.getImagen()), o.isEsCorrecta(), o.getRetroalimentacion(),
                o.getTipoError() == null ? null : new Item(o.getTipoError().getId(), o.getTipoError().getNombre()),
                o.getOrden(), usadas.contains(o.getId()))).toList();
        long total = intentos.countByEjercicio_Id(e.getId());
        long correctos = total == 0 ? 0 : intentos.countByEjercicio_IdAndEsCorrectoTrue(e.getId());
        long incorrectos = total - correctos;
        List<DistribucionError> errores = intentos.erroresPorEjercicio(e.getId()).stream()
                .map(r -> new DistribucionError((String) r[0], (Long) r[1], Porcentajes.de((Long) r[1], incorrectos)))
                .toList();
        EstadisticasUso uso = new EstadisticasUso(total, correctos, Porcentajes.de(correctos, total), errores);
        return new EjercicioAdmin(e.getId(), e.getNumero(), e.getEnunciado(), archivos.url(e.getImagenEnunciado()),
                new Item(e.getComponente().getId(), e.getComponente().getNombre()),
                new Item(e.getCompetencia().getId(), e.getCompetencia().getNombre()),
                new Item(e.getNivelDificultad().getId(), e.getNivelDificultad().getNivel()),
                e.getEstado(), e.getCreador().getNombreCompleto(), e.getCreadoEn(), ops, uso, total > 0);
    }

    // ---------- HU-020: crear ----------

    @Transactional
    public EjercicioAdmin crear(Long idAdmin, EjercicioRequest req) {
        Ejercicio e = new Ejercicio();
        e.setCreador(usuarios.findById(idAdmin).orElseThrow());
        e.setEstado(Ejercicio.ACTIVO);                                                        // CA-10
        aplicar(e, req, Set.of());
        ejercicios.saveAndFlush(e);
        registrarAuditoria("Creación", e.getId(), idAdmin);
        return vistaAdmin(ejercicios.buscarCompleto(e.getId()).orElseThrow());
    }

    // ---------- HU-021: editar ----------

    @Transactional
    public EjercicioAdmin editar(Long idAdmin, Long idEjercicio, EjercicioRequest req) {
        Ejercicio e = ejercicios.buscarCompleto(idEjercicio).orElseThrow(() -> ApiException.noEncontrado("El ejercicio no existe."));
        Set<Long> usadas = new HashSet<>(intentos.opcionesUsadas(idEjercicio));
        aplicar(e, req, usadas);                                                             // CA-06/CA-07: ni número ni estado cambian
        ejercicios.saveAndFlush(e);
        registrarAuditoria("Edición", e.getId(), idAdmin);
        return vistaAdmin(ejercicios.buscarCompleto(e.getId()).orElseThrow());
    }

    private void aplicar(Ejercicio e, EjercicioRequest req, Set<Long> opcionesUsadas) {
        List<String> errores = new ArrayList<>();
        String enunciado = req.enunciado() == null ? "" : req.enunciado().trim();
        if (enunciado.isEmpty()) errores.add("El enunciado es obligatorio.");
        if (req.idComponente() == null) errores.add("Debes seleccionar un componente.");
        if (req.idCompetencia() == null) errores.add("Debes seleccionar una competencia.");
        if (req.idNivelDificultad() == null) errores.add("Debes seleccionar un nivel de dificultad.");
        List<OpcionRequest> ops = req.opciones() == null ? List.of() : req.opciones();
        if (ops.size() < 2) errores.add("El ejercicio debe tener al menos dos opciones de respuesta.");
        long correctas = ops.stream().filter(o -> Boolean.TRUE.equals(o.esCorrecta())).count();
        if (ops.size() >= 2 && correctas != 1) errores.add("El ejercicio debe tener exactamente una opción marcada como correcta.");

        Set<String> vistas = new HashSet<>();
        for (int i = 0; i < ops.size(); i++) {
            OpcionRequest o = ops.get(i);
            String desc = o.descripcion() == null ? "" : o.descripcion().trim();
            String img = o.imagen() == null ? "" : archivos.nombre(o.imagen().trim());
            if (desc.isEmpty() && img.isEmpty()) errores.add("La opción " + (char) ('A' + i) + " debe tener texto o imagen.");
            if (o.retroalimentacion() == null || o.retroalimentacion().isBlank()) errores.add("La opción " + (char) ('A' + i) + " debe tener retroalimentación.");
            String clave = (desc.toLowerCase() + "|" + img).trim();
            if (!clave.equals("|") && !vistas.add(clave)) errores.add("Hay opciones de respuesta duplicadas (" + (char) ('A' + i) + ").");
            if (!img.isEmpty() && !archivos.existe(img)) errores.add("La imagen de la opción " + (char) ('A' + i) + " no existe en el servidor.");
        }
        String imgEnunciado = req.imagenEnunciado() == null ? "" : archivos.nombre(req.imagenEnunciado().trim());
        if (!imgEnunciado.isEmpty() && !archivos.existe(imgEnunciado)) errores.add("La imagen del enunciado no existe en el servidor.");

        if (!errores.isEmpty()) throw ApiException.validacion(errores);

        // CA-14 / CA-16: enunciado único
        List<Long> duplicados = ejercicios.idsPorEnunciado(enunciado);
        if (duplicados.stream().anyMatch(id -> !id.equals(e.getId()))) {
            throw ApiException.conflicto("ENUNCIADO_DUPLICADO", "Ya existe un ejercicio con el mismo enunciado.");
        }

        Componente comp = componentes.findById(req.idComponente()).filter(c -> "Activo".equals(c.getEstado()))
                .orElseThrow(() -> ApiException.badRequest("COMPONENTE_INVALIDO", "El componente seleccionado no existe o no está activo."));
        Competencia compe = competencias.findById(req.idCompetencia()).filter(c -> "Activo".equals(c.getEstado()))
                .orElseThrow(() -> ApiException.badRequest("COMPETENCIA_INVALIDA", "La competencia seleccionada no existe o no está activa."));
        NivelDificultad nivel = niveles.findById(req.idNivelDificultad())
                .orElseThrow(() -> ApiException.badRequest("NIVEL_INVALIDO", "El nivel de dificultad no existe."));

        e.setEnunciado(enunciado);
        e.setImagenEnunciado(imgEnunciado.isEmpty() ? null : imgEnunciado);
        e.setComponente(comp);
        e.setCompetencia(compe);
        e.setNivelDificultad(nivel);

        // ----- Opciones -----
        Map<Long, OpcionRespuesta> actuales = e.getOpciones().stream().filter(o -> o.getId() != null)
                .collect(Collectors.toMap(OpcionRespuesta::getId, Function.identity()));
        Set<Long> conservadas = ops.stream().map(OpcionRequest::id).filter(Objects::nonNull).collect(Collectors.toSet());

        // HU-021 CA-09: una opción ya usada en intentos no puede eliminarse
        for (Long usada : opcionesUsadas) {
            if (!conservadas.contains(usada)) {
                OpcionRespuesta op = actuales.get(usada);
                throw ApiException.conflicto("OPCION_CON_INTENTOS", "La opción " + (op == null ? "" : op.getLetra()) +
                        " ya fue usada en intentos de estudiantes y no puede eliminarse.");
            }
        }
        // Se vacía la lista y se reconstruye en el orden recibido (orphanRemoval elimina las que no vuelven).
        List<OpcionRespuesta> nuevas = new ArrayList<>();
        for (int i = 0; i < ops.size(); i++) {
            OpcionRequest o = ops.get(i);
            OpcionRespuesta op;
            if (o.id() != null) {
                op = actuales.get(o.id());
                if (op == null) throw ApiException.badRequest("OPCION_INVALIDA", "Una de las opciones no pertenece a este ejercicio.");
                if (opcionesUsadas.contains(op.getId())) {
                    String descNueva = o.descripcion() == null ? "" : o.descripcion().trim();
                    String imgNueva = o.imagen() == null ? "" : archivos.nombre(o.imagen().trim());
                    boolean mismaDesc = Objects.equals(vacioANull(descNueva), op.getDescripcion());
                    boolean mismaImg = Objects.equals(vacioANull(imgNueva), op.getImagen());
                    boolean mismaCorrecta = Boolean.TRUE.equals(o.esCorrecta()) == op.isEsCorrecta();
                    if (!mismaDesc || !mismaImg || !mismaCorrecta) {
                        throw ApiException.conflicto("OPCION_CON_INTENTOS", "La opción " + op.getLetra() +
                                " ya fue usada en intentos de estudiantes: solo puede cambiarse su retroalimentación o tipo de error.");
                    }
                }
            } else {
                op = new OpcionRespuesta();
                op.setEjercicio(e);
            }
            op.setDescripcion(vacioANull(o.descripcion() == null ? "" : o.descripcion().trim()));
            op.setImagen(vacioANull(o.imagen() == null ? "" : archivos.nombre(o.imagen().trim())));
            op.setEsCorrecta(Boolean.TRUE.equals(o.esCorrecta()));
            op.setRetroalimentacion(o.retroalimentacion().trim());
            if (op.isEsCorrecta() || o.idTipoError() == null) {
                op.setTipoError(null);
            } else {
                op.setTipoError(tiposError.findById(o.idTipoError())
                        .orElseThrow(() -> ApiException.badRequest("TIPO_ERROR_INVALIDO", "El tipo de error no existe.")));
            }
            op.setOrden(i + 1);
            nuevas.add(op);
        }
        e.getOpciones().clear();
        e.getOpciones().addAll(nuevas);
    }

    private static String vacioANull(String s) {
        return s == null || s.isEmpty() ? null : s;
    }

    // ---------- HU-023 / HU-024: desactivar y activar ----------

    @Transactional
    public EjercicioAdmin cambiarEstado(Long idAdmin, Long idEjercicio, String estado) {
        if (!Ejercicio.ACTIVO.equals(estado) && !Ejercicio.DESACTIVADO.equals(estado)) {
            throw ApiException.badRequest("ESTADO_INVALIDO", "El estado debe ser Activo o Desactivado.");
        }
        Ejercicio e = ejercicios.buscarCompleto(idEjercicio).orElseThrow(() -> ApiException.noEncontrado("El ejercicio no existe."));
        if (estado.equals(e.getEstado())) {
            throw ApiException.conflicto("ESTADO_SIN_CAMBIO", "El ejercicio ya está " + estado.toLowerCase() + ".");
        }
        e.setEstado(estado);
        ejercicios.save(e);
        registrarAuditoria(Ejercicio.ACTIVO.equals(estado) ? "Activación" : "Desactivación", e.getId(), idAdmin);
        return vistaAdmin(e);
    }

    private void registrarAuditoria(String accion, Long idEjercicio, Long idActor) {
        AuditoriaEjercicio a = new AuditoriaEjercicio();
        a.setTipoAccion(accion);
        a.setIdEjercicio(idEjercicio);
        a.setIdUsuarioActor(idActor);
        auditoria.save(a);
    }
}
