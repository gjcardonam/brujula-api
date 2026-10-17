package co.edu.udea.brujula.infraestructura.salida.persistencia;

import co.edu.udea.brujula.dominio.modelo.Competencia;
import co.edu.udea.brujula.dominio.modelo.Componente;
import co.edu.udea.brujula.dominio.modelo.DuracionSimulacro;
import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.modelo.Intento;
import co.edu.udea.brujula.dominio.modelo.NivelDificultad;
import co.edu.udea.brujula.dominio.modelo.Opcion;
import co.edu.udea.brujula.dominio.modelo.Recomendacion;
import co.edu.udea.brujula.dominio.modelo.Rol;
import co.edu.udea.brujula.dominio.modelo.Simulacro;
import co.edu.udea.brujula.dominio.modelo.TokenRecuperacion;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.CompetenciaEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.ComponenteEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.DuracionSimulacroEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.EjercicioEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.IntentoEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.NivelDificultadEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.OpcionEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.RecomendacionEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.RolEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.SimulacroEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.TokenRecuperacionEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.UsuarioEntidad;

import java.util.List;

public final class Mapeador {

    private Mapeador() {
    }

    public static Rol aDominio(RolEntidad entidad) {
        return entidad == null ? null : new Rol(entidad.getId(), entidad.getNombre());
    }

    public static Componente aDominio(ComponenteEntidad entidad) {
        return entidad == null ? null : new Componente(entidad.getId(), entidad.getNombre(), entidad.getEstado());
    }

    public static Competencia aDominio(CompetenciaEntidad entidad) {
        return entidad == null ? null : new Competencia(entidad.getId(), entidad.getNombre(), entidad.getEstado());
    }

    public static NivelDificultad aDominio(NivelDificultadEntidad entidad) {
        return entidad == null ? null : new NivelDificultad(entidad.getId(), entidad.getNivel());
    }

    public static Usuario aDominio(UsuarioEntidad entidad) {
        if (entidad == null) return null;
        return Usuario.reconstruir(entidad.getId(), entidad.getNombre(), entidad.getApellido(), entidad.getEmail(),
                entidad.getGoogleSub(), entidad.getPasswordHash(), entidad.getPasswordActualizadoEn(),
                entidad.isAceptoTerminos(), entidad.getTerminosAceptadosEn(), entidad.getCreadoEn(),
                entidad.getIntentosFallidos(), entidad.getBloqueadoHasta(), entidad.getUltimoLoginEn(),
                entidad.getEstado(), aDominio(entidad.getRol()));
    }

    public static Opcion aDominio(OpcionEntidad entidad) {
        return new Opcion(entidad.getId(), entidad.getTexto(), entidad.getImagen(), entidad.isCorrecta(),
                entidad.getRetroalimentacion(), entidad.getOrden());
    }

    public static Ejercicio aDominioSinOpciones(EjercicioEntidad entidad) {
        return new Ejercicio(entidad.getId(), entidad.getNumero(), entidad.getEnunciado(), entidad.getImagen(),
                aDominio(entidad.getNivel()), aDominio(entidad.getComponente()), aDominio(entidad.getCompetencia()),
                entidad.getEstado(), entidad.getCreadoEn(), null, null, List.of());
    }

    public static Ejercicio aDominio(EjercicioEntidad entidad) {
        List<Opcion> opciones = entidad.getOpciones().stream().map(Mapeador::aDominio).toList();
        UsuarioEntidad creador = entidad.getCreador();
        return new Ejercicio(entidad.getId(), entidad.getNumero(), entidad.getEnunciado(), entidad.getImagen(),
                aDominio(entidad.getNivel()), aDominio(entidad.getComponente()), aDominio(entidad.getCompetencia()),
                entidad.getEstado(), entidad.getCreadoEn(),
                creador == null ? null : creador.getId(),
                creador == null ? null : creador.getNombre() + " " + creador.getApellido(),
                opciones);
    }

    public static Intento aDominio(IntentoEntidad entidad) {
        return intento(entidad, aDominioSinOpciones(entidad.getEjercicio()));
    }

    public static Intento aDominioConOpciones(IntentoEntidad entidad) {
        EjercicioEntidad ejercicio = entidad.getEjercicio();
        List<Opcion> opciones = ejercicio.getOpciones().stream().map(Mapeador::aDominio).toList();
        return intento(entidad, new Ejercicio(ejercicio.getId(), ejercicio.getNumero(), ejercicio.getEnunciado(),
                ejercicio.getImagen(), aDominio(ejercicio.getNivel()), aDominio(ejercicio.getComponente()),
                aDominio(ejercicio.getCompetencia()), ejercicio.getEstado(), ejercicio.getCreadoEn(), null, null,
                opciones));
    }

    private static Intento intento(IntentoEntidad entidad, Ejercicio ejercicio) {
        return new Intento(entidad.getId(), entidad.getRespondidoEn(), entidad.isCorrecto(),
                entidad.getNivelConfianza(), entidad.getIdUsuario(), ejercicio,
                aDominio(entidad.getOpcionSeleccionada()), entidad.getTokenIdempotencia(), entidad.getIdSimulacro());
    }

    public static DuracionSimulacro aDominio(DuracionSimulacroEntidad entidad) {
        return new DuracionSimulacro(entidad.getId(), entidad.getMinutos());
    }

    public static Simulacro aDominio(SimulacroEntidad entidad) {
        return new Simulacro(entidad.getId(), entidad.getIdUsuario(), aDominio(entidad.getDuracion()),
                entidad.getInicio(), entidad.getFin(), entidad.getEstado(), entidad.getTiempoUtilizadoSeg());
    }

    public static Recomendacion aDominio(RecomendacionEntidad entidad) {
        ComponenteEntidad componente = entidad.getComponente();
        CompetenciaEntidad competencia = entidad.getCompetencia();
        return componente != null
                ? new Recomendacion(entidad.getId(), entidad.getOrden(), componente.getNombre(),
                        Recomendacion.COMPONENTE, componente.getId(), null, entidad.getPorcentaje(),
                        entidad.getMensaje())
                : new Recomendacion(entidad.getId(), entidad.getOrden(), competencia.getNombre(),
                        Recomendacion.COMPETENCIA, null, competencia.getId(), entidad.getPorcentaje(),
                        entidad.getMensaje());
    }

    public static TokenRecuperacion aDominio(TokenRecuperacionEntidad entidad) {
        return new TokenRecuperacion(entidad.getId(), entidad.getIdUsuario(), entidad.getTokenHash(),
                entidad.getCreadoEn(), entidad.getExpiraEn(), entidad.getUsadoEn());
    }
}
