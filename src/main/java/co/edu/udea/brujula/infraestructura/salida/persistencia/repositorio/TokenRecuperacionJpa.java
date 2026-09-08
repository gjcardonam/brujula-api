package co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio;

import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.TokenRecuperacionEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface TokenRecuperacionJpa extends JpaRepository<TokenRecuperacionEntidad, Long> {
    Optional<TokenRecuperacionEntidad> findByTokenHash(String hash);

    long countByIdUsuarioAndCreadoEnAfter(Long idUsuario, Instant desde);
}
