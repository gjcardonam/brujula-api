package co.edu.udea.brujula.aplicacion;

import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.excepcion.RecursoNoDisponible;
import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.consulta.ComponentesDelBanco;
import co.edu.udea.brujula.dominio.modelo.consulta.TarjetaDeEjercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.BuscarSiguienteEjercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarBanco;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarEjercicio;
import co.edu.udea.brujula.dominio.puerto.salida.EjercicioRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.IntentoRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.ParametrosDelSistema;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * Consulta del banco (HU-006 a HU-009). El estudiante solo ve ejercicios activos; el administrador
 * ve todos y además cuántos intentos lleva cada uno.
 */
@Service
public class BancoDeEjercicios implements ConsultarBanco, ConsultarEjercicio, BuscarSiguienteEjercicio {

    private final EjercicioRepositorio ejercicios;
    private final IntentoRepositorio intentos;
    private final ParametrosDelSistema parametros;

    public BancoDeEjercicios(EjercicioRepositorio ejercicios, IntentoRepositorio intentos,
                             ParametrosDelSistema parametros) {
        this.ejercicios = ejercicios;
        this.intentos = intentos;
        this.parametros = parametros;
    }

    @Override
    @Transactional(readOnly = true)
    public Pagina<TarjetaDeEjercicio> listar(boolean esAdministrador, Long idComponente, int pagina) {
        int tamano = parametros.entero(ParametrosDelSistema.TAMANO_PAGINA_BANCO, 20);
        Pagina<Ejercicio> encontrados = ejercicios.buscar(idComponente, !esAdministrador, Math.max(0, pagina), tamano);

        Map<Long, Long> conteos = Map.of();
        if (esAdministrador && !encontrados.contenido().isEmpty()) {
            conteos = intentos.conteoPorEjercicio(encontrados.contenido().stream().map(Ejercicio::id).toList());
        }
        final Map<Long, Long> intentosPorEjercicio = conteos;
        return encontrados.mapear(e -> new TarjetaDeEjercicio(e.id(), e.numero(), e.componente().nombre(),
                e.competencia().nombre(), e.nivel().nombre(), e.estado(),
                esAdministrador ? intentosPorEjercicio.getOrDefault(e.id(), 0L) : null));
    }

    @Override
    @Transactional(readOnly = true)
    public ComponentesDelBanco componentes(boolean esAdministrador) {
        return ejercicios.conteoPorComponente(!esAdministrador);
    }

    @Override
    @Transactional(readOnly = true)
    public ParaPracticar paraPracticar(Long idEstudiante, Long idEjercicio) {
        Ejercicio ejercicio = ejercicios.porId(idEjercicio)
                .orElseThrow(() -> new NoEncontrado("El ejercicio no existe."));
        if (!ejercicio.estaActivo()) {
            // Alcanzó a verlo en el banco pero lo desactivaron antes de abrirlo (HU-009 CA-03).
            throw new RecursoNoDisponible("Este ejercicio ya no está disponible.");
        }
        return new ParaPracticar(ejercicio, intentos.cantidadDeIntentos(idEstudiante, idEjercicio));
    }

    @Override
    @Transactional(readOnly = true)
    public Siguiente buscar(Long idEstudiante, Long idEjercicioActual, Long idComponente) {
        Optional<Long> siguiente = ejercicios.siguienteParaPractica(idEjercicioActual, idComponente, idEstudiante);
        if (siguiente.isPresent()) {
            return new Siguiente(true, siguiente.get(), null);
        }
        String mensaje = idComponente == null
                ? "No hay más ejercicios disponibles por ahora."
                : "No hay más ejercicios disponibles para este componente. Puedes volver al banco o cambiar el filtro.";
        return new Siguiente(false, null, mensaje);
    }
}
