package co.edu.udea.brujula.practica;

import co.edu.udea.brujula.catalogo.TipoError;
import co.edu.udea.brujula.common.ApiException;
import co.edu.udea.brujula.common.PageResponse;
import co.edu.udea.brujula.ejercicio.Ejercicio;
import co.edu.udea.brujula.ejercicio.EjercicioRepository;
import co.edu.udea.brujula.ejercicio.OpcionRespuesta;
import co.edu.udea.brujula.practica.IntentoDtos.*;
import co.edu.udea.brujula.simulacro.Simulacro;
import co.edu.udea.brujula.simulacro.SimulacroService;
import co.edu.udea.brujula.usuario.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class IntentoService {

    public static final int TAMANO_PAGINA_HISTORIAL = 20;

    private final IntentoRepository intentos;
    private final EjercicioRepository ejercicios;
    private final UsuarioRepository usuarios;
    private final ClasificadorErrorService clasificador;
    private final SimulacroService simulacros;
    private final IntentoMapper mapper;

    public IntentoService(IntentoRepository intentos, EjercicioRepository ejercicios, UsuarioRepository usuarios,
                          ClasificadorErrorService clasificador, SimulacroService simulacros, IntentoMapper mapper) {
        this.intentos = intentos;
        this.ejercicios = ejercicios;
        this.usuarios = usuarios;
        this.clasificador = clasificador;
        this.simulacros = simulacros;
        this.mapper = mapper;
    }

    /** HU-010, HU-011, HU-012, HU-014, HU-016, HU-027: registra un intento y devuelve el resultado inmediato. */
    @Transactional
    public ResultadoIntento registrar(Long idUsuario, IntentoRequest req) {
        List<String> errores = new ArrayList<>();
        if (req.idEjercicio() == null) errores.add("Falta el ejercicio.");
        if (req.idOpcion() == null) errores.add("Debes seleccionar una opción de respuesta.");
        if (req.nivelConfianza() == null || req.nivelConfianza() < 1 || req.nivelConfianza() > 5) errores.add("Debes indicar un nivel de confianza entre 1 y 5.");
        if (!errores.isEmpty()) throw ApiException.validacion(errores);

        // HU-016 CA-02: el reenvío de un intento ya confirmado no se duplica.
        if (req.tokenIdempotencia() != null) {
            Optional<Intento> previo = intentos.findByTokenIdempotencia(req.tokenIdempotencia());
            if (previo.isPresent()) {
                Intento p = previo.get();
                if (!p.getUsuario().getId().equals(idUsuario)) throw ApiException.prohibido("El intento no te pertenece.");
                return resultado(p, true);
            }
        }

        Ejercicio e = ejercicios.buscarCompleto(req.idEjercicio()).orElseThrow(() -> ApiException.noEncontrado("El ejercicio no existe."));
        if (!e.estaActivo()) {
            throw ApiException.noDisponible("Este ejercicio ya no está disponible; no es posible registrar la respuesta.");   // HU-010 CA-10, HU-023 CA-09
        }
        OpcionRespuesta opcion = e.getOpciones().stream().filter(o -> o.getId().equals(req.idOpcion())).findFirst()
                .orElseThrow(() -> ApiException.badRequest("OPCION_INVALIDA", "La opción seleccionada no pertenece al ejercicio."));

        Simulacro sim = null;
        if (req.idSimulacro() != null) {
            sim = simulacros.validarParaResponder(idUsuario, req.idSimulacro());            // en curso y con tiempo
            if (intentos.existsBySimulacro_IdAndEjercicio_Id(sim.getId(), e.getId())) {
                throw ApiException.conflicto("EJERCICIO_YA_RESPONDIDO", "Este ejercicio ya fue respondido en el simulacro.");
            }
        }

        Instant ahora = Instant.now();                                                     // HU-010 CA-03, HU-012 CA-07
        Intento i = new Intento();
        i.setFechaHora(ahora);
        i.setUsuario(usuarios.getReferenceById(idUsuario));
        i.setEjercicio(e);
        i.setOpcionSeleccionada(opcion);
        i.setEsCorrecto(opcion.isEsCorrecta());
        i.setNivelConfianza(req.nivelConfianza());
        i.setSimulacro(sim);
        i.setTokenIdempotencia(req.tokenIdempotencia());
        if (!opcion.isEsCorrecta()) {
            TipoError tipo = clasificador.clasificar(idUsuario, e, opcion, req.nivelConfianza());   // HU-027 CA-01
            i.setTipoError(tipo);
        }
        try {
            intentos.saveAndFlush(i);
        } catch (DataIntegrityViolationException ex) {
            // Carrera entre reenvíos con el mismo token: se devuelve el que ganó.
            if (req.tokenIdempotencia() != null) {
                Optional<Intento> previo = intentos.findByTokenIdempotencia(req.tokenIdempotencia());
                if (previo.isPresent()) return resultado(previo.get(), true);
            }
            throw new ApiException(HttpStatus.CONFLICT, "INTENTO_DUPLICADO", "No fue posible registrar la respuesta. Intenta de nuevo.");
        }
        return resultado(i, false);
    }

    private ResultadoIntento resultado(Intento i, boolean repetido) {
        Ejercicio e = i.getEjercicio();
        OpcionRespuesta sel = i.getOpcionSeleccionada();
        OpcionRespuesta correcta = e.opcionCorrecta();
        String retro = sel.getRetroalimentacion();
        boolean disponible = retro != null && !retro.isBlank();                              // HU-011 CA-06
        long numero = intentos.countByUsuario_IdAndEjercicio_Id(i.getUsuario().getId(), e.getId());
        return new ResultadoIntento(i.getId(), i.isEsCorrecto(), sel.getId(), correcta == null ? null : correcta.getId(),
                disponible ? retro : IntentoMapper.SIN_RETROALIMENTACION, disponible,
                i.getNivelConfianza(), i.getFechaHora(), numero, repetido,
                i.getSimulacro() == null ? null : i.getSimulacro().getId());
    }

    // ---------- HU-025: historial ----------

    @Transactional(readOnly = true)
    public PageResponse<IntentoResumen> historial(Long idUsuario, int pagina) {
        var page = intentos.findByUsuario_IdOrderByFechaHoraDescIdDesc(idUsuario, PageRequest.of(Math.max(0, pagina), TAMANO_PAGINA_HISTORIAL));
        return PageResponse.de(page, i -> {
            Ejercicio e = i.getEjercicio();
            return new IntentoResumen(i.getId(), e.getId(), e.getNumero(), e.getComponente().getNombre(),
                    e.getCompetencia().getNombre(), e.getNivelDificultad().getNivel(), i.isEsCorrecto(), i.getFechaHora(),
                    e.getEstado(), i.getSimulacro() != null);
        });
    }

    @Transactional(readOnly = true)
    public IntentoDetalle detalle(Long idUsuario, Long idIntento) {
        Intento i = intentos.findByIdAndUsuario_Id(idIntento, idUsuario)
                .orElseThrow(() -> ApiException.noEncontrado("El intento no existe."));            // CA-11: solo los propios
        return mapper.detalle(i);
    }
}
