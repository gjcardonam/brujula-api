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
import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.puerto.entrada.CrearEjercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.comando.DatosDeEjercicio;
import co.edu.udea.brujula.dominio.puerto.salida.AlmacenDeImagenes;
import co.edu.udea.brujula.dominio.puerto.salida.AuditoriaRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.CatalogoRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.EjercicioRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.Reloj;
import co.edu.udea.brujula.dominio.puerto.salida.UsuarioRepositorio;
import co.edu.udea.brujula.dominio.servicio.PoliticaDelEjercicio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class CreacionDeEjercicio implements CrearEjercicio {

    private final EjercicioRepositorio ejercicios;
    private final CatalogoRepositorio catalogos;
    private final UsuarioRepositorio usuarios;
    private final AuditoriaRepositorio auditoria;
    private final AlmacenDeImagenes imagenes;
    private final Reloj reloj;

    public CreacionDeEjercicio(EjercicioRepositorio ejercicios, CatalogoRepositorio catalogos,
                               UsuarioRepositorio usuarios, AuditoriaRepositorio auditoria,
                               AlmacenDeImagenes imagenes, Reloj reloj) {
        this.ejercicios = ejercicios;
        this.catalogos = catalogos;
        this.usuarios = usuarios;
        this.auditoria = auditoria;
        this.imagenes = imagenes;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public Ejercicio crear(Long idAdministrador, DatosDeEjercicio datos) {
        Usuario creador = usuarios.porId(idAdministrador)
                .orElseThrow(() -> new NoEncontrado("El usuario no existe."));
        List<String> errores = new ArrayList<>(PoliticaDelEjercicio.revisar(datos));
        errores.addAll(revisarImagenes(datos));
        if (!errores.isEmpty()) throw new DatosInvalidos(errores);

        String enunciado = datos.enunciado().trim();
        if (ejercicios.existeOtroConEnunciado(enunciado, null)) {
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

        List<Opcion> opciones = PoliticaDelEjercicio.armarOpciones(datos.opciones(), Set.of(), List.of(),
                this::nombreDeImagen, this::tipoDeError);
        Ejercicio guardado = ejercicios.guardar(Ejercicio.nuevo(enunciado, nombreDeImagen(datos.imagen()),
                nivel, componente, competencia, opciones, creador.id(), creador.nombreCompleto(), reloj.ahora()));
        auditoria.registrar(Auditoria.de(Auditoria.CREACION, guardado.id(), idAdministrador, reloj.ahora()));
        return guardado;
    }

    private TipoError tipoDeError(Long idTipoError) {
        return catalogos.tipoDeError(idTipoError)
                .orElseThrow(() -> new DatosInvalidos("TIPO_ERROR_INVALIDO", "El tipo de error no existe."));
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

    private String nombreDeImagen(String valor) {
        String nombre = imagenes.nombreDe(valor == null ? "" : valor.trim());
        return nombre.isBlank() ? null : nombre;
    }
}
