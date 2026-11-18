package co.edu.udea.brujula.aplicacion.practica;

import co.edu.udea.brujula.dominio.excepcion.AccesoDenegado;
import co.edu.udea.brujula.dominio.excepcion.Conflicto;
import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.excepcion.RecursoNoDisponible;
import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.modelo.Intento;
import co.edu.udea.brujula.dominio.modelo.Opcion;
import co.edu.udea.brujula.dominio.modelo.Simulacro;
import co.edu.udea.brujula.dominio.modelo.TipoError;
import co.edu.udea.brujula.dominio.modelo.consulta.ResultadoDeIntento;
import co.edu.udea.brujula.dominio.puerto.entrada.RegistrarIntento;
import co.edu.udea.brujula.dominio.puerto.salida.CatalogoRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.EjercicioRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.IntentoRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.ParametrosDelSistema;
import co.edu.udea.brujula.dominio.puerto.salida.Reloj;
import co.edu.udea.brujula.dominio.puerto.salida.SimulacroRepositorio;
import co.edu.udea.brujula.dominio.servicio.CierreDeSimulacros;
import co.edu.udea.brujula.dominio.servicio.ClasificadorDeError;
import co.edu.udea.brujula.dominio.servicio.LimiteDeReintentos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RegistroDeIntento implements RegistrarIntento {

    private static final int CONFIANZA_MINIMA = 1;
    private static final int CONFIANZA_MAXIMA = 5;
    private static final int TIEMPO_MAXIMO_MS = 3_600_000;

    private final IntentoRepositorio intentos;
    private final EjercicioRepositorio ejercicios;
    private final CatalogoRepositorio catalogos;
    private final ParametrosDelSistema parametros;
    private final SimulacroRepositorio simulacros;
    private final CierreDeSimulacros cierre;
    private final Reloj reloj;

    public RegistroDeIntento(IntentoRepositorio intentos, EjercicioRepositorio ejercicios,
                             CatalogoRepositorio catalogos, ParametrosDelSistema parametros,
                             SimulacroRepositorio simulacros, CierreDeSimulacros cierre, Reloj reloj) {
        this.intentos = intentos;
        this.ejercicios = ejercicios;
        this.catalogos = catalogos;
        this.parametros = parametros;
        this.simulacros = simulacros;
        this.cierre = cierre;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public ResultadoDeIntento registrar(Long idEstudiante, Respuesta respuesta) {
        exigirTokenDeIdempotencia(respuesta);
        exigirRespuestaCompleta(respuesta);

        Optional<Simulacro> simulacro = Optional.ofNullable(respuesta.idSimulacro())
                .map(idSimulacro -> simulacros.deEstudianteParaResponder(idSimulacro, idEstudiante)
                        .orElseThrow(() -> new NoEncontrado("El simulacro no existe.")));

        Optional<Intento> yaRegistrado = buscarPorToken(idEstudiante, respuesta);
        if (yaRegistrado.isPresent()) return resultado(yaRegistrado.get(), true);

        Ejercicio ejercicio = ejercicios.porId(respuesta.idEjercicio())
                .orElseThrow(() -> new NoEncontrado("El ejercicio no existe."));
        if (!ejercicio.estaActivo()) {
            throw new RecursoNoDisponible(
                    "Este ejercicio ya no está disponible; no es posible registrar la respuesta.");
        }
        Opcion opcion = ejercicio.opcion(respuesta.idOpcion())
                .orElseThrow(() -> new DatosInvalidos("OPCION_INVALIDA",
                        "La opción seleccionada no pertenece al ejercicio."));

        Instant ahora = reloj.ahora();
        if (simulacro.isPresent()) exigirQueAdmitaLaRespuesta(simulacro.get(), ejercicio, ahora);
        else exigirQueQuedenIntentos(idEstudiante, ejercicio, ahora);
        TipoError tipoError = opcion.correcta()
                ? null
                : clasificar(idEstudiante, ejercicio, opcion, respuesta.nivelConfianza());
        Intento nuevo = Intento.nuevo(idEstudiante, ejercicio, opcion, respuesta.nivelConfianza(), tipoError,
                respuesta.tokenIdempotencia(), ahora, tiempoValido(respuesta.tiempoRespuestaMs()),
                respuesta.idSimulacro());
        return resultado(intentos.guardar(nuevo), false);
    }

    private static Integer tiempoValido(Integer tiempoMs) {
        return tiempoMs == null || tiempoMs <= 0 || tiempoMs > TIEMPO_MAXIMO_MS ? null : tiempoMs;
    }

    private TipoError clasificar(Long idEstudiante, Ejercicio ejercicio, Opcion opcion, int nivelConfianza) {
        var ventana = new ClasificadorDeError.Ventana(
                parametros.entero(ParametrosDelSistema.VENTANA_INTENTOS, 5),
                parametros.entero(ParametrosDelSistema.MINIMO_INTENTOS_VENTANA, 3),
                parametros.entero(ParametrosDelSistema.UMBRAL_DOMINIO_PCT, 70));
        List<Boolean> previos = intentos.ultimosResultados(idEstudiante, ejercicio.componente().id(),
                ejercicio.competencia().id(), ventana.tamano());
        return catalogos.tipoDeError(ClasificadorDeError.clasificar(nivelConfianza, opcion, previos, ventana));
    }

    private void exigirQueQuedenIntentos(Long idEstudiante, Ejercicio ejercicio, Instant ahora) {
        LimiteDeReintentos.Disponibilidad disponibilidad = LimiteDeReintentos.evaluar(
                intentos.practicaDe(idEstudiante, ejercicio.id()),
                parametros.entero(ParametrosDelSistema.MAX_INTENTOS_PRACTICA, 2),
                parametros.entero(ParametrosDelSistema.HORAS_ESPERA_REINTENTO, 24));
        if (disponibilidad.restantes() == 0) {
            throw new Conflicto("SIN_INTENTOS", "Ya usaste todos los intentos de este ejercicio.");
        }
        if (!disponibilidad.permiteResponder(ahora)) {
            throw new Conflicto("REINTENTO_EN_ESPERA", "Podrás volver a intentar este ejercicio más adelante.",
                    Map.of("disponibleDesde", disponibilidad.disponibleDesde().toString()));
        }
    }

    private void exigirQueAdmitaLaRespuesta(Simulacro simulacro, Ejercicio ejercicio, Instant ahora) {
        if (!cierre.cerrarSiSeVencio(simulacro, ahora).enCurso()) {
            throw new Conflicto("SIMULACRO_FINALIZADO",
                    "El simulacro ya finalizó; no es posible registrar más respuestas.",
                    Map.of("idSimulacro", simulacro.id()));
        }
        if (intentos.yaRespondidoEnSimulacro(simulacro.id(), ejercicio.id())) {
            throw new Conflicto("EJERCICIO_YA_RESPONDIDO", "Este ejercicio ya fue respondido en el simulacro.");
        }
    }

    private Optional<Intento> buscarPorToken(Long idEstudiante, Respuesta respuesta) {
        Optional<Intento> previo = intentos.porToken(respuesta.tokenIdempotencia());
        if (previo.isPresent() && !previo.get().idEstudiante().equals(idEstudiante)) {
            throw new AccesoDenegado("El intento no te pertenece.");
        }
        return previo;
    }

    private ResultadoDeIntento resultado(Intento intento, boolean repetido) {
        Opcion elegida = intento.opcionSeleccionada();
        Long idCorrecta = intento.ejercicio().opcionCorrecta().map(Opcion::id).orElse(null);
        long numero = intentos.cantidadDeIntentos(intento.idEstudiante(), intento.ejercicio().id());
        return new ResultadoDeIntento(intento.id(), intento.correcto(), elegida.id(), idCorrecta,
                elegida.retroalimentacionParaMostrar(), elegida.tieneRetroalimentacion(), intento.nivelConfianza(),
                intento.respondidoEn(), numero, repetido,
                intento.tipoError() == null ? null : intento.tipoError().nombre(), intento.idSimulacro());
    }

    private static void exigirTokenDeIdempotencia(Respuesta respuesta) {
        if (respuesta.tokenIdempotencia() == null) {
            throw new DatosInvalidos("TOKEN_IDEMPOTENCIA_REQUERIDO",
                    "Falta el token de idempotencia de la respuesta. Vuelve a enviarla desde el ejercicio.");
        }
    }

    private static void exigirRespuestaCompleta(Respuesta respuesta) {
        List<String> errores = new ArrayList<>();
        if (respuesta.idEjercicio() == null) errores.add("Falta el ejercicio.");
        if (respuesta.idOpcion() == null) errores.add("Debes seleccionar una opción de respuesta.");
        Integer confianza = respuesta.nivelConfianza();
        if (confianza == null || confianza < CONFIANZA_MINIMA || confianza > CONFIANZA_MAXIMA) {
            errores.add("Debes indicar un nivel de confianza entre 1 y 5.");
        }
        if (!errores.isEmpty()) throw new DatosInvalidos(errores);
    }
}
