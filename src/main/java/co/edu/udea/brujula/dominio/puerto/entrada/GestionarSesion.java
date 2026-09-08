package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.SesionIniciada;

import java.time.Instant;

/** HU-004: cerrar sesión y renovarla mientras el usuario siga activo. */
public interface GestionarSesion {

    void cerrar(Long idUsuario, String jti, Instant expiraEn);

    SesionIniciada renovar(Long idUsuario, String jti, Instant expiraEn);

    /** Limpieza periódica de los tokens revocados que ya vencieron. */
    int limpiarRevocadasVencidas();
}
