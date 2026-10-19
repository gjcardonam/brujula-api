package co.edu.udea.brujula.aplicacion.practica;
import co.edu.udea.brujula.dominio.modelo.TipoError;
import co.edu.udea.brujula.dominio.modelo.consulta.ErrorRegistrado;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class ConsultaDeErroresDelEstudianteTest {
    @Test void consultaSoloLosErroresDelEstudianteSolicitado() {
        var consulta = new ConsultaDeErroresDelEstudiante(id -> id.equals(7L)
            ? List.of(new ErrorRegistrado(1L, "Matemáticas", TipoError.COGNITIVO)) : List.of());
        assertEquals(1, consulta.consultar(7L, null).totalIncorrectos());
        assertEquals(0, consulta.consultar(8L, null).totalIncorrectos());
        assertEquals(0, consulta.consultar(7L, 2L).totalIncorrectos());
    }
}
