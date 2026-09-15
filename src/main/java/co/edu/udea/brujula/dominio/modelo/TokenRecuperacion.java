package co.edu.udea.brujula.dominio.modelo;

import java.time.Instant;

public record TokenRecuperacion(Long id, Long idUsuario, String hash, Instant creadoEn, Instant expiraEn,
                                Instant usadoEn) {

    public static final String ENLACE_INVALIDO = "Este enlace expiró o ya fue utilizado. Solicita uno nuevo.";

    public static TokenRecuperacion nuevo(Long idUsuario, String hash, Instant ahora, int minutosDeVigencia) {
        return new TokenRecuperacion(null, idUsuario, hash, ahora, ahora.plusSeconds(minutosDeVigencia * 60L), null);
    }

    public boolean vigente(Instant ahora) {
        return usadoEn == null && expiraEn.isAfter(ahora);
    }

    public TokenRecuperacion marcarUsado(Instant ahora) {
        return new TokenRecuperacion(id, idUsuario, hash, creadoEn, expiraEn, ahora);
    }
}
