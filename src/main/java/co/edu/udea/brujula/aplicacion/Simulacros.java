package co.edu.udea.brujula.aplicacion;

import co.edu.udea.brujula.dominio.excepcion.Conflicto;
import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.modelo.*;
import co.edu.udea.brujula.dominio.modelo.consulta.*;
import co.edu.udea.brujula.dominio.puerto.entrada.GestionarSimulacro;
import co.edu.udea.brujula.dominio.puerto.salida.*;
import co.edu.udea.brujula.dominio.servicio.CalculadoraDeDesempeno;
import co.edu.udea.brujula.dominio.servicio.GeneradorDeRecomendaciones;
import co.edu.udea.brujula.dominio.servicio.Porcentajes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Ciclo completo del simulacro (HU-013 a HU-018 y HU-026).
 *
 * El cierre por tiempo no lo dispara ningún proceso en segundo plano: cada vez que se toca el
 * simulacro se revisa si ya se venció y, si es así, se cierra ahí mismo. Así el resultado es el
 * mismo aunque el estudiante cierre el navegador a mitad de camino.
 */
@Service
public class Simulacros implements GestionarSimulacro {

    private static final int TAMANO_PAGINA = 20;

    private final SimulacroRepositorio simulacros;
    private final IntentoRepositorio intentos;
    private final EjercicioRepositorio ejercicios;
    private final CatalogoRepositorio catalogos;
    private final ParametrosDelSistema parametros;
    private final Reloj reloj;

