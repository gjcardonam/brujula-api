package co.edu.udea.brujula.dominio.modelo;

import java.time.Instant;

public record SesionRevocada(String jti, Long idUsuario, Instant revocadaEn, Instant expiraEn) {
}
