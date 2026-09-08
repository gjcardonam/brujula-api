package co.edu.udea.brujula.aplicacion;

import co.edu.udea.brujula.dominio.excepcion.Conflicto;
import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.modelo.*;
import co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeEjercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.AdministrarEjercicios;
import co.edu.udea.brujula.dominio.puerto.entrada.GuardarImagen;
import co.edu.udea.brujula.dominio.puerto.entrada.comando.DatosDeEjercicio;
import co.edu.udea.brujula.dominio.puerto.salida.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Gestión del banco por parte del administrador (HU-020 a HU-024).
 *
 * La regla que más cuidado pide es la de HU-021 CA-09: si un ejercicio ya tiene intentos, sus
 * opciones no pueden cambiar de contenido ni desaparecer, porque el historial de los estudiantes
 * dejaría de tener sentido. Lo único editable en esas opciones es la retroalimentación.
 */
@Service
public class AdministracionDeEjercicios implements AdministrarEjercicios, GuardarImagen {

    private final EjercicioRepositorio ejercicios;
    private final IntentoRepositorio intentos;
    private final CatalogoRepositorio catalogos;
    private final UsuarioRepositorio usuarios;
    private final AuditoriaRepositorio auditoria;
    private final AlmacenDeImagenes imagenes;
    private final Reloj reloj;

