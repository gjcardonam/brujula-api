package co.edu.udea.brujula.aplicacion.administracion;

import co.edu.udea.brujula.dominio.excepcion.Conflicto;
import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.modelo.Competencia;
import co.edu.udea.brujula.dominio.modelo.Componente;
import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.modelo.NivelDificultad;
import co.edu.udea.brujula.dominio.modelo.Opcion;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.puerto.entrada.CrearEjercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.comando.DatosDeEjercicio;
import co.edu.udea.brujula.dominio.puerto.salida.AlmacenDeImagenes;
import co.edu.udea.brujula.dominio.puerto.salida.CatalogoRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.EjercicioRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.Reloj;
import co.edu.udea.brujula.dominio.puerto.salida.UsuarioRepositorio;
import co.edu.udea.brujula.dominio.servicio.PoliticaDelEjercicio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CreacionDeEjercicio implements CrearEjercicio {

    private final EjercicioRepositorio ejercicios;
    private final CatalogoRepositorio catalogos;
    private final UsuarioRepositorio usuarios;
    private final AlmacenDeImagenes imagenes;
    private final Reloj reloj;

    public CreacionDeEjercicio(EjercicioRepositorio ejercicios, CatalogoRepositorio catalogos,
                               UsuarioRepositorio usuarios, AlmacenDeImagenes imagenes, Reloj reloj) {
        this.ejercicios = ejercicios;
        this.catalogos = catalogos;
        this.usuarios = usuarios;
        this.imagenes = imagenes;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public Ejercicio crear(Long idAdministrador, DatosDeEjercicio datos) {
        Usuario creador = usuarios.porId(idAdministrador)
                .orElseThrow(() -> new NoEncontrado("La cuenta no existe."));
        List<String> errores = new ArrayList<>(PoliticaDelEjercicio.revisar(datos));
        errores.addAll(revisarImagenes(datos));
        if (!errores.isEmpty()) throw new DatosInvalidos(errores);

        String enunciado = datos.enunciado().trim();
        if (ejercicios.existeConEnunciado(enunciado)) {
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

        return ejercicios.guardar(Ejercicio.nuevo(enunciado, nombreDeImagen(datos.imagen()), componente, competencia,
                nivel, armarOpciones(datos), creador.id(), creador.nombreCompleto(), reloj.ahora()));
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

    private List<Opcion> armarOpciones(DatosDeEjercicio datos) {
        List<Opcion> opciones = new ArrayList<>();
        for (int posicion = 0; posicion < datos.opciones().size(); posicion++) {
            DatosDeEjercicio.DatosDeOpcion opcion = datos.opciones().get(posicion);
            opciones.add(new Opcion(null, sinEspacios(opcion.texto()), nombreDeImagen(opcion.imagen()),
                    opcion.correcta(), opcion.retroalimentacion().trim(), posicion + 1));
        }
        return opciones;
    }

    private String nombreDeImagen(String valor) {
        String nombre = imagenes.nombreDe(valor == null ? "" : valor.trim());
        return nombre.isBlank() ? null : nombre;
    }

    private static String sinEspacios(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
