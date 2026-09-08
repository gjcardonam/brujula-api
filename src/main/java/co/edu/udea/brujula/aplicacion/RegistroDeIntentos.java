package co.edu.udea.brujula.aplicacion;

import co.edu.udea.brujula.dominio.excepcion.*;
import co.edu.udea.brujula.dominio.modelo.*;
import co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeIntento;
import co.edu.udea.brujula.dominio.modelo.consulta.ResultadoDeIntento;
import co.edu.udea.brujula.dominio.modelo.consulta.ResumenDeIntento;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarHistorialDeIntentos;
import co.edu.udea.brujula.dominio.puerto.entrada.ResponderEjercicio;
import co.edu.udea.brujula.dominio.puerto.salida.*;
import co.edu.udea.brujula.dominio.servicio.ClasificadorDeError;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Registro de respuestas (HU-010 a HU-012, HU-014) y consulta del historial (HU-025).
 *
 * Un intento no se modifica nunca: si el estudiante repite el ejercicio se guarda uno nuevo.
 */
@Service
public class RegistroDeIntentos implements ResponderEjercicio, ConsultarHistorialDeIntentos {

    private static final int TAMANO_PAGINA = 20;

    private final IntentoRepositorio intentos;
    private final EjercicioRepositorio ejercicios;
    private final CatalogoRepositorio catalogos;
    private final ParametrosDelSistema parametros;
    private final Simulacros simulacros;
    private final Reloj reloj;

    public RegistroDeIntentos(IntentoRepositorio intentos, EjercicioRepositorio ejercicios,
                              CatalogoRepositorio catalogos, ParametrosDelSistema parametros,
                              Simulacros simulacros, Reloj reloj) {
        this.intentos = intentos;
        this.ejercicios = ejercicios;
        this.catalogos = catalogos;
        this.parametros = parametros;
        this.simulacros = simulacros;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public ResultadoDeIntento responder(Long idEstudiante, Respuesta respuesta) {
        validar(respuesta);

        // Si la conexión se cayó y el navegador reenvía la misma respuesta, se devuelve la que ya quedó.
        if (respuesta.tokenIdempotencia() != null) {
            Optional<Intento> yaRegistrado = intentos.porToken(respuesta.tokenIdempotencia());
            if (yaRegistrado.isPresent()) {
                Intento previo = yaRegistrado.get();
                if (!previo.idEstudiante().equals(idEstudiante)) {
                    throw new AccesoDenegado("El intento no te pertenece.");
                }
                return resultado(previo, true);
            }
        }

        Ejercicio ejercicio = ejercicios.porId(respuesta.idEjercicio())
                .orElseThrow(() -> new NoEncontrado("El ejercicio no existe."));
        if (!ejercicio.estaActivo()) {
            throw new RecursoNoDisponible("Este ejercicio ya no está disponible; no es posible registrar la respuesta.");
        }
        Opcion opcion = ejercicio.opcion(respuesta.idOpcion())
                .orElseThrow(() -> new DatosInvalidos("OPCION_INVALIDA",
                        "La opción seleccionada no pertenece al ejercicio."));

        if (respuesta.idSimulacro() != null) {
            simulacros.asegurarQuePuedeResponder(idEstudiante, respuesta.idSimulacro());
            if (intentos.yaRespondidoEnSimulacro(respuesta.idSimulacro(), ejercicio.id())) {
                throw new Conflicto("EJERCICIO_YA_RESPONDIDO", "Este ejercicio ya fue respondido en el simulacro.");
            }
        }

        TipoError tipoError = opcion.correcta() ? null : clasificar(idEstudiante, ejercicio, opcion, respuesta.nivelConfianza());
        Intento nuevo = Intento.nuevo(idEstudiante, ejercicio, opcion, respuesta.nivelConfianza(), tipoError,
                respuesta.idSimulacro(), respuesta.tokenIdempotencia(), reloj.ahora());
        return resultado(intentos.guardar(nuevo), false);
    }

    private void validar(Respuesta respuesta) {
        List<String> errores = new ArrayList<>();
        if (respuesta.idEjercicio() == null) errores.add("Falta el ejercicio.");
        if (respuesta.idOpcion() == null) errores.add("Debes seleccionar una opción de respuesta.");
        if (respuesta.nivelConfianza() == null || respuesta.nivelConfianza() < 1 || respuesta.nivelConfianza() > 5) {
            errores.add("Debes indicar un nivel de confianza entre 1 y 5.");
        }
        if (!errores.isEmpty()) throw new DatosInvalidos(errores);
    }

    /** Se calcula una sola vez, al momento de responder, y se guarda con el intento (HU-027 CA-06). */
    private TipoError clasificar(Long idEstudiante, Ejercicio ejercicio, Opcion opcion, int nivelConfianza) {
        var ventana = new ClasificadorDeError.Ventana(
                parametros.entero(ParametrosDelSistema.VENTANA_INTENTOS, 5),
                parametros.entero(ParametrosDelSistema.MINIMO_INTENTOS_VENTANA, 3),
                parametros.entero(ParametrosDelSistema.UMBRAL_DOMINIO_PCT, 70));
        List<Boolean> previos = intentos.ultimosResultados(idEstudiante, ejercicio.componente().id(),
                ejercicio.competencia().id(), ventana.tamano());
        String tipo = ClasificadorDeError.clasificar(nivelConfianza, opcion, previos, ventana);
        return catalogos.tipoDeError(tipo);
    }

    private ResultadoDeIntento resultado(Intento intento, boolean repetido) {
        Opcion elegida = intento.opcionSeleccionada();
        Long idCorrecta = intento.ejercicio().opcionCorrecta().map(Opcion::id).orElse(null);
        long numero = intentos.cantidadDeIntentos(intento.idEstudiante(), intento.ejercicio().id());
        return new ResultadoDeIntento(intento.id(), intento.correcto(), elegida.id(), idCorrecta,
                elegida.retroalimentacionParaMostrar(), elegida.tieneRetroalimentacion(), intento.nivelConfianza(),
                intento.fechaHora(), numero, repetido, intento.idSimulacro());
    }

    @Override
    @Transactional(readOnly = true)
    public Pagina<ResumenDeIntento> listar(Long idEstudiante, int pagina) {
        return intentos.historialDe(idEstudiante, Math.max(0, pagina), TAMANO_PAGINA);
    }

    @Override
    @Transactional(readOnly = true)
    public DetalleDeIntento detalle(Long idEstudiante, Long idIntento) {
        // La consulta filtra por estudiante: nadie ve los intentos de otro (HU-025 CA-11).
        return intentos.detalle(idIntento, idEstudiante)
                .orElseThrow(() -> new NoEncontrado("El intento no existe."));
    }
}
