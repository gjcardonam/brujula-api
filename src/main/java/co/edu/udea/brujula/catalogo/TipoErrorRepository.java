package co.edu.udea.brujula.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TipoErrorRepository extends JpaRepository<TipoError, Long> {
    Optional<TipoError> findByNombre(String nombre);
    List<TipoError> findAllByOrderByIdAsc();
}
