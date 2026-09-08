package co.edu.udea.brujula.infraestructura.salida.persistencia;

import co.edu.udea.brujula.dominio.modelo.*;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.*;

import java.util.List;

/** Traduce entre las entidades de JPA y los modelos del dominio. */
public final class Mapeador {

    private Mapeador() {
    }

    public static Rol aDominio(RolEntidad e) {
        return e == null ? null : new Rol(e.getId(), e.getNombre());
    }

    public static Componente aDominio(ComponenteEntidad e) {
        return e == null ? null : new Componente(e.getId(), e.getNombre(), e.getEstado());
    }

    public static Competencia aDominio(CompetenciaEntidad e) {
        return e == null ? null : new Competencia(e.getId(), e.getNombre(), e.getEstado());
    }

    public static NivelDificultad aDominio(NivelDificultadEntidad e) {
        return e == null ? null : new NivelDificultad(e.getId(), e.getNivel());
    }

    public static TipoError aDominio(TipoErrorEntidad e) {
        return e == null ? null : new TipoError(e.getId(), e.getNombre());
    }

    public static DuracionSimulacro aDominio(DuracionSimulacroEntidad e) {
        return e == null ? null : new DuracionSimulacro(e.getId(), e.getMinutos());
    }

    public static Usuario aDominio(UsuarioEntidad e) {
        return e == null ? null : Usuario.reconstruir(e.getId(), e.getNombre(), e.getApellido(), e.getEmail(),
                e.getGoogleSub(), e.getPasswordHash(), e.isAceptoTerminos(), e.getFechaAceptacionTerminos(),
                e.getCreadoEn(), e.getIntentosFallidos(), e.getUltimoLogin(), e.getFechaBloqueo(), e.getEstado(),
                aDominio(e.getRol()), e.getPasswordActualizadoEn());
    }

    public static Opcion aDominio(OpcionEntidad e) {
        return new Opcion(e.getId(), e.getTexto(), e.getImagen(), e.isCorrecta(), e.getRetroalimentacion(),
                aDominio(e.getTipoError()), e.getOrden());
    }

    /** Sin opciones: sirve para el banco y para el historial, donde no hace falta el detalle. */
    public static Ejercicio aDominioSinOpciones(EjercicioEntidad e) {
        return new Ejercicio(e.getId(), e.getNumero(), e.getEnunciado(), e.getImagen(), aDominio(e.getNivel()),
                aDominio(e.getComponente()), aDominio(e.getCompetencia()), e.getEstado(), e.getCreadoEn(),
                null, null, List.of());
    }

    public static Ejercicio aDominio(EjercicioEntidad e) {
        List<Opcion> opciones = e.getOpciones().stream().map(Mapeador::aDominio).toList();
        UsuarioEntidad creador = e.getCreador();
        return new Ejercicio(e.getId(), e.getNumero(), e.getEnunciado(), e.getImagen(), aDominio(e.getNivel()),
                aDominio(e.getComponente()), aDominio(e.getCompetencia()), e.getEstado(), e.getCreadoEn(),
                creador == null ? null : creador.getId(),
                creador == null ? null : creador.getNombre() + " " + creador.getApellido(),
                opciones);
    }

    public static Intento aDominio(IntentoEntidad e) {
        return new Intento(e.getId(), e.getFechaHora(), e.isCorrecto(), e.getNivelConfianza(),
                aDominio(e.getTipoError()), e.getIdUsuario(), aDominioSinOpciones(e.getEjercicio()),
                aDominio(e.getOpcionSeleccionada()), e.getIdSimulacro(), e.getTokenIdempotencia());
    }

    public static Simulacro aDominio(SimulacroEntidad e) {
        return new Simulacro(e.getId(), e.getIdUsuario(), aDominio(e.getDuracion()), e.getInicio(), e.getFin(),
                e.getEstado(), e.getTiempoUtilizadoSeg());
    }

    public static Recomendacion aDominio(RecomendacionEntidad e) {
        boolean esComponente = e.getComponente() != null;
        return new Recomendacion(e.getId(), e.getOrden(),
                esComponente ? e.getComponente().getNombre() : e.getCompetencia().getNombre(),
                esComponente ? Recomendacion.COMPONENTE : Recomendacion.COMPETENCIA,
                esComponente ? e.getComponente().getId() : null,
                esComponente ? null : e.getCompetencia().getId(),
                e.getPorcentaje(), e.getMensaje());
    }

    public static TokenRecuperacion aDominio(TokenRecuperacionEntidad e) {
        return new TokenRecuperacion(e.getId(), e.getIdUsuario(), e.getTokenHash(), e.getCreadoEn(),
                e.getExpiraEn(), e.getUsadoEn());
    }
}
