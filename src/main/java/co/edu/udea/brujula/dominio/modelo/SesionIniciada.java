package co.edu.udea.brujula.dominio.modelo;

import java.time.Instant;

public record SesionIniciada(String token, Instant expiraEn, Usuario usuario) {
}
