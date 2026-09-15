package co.edu.udea.brujula.apoyo.dobles;

import co.edu.udea.brujula.dominio.modelo.TokenRecuperacion;
import co.edu.udea.brujula.dominio.puerto.salida.TokenRecuperacionRepositorio;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TokensDeRecuperacionEnMemoria implements TokenRecuperacionRepositorio {

    private final List<TokenRecuperacion> guardados = new ArrayList<>();
    private long siguienteId = 1;

    public List<TokenRecuperacion> todos() {
        return guardados;
    }

    public TokenRecuperacion ultimo() {
        return guardados.get(guardados.size() - 1);
    }

    @Override
    public TokenRecuperacion guardar(TokenRecuperacion token) {
        guardados.removeIf(previo -> token.id() != null && token.id().equals(previo.id()));
        TokenRecuperacion conId = token.id() == null
                ? new TokenRecuperacion(siguienteId++, token.idUsuario(), token.hash(), token.creadoEn(),
                        token.expiraEn(), token.usadoEn())
                : token;
        guardados.add(conId);
        return conId;
    }

    @Override
    public Optional<TokenRecuperacion> porHash(String hash) {
        return guardados.stream().filter(token -> token.hash().equals(hash)).findFirst();
    }

    @Override
    public long solicitudesDesde(Long idUsuario, Instant desde) {
        return guardados.stream()
                .filter(token -> token.idUsuario().equals(idUsuario))
                .filter(token -> token.creadoEn().isAfter(desde))
                .count();
    }
}
