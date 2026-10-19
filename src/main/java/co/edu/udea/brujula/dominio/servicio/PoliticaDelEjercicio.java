package co.edu.udea.brujula.dominio.servicio;

import co.edu.udea.brujula.dominio.excepcion.Conflicto;
import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.modelo.Opcion;
import co.edu.udea.brujula.dominio.modelo.TipoError;
import co.edu.udea.brujula.dominio.puerto.entrada.comando.DatosDeEjercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.comando.DatosDeEjercicio.DatosDeOpcion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public final class PoliticaDelEjercicio {

    public static final int MINIMO_DE_OPCIONES = 2;
    public static final int MAXIMO_DE_OPCIONES = 6;

    private PoliticaDelEjercicio() {
    }

    public static List<String> revisar(DatosDeEjercicio datos) {
        List<String> errores = new ArrayList<>();
        if (datos.enunciado() == null || datos.enunciado().isBlank()) {
            errores.add("El enunciado es obligatorio.");
        }
        if (datos.idComponente() == null) {
            errores.add("Debes seleccionar un componente.");
        }
        if (datos.idCompetencia() == null) {
            errores.add("Debes seleccionar una competencia.");
        }
        if (datos.idNivel() == null) {
            errores.add("Debes seleccionar un nivel de dificultad.");
        }
        errores.addAll(revisarOpciones(datos.opciones() == null ? List.of() : datos.opciones()));
        return errores;
    }

    public static List<Opcion> armarOpciones(List<DatosDeOpcion> recibidas, Set<Long> opcionesUsadas,
                                             List<Opcion> opcionesActuales,
                                             Function<String, String> nombreDeImagen,
                                             Function<Long, TipoError> tipoDeError) {
        Map<Long, Opcion> actuales = new HashMap<>();
        opcionesActuales.forEach(opcion -> actuales.put(opcion.id(), opcion));

        Set<Long> conservadas = new HashSet<>();
        recibidas.stream().map(DatosDeOpcion::id).filter(Objects::nonNull).forEach(conservadas::add);
        for (Long usada : opcionesUsadas) {
            if (!conservadas.contains(usada)) {
                Opcion opcion = actuales.get(usada);
                throw new Conflicto("OPCION_CON_INTENTOS", "La opción "
                        + (opcion == null ? "" : opcion.letra())
                        + " ya fue usada en intentos de estudiantes y no puede eliminarse.");
            }
        }

        List<Opcion> resultado = new ArrayList<>();
        for (int posicion = 0; posicion < recibidas.size(); posicion++) {
            DatosDeOpcion datos = recibidas.get(posicion);
            String texto = sinEspacios(datos.texto());
            String imagen = sinEspacios(nombreDeImagen.apply(datos.imagen() == null ? "" : datos.imagen().trim()));
            TipoError tipoError = datos.correcta() || datos.idTipoError() == null
                    ? null
                    : tipoDeError.apply(datos.idTipoError());

            if (datos.id() != null) {
                Opcion actual = actuales.get(datos.id());
                if (actual == null) {
                    throw new DatosInvalidos("OPCION_INVALIDA",
                            "Una de las opciones no pertenece a este ejercicio.");
                }
                if (opcionesUsadas.contains(datos.id())) {
                    boolean cambioLoQueNoDebe = !Objects.equals(texto, actual.texto())
                            || !Objects.equals(imagen, actual.imagen())
                            || datos.correcta() != actual.correcta();
                    if (cambioLoQueNoDebe) {
                        throw new Conflicto("OPCION_CON_INTENTOS", "La opción " + actual.letra()
                                + " ya fue usada en intentos de estudiantes: solo puede cambiarse su "
                                + "retroalimentación o tipo de error.");
                    }
                }
            }
            resultado.add(new Opcion(datos.id(), texto, imagen, datos.correcta(),
                    datos.retroalimentacion().trim(), tipoError, posicion + 1));
        }
        return resultado;
    }

    private static String sinEspacios(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static List<String> revisarOpciones(List<DatosDeOpcion> opciones) {
        List<String> errores = new ArrayList<>();
        if (opciones.size() < MINIMO_DE_OPCIONES) {
            errores.add("El ejercicio debe tener al menos dos opciones de respuesta.");
            return errores;
        }
        if (opciones.size() > MAXIMO_DE_OPCIONES) {
            errores.add("El ejercicio admite un máximo de seis opciones de respuesta.");
        }
        if (opciones.stream().filter(DatosDeOpcion::correcta).count() != 1) {
            errores.add("El ejercicio debe tener exactamente una opción marcada como correcta.");
        }
        errores.addAll(revisarCadaOpcion(opciones));
        return errores;
    }

    private static List<String> revisarCadaOpcion(List<DatosDeOpcion> opciones) {
        List<String> errores = new ArrayList<>();
        Set<String> vistas = new HashSet<>();
        for (int posicion = 0; posicion < opciones.size(); posicion++) {
            DatosDeOpcion datos = opciones.get(posicion);
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
