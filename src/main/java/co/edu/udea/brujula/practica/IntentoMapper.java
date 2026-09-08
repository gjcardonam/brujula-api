package co.edu.udea.brujula.practica;

import co.edu.udea.brujula.ejercicio.ArchivoService;
import co.edu.udea.brujula.ejercicio.Ejercicio;
import co.edu.udea.brujula.ejercicio.OpcionRespuesta;
import co.edu.udea.brujula.practica.IntentoDtos.IntentoDetalle;
import co.edu.udea.brujula.practica.IntentoDtos.OpcionHistorial;
import org.springframework.stereotype.Component;

import java.util.List;

/** Convierte un intento en su detalle histórico (HU-025 CA-07, HU-017 CA-05/CA-06). */
@Component
public class IntentoMapper {

    public static final String SIN_RETROALIMENTACION = "No hay una explicación disponible para esta respuesta.";

    private final ArchivoService archivos;

    public IntentoMapper(ArchivoService archivos) {
        this.archivos = archivos;
    }

    public IntentoDetalle detalle(Intento i) {
        Ejercicio e = i.getEjercicio();
        OpcionRespuesta sel = i.getOpcionSeleccionada();
        List<OpcionHistorial> ops = e.getOpciones().stream().map(o -> new OpcionHistorial(o.getId(), o.getLetra(),
                o.getDescripcion(), archivos.url(o.getImagen()), o.getId().equals(sel.getId()), o.isEsCorrecta())).toList();
        String retro = sel.getRetroalimentacion();
        return new IntentoDetalle(i.getId(), e.getId(), e.getNumero(), e.getEnunciado(), archivos.url(e.getImagenEnunciado()),
                e.getComponente().getNombre(), e.getCompetencia().getNombre(), e.getNivelDificultad().getNivel(), e.getEstado(),
                ops, i.isEsCorrecto(), i.getNivelConfianza(), i.getFechaHora(),
                retro == null || retro.isBlank() ? SIN_RETROALIMENTACION : retro,
                i.getSimulacro() == null ? null : i.getSimulacro().getId());
    }
}