    public Simulacros(SimulacroRepositorio simulacros, IntentoRepositorio intentos, EjercicioRepositorio ejercicios,
                      CatalogoRepositorio catalogos, ParametrosDelSistema parametros, Reloj reloj) {
        this.simulacros = simulacros;
        this.intentos = intentos;
        this.ejercicios = ejercicios;
        this.catalogos = catalogos;
        this.parametros = parametros;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public EstadoDelSimulacro iniciar(Long idEstudiante, Long idDuracion) {
        if (idDuracion == null) {
            throw new DatosInvalidos(List.of("Debes seleccionar la duración del simulacro."));
        }
        DuracionSimulacro duracion = catalogos.duracion(idDuracion)
                .orElseThrow(() -> new DatosInvalidos("DURACION_INVALIDA", "La duración seleccionada no está disponible."));

        Instant ahora = reloj.ahora();
        for (Simulacro abierto : simulacros.enCursoDe(idEstudiante)) {
            if (abierto.tiempoAgotado(ahora)) {
                cerrar(abierto, ahora);
            } else {
                throw new Conflicto("SIMULACRO_EN_CURSO",
                        "Ya tienes un simulacro en curso. Continúalo o finalízalo antes de iniciar otro.",
                        Map.of("idSimulacro", abierto.id()));
            }
        }
        if (ejercicios.cantidadDeActivos() == 0) {
            throw new Conflicto("SIN_EJERCICIOS", "Aún no hay ejercicios disponibles para hacer un simulacro.");
        }
        Simulacro nuevo = simulacros.guardar(Simulacro.iniciar(idEstudiante, duracion, ahora));
        return estadoDe(nuevo, ahora);
    }

    @Override
    @Transactional
    public Optional<EstadoDelSimulacro> enCurso(Long idEstudiante) {
        Instant ahora = reloj.ahora();
        for (Simulacro abierto : simulacros.enCursoDe(idEstudiante)) {
            if (abierto.tiempoAgotado(ahora)) {
                cerrar(abierto, ahora);
            } else {
                return Optional.of(estadoDe(abierto, ahora));
            }
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public EstadoDelSimulacro estado(Long idEstudiante, Long idSimulacro) {
        Instant ahora = reloj.ahora();
        Simulacro simulacro = cerrarSiSeVencio(propio(idEstudiante, idSimulacro), ahora);
        return estadoDe(simulacro, ahora);
    }

    @Override
    @Transactional
    public SiguienteDelSimulacro siguienteEjercicio(Long idEstudiante, Long idSimulacro) {
        Instant ahora = reloj.ahora();
        Simulacro simulacro = propio(idEstudiante, idSimulacro);
        if (!simulacro.enCurso()) {
            return new SiguienteDelSimulacro(true, SiguienteDelSimulacro.YA_FINALIZADO, null, estadoDe(simulacro, ahora));
        }
        if (simulacro.tiempoAgotado(ahora)) {
            cerrar(simulacro, ahora);
            return new SiguienteDelSimulacro(true, SiguienteDelSimulacro.POR_TIEMPO, null, estadoDe(simulacro, ahora));
        }
        Optional<Long> siguiente = ejercicios.siguienteParaSimulacro(simulacro.id());
        if (siguiente.isEmpty()) {
            // Ya respondió todo lo que había: no tiene sentido dejarlo esperando (HU-015 CA-03).
            cerrar(simulacro, ahora);
            return new SiguienteDelSimulacro(true, SiguienteDelSimulacro.SIN_EJERCICIOS, null, estadoDe(simulacro, ahora));
        }
        Ejercicio ejercicio = ejercicios.porId(siguiente.get()).orElseThrow();
        return new SiguienteDelSimulacro(false, null, ejercicio, estadoDe(simulacro, ahora));
    }

    @Override
    @Transactional
    public ResultadoDeSimulacro finalizar(Long idEstudiante, Long idSimulacro) {
        Simulacro simulacro = propio(idEstudiante, idSimulacro);
        if (simulacro.enCurso()) {
            cerrar(simulacro, reloj.ahora());
        }
        return armarResultado(simulacro);
    }

    @Override
    @Transactional
    public ResultadoDeSimulacro resultado(Long idEstudiante, Long idSimulacro) {
        Instant ahora = reloj.ahora();
        Simulacro simulacro = cerrarSiSeVencio(propio(idEstudiante, idSimulacro), ahora);
        if (simulacro.enCurso()) {
            throw new Conflicto("SIMULACRO_EN_CURSO", "El simulacro aún está en curso.",
                    Map.of("idSimulacro", simulacro.id()));
        }
        return armarResultado(simulacro);
    }

    @Override
    @Transactional
    public Pagina<ResumenDeSimulacro> historial(Long idEstudiante, int pagina) {
        Instant ahora = reloj.ahora();
        simulacros.enCursoDe(idEstudiante).stream()
                .filter(s -> s.tiempoAgotado(ahora))
                .forEach(s -> cerrar(s, ahora));

        return simulacros.finalizadosDe(idEstudiante, Math.max(0, pagina), TAMANO_PAGINA).mapear(s -> {
            long total = intentos.respondidosEn(s.id());
            long correctas = total == 0 ? 0 : intentos.correctasEn(s.id());
            return new ResumenDeSimulacro(s.id(), s.inicio(), s.fin(), s.duracion().minutos(),
                    s.tiempoUtilizadoSeg(), total, correctas, total - correctas, Porcentajes.de(correctas, total));
        });
    }

    /**
     * Comprueba que el simulacro siga abierto antes de aceptar una respuesta. Lo llama el caso de
     * uso de responder ejercicio.
     */
    @Transactional
    public Simulacro asegurarQuePuedeResponder(Long idEstudiante, Long idSimulacro) {
        Instant ahora = reloj.ahora();
        Simulacro simulacro = cerrarSiSeVencio(propio(idEstudiante, idSimulacro), ahora);
        if (!simulacro.enCurso()) {
            throw new Conflicto("SIMULACRO_FINALIZADO",
                    "El simulacro ya finalizó; no es posible registrar más respuestas.",
                    Map.of("idSimulacro", simulacro.id()));
        }
        return simulacro;
    }

    private Simulacro cerrarSiSeVencio(Simulacro simulacro, Instant ahora) {
        if (simulacro.enCurso() && simulacro.tiempoAgotado(ahora)) {
            cerrar(simulacro, ahora);
        }
        return simulacro;
    }

    /** Cierra el simulacro y deja escritas las recomendaciones con los resultados de ese momento. */
    private void cerrar(Simulacro simulacro, Instant ahora) {
        simulacro.finalizar(ahora);
        simulacros.guardar(simulacro);

        List<Intento> respondidos = intentos.delSimulacro(simulacro.id());
        int umbral = parametros.entero(ParametrosDelSistema.UMBRAL_RECOMENDACION_PCT, 60);
        List<Recomendacion> recomendaciones = GeneradorDeRecomendaciones.generar(respondidos, umbral);
        if (!recomendaciones.isEmpty()) {
            simulacros.guardarRecomendaciones(simulacro.id(), recomendaciones);
        }
    }

    private EstadoDelSimulacro estadoDe(Simulacro simulacro, Instant ahora) {
        long respondidos = intentos.respondidosEn(simulacro.id());
        long correctas = respondidos == 0 ? 0 : intentos.correctasEn(simulacro.id());
        return new EstadoDelSimulacro(simulacro.id(), simulacro.estado(), simulacro.duracion().minutos(),
                simulacro.inicio(), simulacro.finPrevisto(), ahora,
                simulacro.enCurso() ? simulacro.segundosRestantes(ahora) : 0, respondidos, correctas);
    }

    private ResultadoDeSimulacro armarResultado(Simulacro simulacro) {
        List<Intento> respondidos = intentos.delSimulacro(simulacro.id());
        long total = respondidos.size();
        long correctas = respondidos.stream().filter(Intento::correcto).count();
        List<Recomendacion> recomendaciones = simulacros.recomendacionesDe(simulacro.id());

        String mensaje = null;
        if (total == 0) {
            mensaje = GeneradorDeRecomendaciones.SIN_RESPUESTAS;
        } else if (recomendaciones.isEmpty()) {
            mensaje = GeneradorDeRecomendaciones.BUEN_DESEMPENO;
        }
        return new ResultadoDeSimulacro(simulacro.id(), simulacro.inicio(), simulacro.fin(),
                simulacro.duracion().minutos(), simulacro.tiempoUtilizadoSeg(), total, correctas, total - correctas,
                Porcentajes.de(correctas, total),
                CalculadoraDeDesempeno.porComponente(respondidos),
                CalculadoraDeDesempeno.porCompetencia(respondidos),
                recomendaciones, mensaje, intentos.detalleDelSimulacro(simulacro.id()));
    }

    private Simulacro propio(Long idEstudiante, Long idSimulacro) {
        return simulacros.deEstudiante(idSimulacro, idEstudiante)
                .orElseThrow(() -> new NoEncontrado("El simulacro no existe."));
    }
}
