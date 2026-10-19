package co.edu.udea.brujula.aplicacion.administracion;

import co.edu.udea.brujula.dominio.excepcion.Conflicto;
import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.modelo.Auditoria;
import co.edu.udea.brujula.dominio.modelo.Competencia;
import co.edu.udea.brujula.dominio.modelo.Componente;
import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.modelo.NivelDificultad;
import co.edu.udea.brujula.dominio.modelo.Opcion;
import co.edu.udea.brujula.dominio.modelo.TipoError;
import co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeEjercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.EditarEjercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.comando.DatosDeEjercicio;
import co.edu.udea.brujula.dominio.puerto.salida.AlmacenDeImagenes;
import co.edu.udea.brujula.dominio.puerto.salida.AuditoriaRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.CatalogoRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.EjercicioRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.IntentoRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.Reloj;
import co.edu.udea.brujula.dominio.servicio.PoliticaDelEjercicio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class EdicionDeEjercicio implements EditarEjercicio {

    private final EjercicioRepositorio ejercicios;
    private final IntentoRepositorio intentos;
    private final CatalogoRepositorio catalogos;
    private final AuditoriaRepositorio auditoria;
    private final AlmacenDeImagenes imagenes;
    private final Reloj reloj;

    public EdicionDeEjercicio(EjercicioRepositorio ejercicios, IntentoRepositorio intentos,
                              CatalogoRepositorio catalogos, AuditoriaRepositorio auditoria,
                              AlmacenDeImagenes imagenes, Reloj reloj) {
        this.ejercicios = ejercicios;
        this.intentos = intentos;
        this.catalogos = catalogos;
        this.auditoria = auditoria;
        this.imagenes = imagenes;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public DetalleDeEjercicio editar(Long idAdministrador, Long idEjercicio, DatosDeEjercicio datos) {
        Ejercicio ejercicio = buscar(idEjercicio);
        Set<Long> opcionesUsadas = intentos.opcionesUsadas(idEjercicio);

        List<String> errores = new ArrayList<>(PoliticaDelEjercicio.revisar(datos));
        errores.addAll(revisarImagenes(datos));
        if (!errores.isEmpty()) throw new DatosInvalidos(errores);

        String enunciado = datos.enunciado().trim();
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

        List<Opcion> opciones = PoliticaDelEjercicio.armarOpciones(datos.opciones(), opcionesUsadas,
                ejercicio.opciones(), this::nombreDeImagen, this::tipoDeError);

        ejercicios.guardar(ejercicio.conContenido(enunciado, nombreDeImagen(datos.imagen()),
                nivel, componente, competencia, opciones));
        auditoria.registrar(Auditoria.de(Auditoria.EDICION, idEjercicio, idAdministrador, reloj.ahora()));
        return detalleDe(idEjercicio);
    }

    private List<String> revisarImagenes(DatosDeEjercicio datos) {
        List<String> errores = new ArrayList<>();
        String enunciado = nombreDeImagen(datos.imagen());
        if (enunciado != null && !imagenes.existe(enunciado)) {
            errores.add("La imagen del enunciado no existe en el servidor.");
        }
        List<DatosDeEjercicio.DatosDeOpcion> opciones = datos.opciones() == null ? List.of() : datos.opciones();
        for (int posicion = 0; posicion < opciones.size(); posicion++) {
            String imagen = nombreDeImagen(opciones.get(posicion).imagen());
            if (imagen != null && !imagenes.existe(imagen)) {
                errores.add("La imagen de la opción " + (char) ('A' + posicion) + " no existe en el servidor.");
            }
        }
        return errores;
    }

    private TipoError tipoDeError(Long idTipoError) {
        return catalogos.tipoDeError(idTipoError).orElseThrow(() -> new DatosInvalidos("TIPO_ERROR_INVALIDO",
                "El tipo de error no existe."));
    }

    private String nombreDeImagen(String valor) {
        String nombre = imagenes.nombreDe(valor == null ? "" : valor.trim());
        return nombre.isBlank() ? null : nombre;
    }

    private Ejercicio buscar(Long idEjercicio) {
        return ejercicios.porId(idEjercicio).orElseThrow(() -> new NoEncontrado("El ejercicio no existe."));
    }

    private DetalleDeEjercicio detalleDe(Long idEjercicio) {
        return new DetalleDeEjercicio(buscar(idEjercicio), intentos.usoDe(idEjercicio),
                intentos.opcionesUsadas(idEjercicio));
    }
}
