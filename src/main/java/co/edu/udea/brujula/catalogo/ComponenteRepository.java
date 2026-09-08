package co.edu.udea.brujula.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComponenteRepository extends JpaRepository<Componente, Long> {
    List<Componente> findAllByOrderByIdAsc();
    List<Componente> findByEstadoOrderByIdAsc(String estado);
}
