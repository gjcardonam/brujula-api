package co.edu.udea.brujula.dominio.modelo;

import java.time.Instant;

/** Token de sesión invalidado al cerrar sesión (HU-004 CA-06). */
public record SesionRevocada(String jti, Long idUsuario, Instant revocadaEn, Instant expiraEn) {
}
