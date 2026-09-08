package co.edu.udea.brujula.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface TokenRecuperacionRepository extends JpaRepository<TokenRecuperacion, Long> {
    Optional<TokenRecuperacion> findByTokenHash(String tokenHash);
    long countByUsuario_IdAndCreadoEnAfter(Long idUsuario, Instant desde);
}