    public AdministracionDeEjercicios(EjercicioRepositorio ejercicios, IntentoRepositorio intentos,
                                      CatalogoRepositorio catalogos, UsuarioRepositorio usuarios,
                                      AuditoriaRepositorio auditoria, AlmacenDeImagenes imagenes, Reloj reloj) {
        this.ejercicios = ejercicios;
        this.intentos = intentos;
        this.catalogos = catalogos;
        this.usuarios = usuarios;
        this.auditoria = auditoria;
        this.imagenes = imagenes;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public DetalleDeEjercicio crear(Long idAdministrador, DatosDeEjercicio datos) {
        Usuario creador = usuarios.porId(idAdministrador).orElseThrow();
        Contenido contenido = validar(datos, null, Set.of(), List.of());

        Ejercicio nuevo = new Ejercicio(null, null, contenido.enunciado(), contenido.imagen(), contenido.nivel(),
                contenido.componente(), contenido.competencia(), Ejercicio.ACTIVO, reloj.ahora(),
                creador.id(), creador.nombreCompleto(), contenido.opciones());

        Ejercicio guardado = ejercicios.guardar(nuevo);
        auditoria.registrar(Auditoria.de(Auditoria.CREACION, guardado.id(), idAdministrador, reloj.ahora()));
        return detalleDe(guardado.id());
    }

    @Override
    @Transactional
    public DetalleDeEjercicio editar(Long idAdministrador, Long idEjercicio, DatosDeEjercicio datos) {
        Ejercicio ejercicio = buscar(idEjercicio);
        Set<Long> opcionesUsadas = intentos.opcionesUsadas(idEjercicio);
        Contenido contenido = validar(datos, idEjercicio, opcionesUsadas, ejercicio.opciones());

        // No se toca ni el número ni el estado: editar no publica ni despublica (HU-021 CA-06 y CA-07).
        ejercicio.actualizarContenido(contenido.enunciado(), contenido.imagen(), contenido.componente(),
                contenido.competencia(), contenido.nivel(), contenido.opciones());

        ejercicios.guardar(ejercicio);
        auditoria.registrar(Auditoria.de(Auditoria.EDICION, idEjercicio, idAdministrador, reloj.ahora()));
        return detalleDe(idEjercicio);
    }

    @Override
    @Transactional(readOnly = true)
    public DetalleDeEjercicio consultarDetalle(Long idEjercicio) {
        buscar(idEjercicio);
        return detalleDe(idEjercicio);
    }

    @Override
    @Transactional
    public DetalleDeEjercicio cambiarEstado(Long idAdministrador, Long idEjercicio, String nuevoEstado) {
        if (!Ejercicio.ACTIVO.equals(nuevoEstado) && !Ejercicio.DESACTIVADO.equals(nuevoEstado)) {
            throw new DatosInvalidos("ESTADO_INVALIDO", "El estado debe ser Activo o Desactivado.");
        }
        Ejercicio ejercicio = buscar(idEjercicio);
        if (nuevoEstado.equals(ejercicio.estado())) {
            throw new Conflicto("ESTADO_SIN_CAMBIO", "El ejercicio ya está " + nuevoEstado.toLowerCase() + ".");
        }
        boolean activar = Ejercicio.ACTIVO.equals(nuevoEstado);
        if (activar) {
            ejercicio.activar();
        } else {
            ejercicio.desactivar();   // nunca se borra: los intentos históricos se conservan (HU-023 CA-04)
        }
        ejercicios.guardar(ejercicio);
        auditoria.registrar(Auditoria.de(activar ? Auditoria.ACTIVACION : Auditoria.DESACTIVACION,
                idEjercicio, idAdministrador, reloj.ahora()));
        return detalleDe(idEjercicio);
    }

    @Override
    public AlmacenDeImagenes.Imagen guardar(byte[] contenido) {
        return imagenes.guardar(contenido);
    }

    private record Contenido(String enunciado, String imagen, Componente componente, Competencia competencia,
                             NivelDificultad nivel, List<Opcion> opciones) {
    }

    private Contenido validar(DatosDeEjercicio datos, Long idEjercicio, Set<Long> opcionesUsadas,
                              List<Opcion> opcionesActuales) {
        List<String> errores = new ArrayList<>();
        String enunciado = datos.enunciado() == null ? "" : datos.enunciado().trim();
        if (enunciado.isEmpty()) errores.add("El enunciado es obligatorio.");
        if (datos.idComponente() == null) errores.add("Debes seleccionar un componente.");
        if (datos.idCompetencia() == null) errores.add("Debes seleccionar una competencia.");
        if (datos.idNivel() == null) errores.add("Debes seleccionar un nivel de dificultad.");

        List<DatosDeEjercicio.DatosDeOpcion> recibidas = datos.opciones() == null ? List.of() : datos.opciones();
        if (recibidas.size() < 2) errores.add("El ejercicio debe tener al menos dos opciones de respuesta.");
        long correctas = recibidas.stream().filter(DatosDeEjercicio.DatosDeOpcion::correcta).count();
        if (recibidas.size() >= 2 && correctas != 1) {
            errores.add("El ejercicio debe tener exactamente una opción marcada como correcta.");
        }
        errores.addAll(revisarOpciones(recibidas));

        String imagenEnunciado = imagenes.nombreDe(datos.imagen() == null ? "" : datos.imagen().trim());
        if (!imagenEnunciado.isEmpty() && !imagenes.existe(imagenEnunciado)) {
            errores.add("La imagen del enunciado no existe en el servidor.");
        }
        if (!errores.isEmpty()) throw new DatosInvalidos(errores);

        if (ejercicios.existeOtroConEnunciado(enunciado, idEjercicio)) {
            throw new Conflicto("ENUNCIADO_DUPLICADO", "Ya existe un ejercicio con el mismo enunciado.");
        }
        Componente componente = catalogos.componente(datos.idComponente()).filter(Componente::estaActivo)
                .orElseThrow(() -> new DatosInvalidos("COMPONENTE_INVALIDO",
                        "El componente seleccionado no existe o no está activo."));
        Competencia competencia = catalogos.competencia(datos.idCompetencia()).filter(Competencia::estaActiva)
                .orElseThrow(() -> new DatosInvalidos("COMPETENCIA_INVALIDA",
                        "La competencia seleccionada no existe o no está activa."));
        NivelDificultad nivel = catalogos.nivel(datos.idNivel())
                .orElseThrow(() -> new DatosInvalidos("NIVEL_INVALIDO", "El nivel de dificultad no existe."));

        List<Opcion> opciones = armarOpciones(recibidas, opcionesUsadas, opcionesActuales);
        return new Contenido(enunciado, imagenEnunciado.isEmpty() ? null : imagenEnunciado,
                componente, competencia, nivel, opciones);
    }

    private List<String> revisarOpciones(List<DatosDeEjercicio.DatosDeOpcion> recibidas) {
        List<String> errores = new ArrayList<>();
        Set<String> vistas = new HashSet<>();
        for (int i = 0; i < recibidas.size(); i++) {
            DatosDeEjercicio.DatosDeOpcion opcion = recibidas.get(i);
            char letra = (char) ('A' + i);
            String texto = opcion.texto() == null ? "" : opcion.texto().trim();
            String imagen = imagenes.nombreDe(opcion.imagen() == null ? "" : opcion.imagen().trim());

            if (texto.isEmpty() && imagen.isEmpty()) {
                errores.add("La opción " + letra + " debe tener texto o imagen.");
            }
            if (opcion.retroalimentacion() == null || opcion.retroalimentacion().isBlank()) {
                errores.add("La opción " + letra + " debe tener retroalimentación.");
            }
            String clave = (texto.toLowerCase() + "|" + imagen).trim();
            if (!clave.equals("|") && !vistas.add(clave)) {
                errores.add("Hay opciones de respuesta duplicadas (" + letra + ").");
            }
            if (!imagen.isEmpty() && !imagenes.existe(imagen)) {
                errores.add("La imagen de la opción " + letra + " no existe en el servidor.");
            }
        }
        return errores;
    }

    private List<Opcion> armarOpciones(List<DatosDeEjercicio.DatosDeOpcion> recibidas, Set<Long> opcionesUsadas,
                                       List<Opcion> opcionesActuales) {
        Map<Long, Opcion> actuales = new HashMap<>();
        opcionesActuales.forEach(o -> actuales.put(o.id(), o));

        Set<Long> conservadas = new HashSet<>();
        recibidas.stream().map(DatosDeEjercicio.DatosDeOpcion::id).filter(Objects::nonNull).forEach(conservadas::add);
        for (Long usada : opcionesUsadas) {
            if (!conservadas.contains(usada)) {
                Opcion opcion = actuales.get(usada);
                throw new Conflicto("OPCION_CON_INTENTOS", "La opción " + (opcion == null ? "" : opcion.letra())
                        + " ya fue usada en intentos de estudiantes y no puede eliminarse.");
            }
        }

        List<Opcion> resultado = new ArrayList<>();
        for (int i = 0; i < recibidas.size(); i++) {
            DatosDeEjercicio.DatosDeOpcion datos = recibidas.get(i);
            String texto = vacioANulo(datos.texto());
            String imagen = vacioANulo(imagenes.nombreDe(datos.imagen() == null ? "" : datos.imagen().trim()));
            TipoError tipoError = datos.correcta() || datos.idTipoError() == null ? null
                    : catalogos.tipoDeError(datos.idTipoError())
                    .orElseThrow(() -> new DatosInvalidos("TIPO_ERROR_INVALIDO", "El tipo de error no existe."));

            if (datos.id() != null) {
                Opcion actual = actuales.get(datos.id());
                if (actual == null) {
                    throw new DatosInvalidos("OPCION_INVALIDA", "Una de las opciones no pertenece a este ejercicio.");
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
                    datos.retroalimentacion().trim(), tipoError, i + 1));
        }
        return resultado;
    }

    private static String vacioANulo(String valor) {
        return valor == null || valor.trim().isEmpty() ? null : valor.trim();
    }

    private Ejercicio buscar(Long idEjercicio) {
        return ejercicios.porId(idEjercicio).orElseThrow(() -> new NoEncontrado("El ejercicio no existe."));
    }

    private DetalleDeEjercicio detalleDe(Long idEjercicio) {
        Ejercicio ejercicio = buscar(idEjercicio);
        return new DetalleDeEjercicio(ejercicio, intentos.usoDe(idEjercicio), intentos.opcionesUsadas(idEjercicio));
    }
}
