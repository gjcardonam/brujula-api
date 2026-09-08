package co.edu.udea.brujula.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NivelDificultadRepository extends JpaRepository<NivelDificultad, Long> {
    List<NivelDificultad> findAllByOrderByIdAsc();
}
