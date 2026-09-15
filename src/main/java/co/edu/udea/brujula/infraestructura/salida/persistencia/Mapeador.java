package co.edu.udea.brujula.infraestructura.salida.persistencia;

import co.edu.udea.brujula.dominio.modelo.Competencia;
import co.edu.udea.brujula.dominio.modelo.Componente;
import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.modelo.Intento;
import co.edu.udea.brujula.dominio.modelo.NivelDificultad;
import co.edu.udea.brujula.dominio.modelo.Opcion;
import co.edu.udea.brujula.dominio.modelo.Rol;
import co.edu.udea.brujula.dominio.modelo.TokenRecuperacion;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.CompetenciaEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.ComponenteEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.EjercicioEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.IntentoEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.NivelDificultadEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.OpcionEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.RolEntidad;
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
        return new Intento(entidad.getId(), entidad.getRespondidoEn(), entidad.isCorrecto(),
                entidad.getNivelConfianza(), entidad.getIdUsuario(), aDominioSinOpciones(entidad.getEjercicio()),
                aDominio(entidad.getOpcionSeleccionada()), entidad.getTokenIdempotencia());
    }

    public static TokenRecuperacion aDominio(TokenRecuperacionEntidad entidad) {
        return new TokenRecuperacion(entidad.getId(), entidad.getIdUsuario(), entidad.getTokenHash(),
                entidad.getCreadoEn(), entidad.getExpiraEn(), entidad.getUsadoEn());
    }
}
