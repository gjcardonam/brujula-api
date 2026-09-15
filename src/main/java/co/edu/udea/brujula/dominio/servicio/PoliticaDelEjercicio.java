package co.edu.udea.brujula.dominio.servicio;

import co.edu.udea.brujula.dominio.modelo.Opcion;
import co.edu.udea.brujula.dominio.puerto.entrada.comando.DatosDeEjercicio;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PoliticaDelEjercicio {

    public static final int MINIMO_DE_OPCIONES = 2;
    public static final int MAXIMO_DE_OPCIONES = 6;

    private PoliticaDelEjercicio() {
    }

    public static List<String> revisar(DatosDeEjercicio datos) {
        List<String> errores = new ArrayList<>();
        if (datos.enunciado() == null || datos.enunciado().isBlank()) errores.add("El enunciado es obligatorio.");
        if (datos.idComponente() == null) errores.add("Debes seleccionar un componente.");
        if (datos.idCompetencia() == null) errores.add("Debes seleccionar una competencia.");
        if (datos.idNivel() == null) errores.add("Debes seleccionar un nivel de dificultad.");
        errores.addAll(revisarOpciones(datos.opciones() == null ? List.of() : datos.opciones()));
        return errores;
    }

    private static List<String> revisarOpciones(List<DatosDeEjercicio.DatosDeOpcion> opciones) {
        List<String> errores = new ArrayList<>();
        if (opciones.size() < MINIMO_DE_OPCIONES) {
            errores.add("El ejercicio debe tener al menos dos opciones de respuesta.");
            return errores;
        }
        if (opciones.size() > MAXIMO_DE_OPCIONES) {
            errores.add("El ejercicio admite un máximo de seis opciones de respuesta.");
        }
        if (opciones.stream().filter(DatosDeEjercicio.DatosDeOpcion::correcta).count() != 1) {
            errores.add("El ejercicio debe tener exactamente una opción marcada como correcta.");
        }
        errores.addAll(revisarCadaOpcion(opciones));
        return errores;
    }

    private static List<String> revisarCadaOpcion(List<DatosDeEjercicio.DatosDeOpcion> opciones) {
        List<String> errores = new ArrayList<>();
        Set<String> vistas = new HashSet<>();
        for (int posicion = 0; posicion < opciones.size(); posicion++) {
            DatosDeEjercicio.DatosDeOpcion datos = opciones.get(posicion);
            Opcion opcion = new Opcion(null, datos.texto(), datos.imagen(), datos.correcta(),
                    datos.retroalimentacion(), posicion + 1);
            if (!opcion.tieneContenido()) {
                errores.add("La opción " + opcion.letra() + " debe tener texto o imagen.");
                continue;
            }
            if (!opcion.tieneRetroalimentacion()) {
                errores.add("La opción " + opcion.letra() + " debe tener retroalimentación.");
            }
            if (!vistas.add(opcion.claveDeComparacion())) {
                errores.add("Hay opciones de respuesta duplicadas (" + opcion.letra() + ").");
            }
        }
        return errores;
    }
}
