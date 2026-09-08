package co.edu.udea.brujula.infraestructura.salida.persistencia.adaptador;

import co.edu.udea.brujula.dominio.modelo.TokenRecuperacion;
import co.edu.udea.brujula.dominio.puerto.salida.TokenRecuperacionRepositorio;
import co.edu.udea.brujula.infraestructura.salida.persistencia.Mapeador;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.TokenRecuperacionEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.TokenRecuperacionJpa;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class TokenRecuperacionAdaptador implements TokenRecuperacionRepositorio {

    private final TokenRecuperacionJpa tokens;

    public TokenRecuperacionAdaptador(TokenRecuperacionJpa tokens) {
        this.tokens = tokens;
    }

    @Override
    public TokenRecuperacion guardar(TokenRecuperacion token) {
        TokenRecuperacionEntidad entidad = token.id() == null
                ? new TokenRecuperacionEntidad()
                : tokens.findById(token.id()).orElseGet(TokenRecuperacionEntidad::new);
        entidad.setIdUsuario(token.idUsuario());
        entidad.setTokenHash(token.hash());
        entidad.setCreadoEn(token.creadoEn());
        entidad.setExpiraEn(token.expiraEn());
        entidad.setUsadoEn(token.usadoEn());
        return Mapeador.aDominio(tokens.save(entidad));
    }

    @Override
    public Optional<TokenRecuperacion> porHash(String hash) {
        return tokens.findByTokenHash(hash).map(Mapeador::aDominio);
    }

    @Override
    public long solicitudesDesde(Long idUsuario, Instant desde) {
        return tokens.countByIdUsuarioAndCreadoEnAfter(idUsuario, desde);
    }
}
