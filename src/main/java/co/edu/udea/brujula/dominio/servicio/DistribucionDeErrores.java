package co.edu.udea.brujula.dominio.servicio;
import co.edu.udea.brujula.dominio.modelo.TipoError;
import co.edu.udea.brujula.dominio.modelo.consulta.*;
import java.util.List;
import java.math.BigDecimal;
public final class DistribucionDeErrores {
    private DistribucionDeErrores() {}
    public static ErroresDelEstudiante calcular(List<ErrorRegistrado> registros, Long componente) {
        var filtrados = registros.stream().filter(r -> componente == null || componente.equals(r.idComponente())).toList();
        long clasificados = filtrados.stream().filter(r -> r.tipo() != null).count();
        var distribucion = List.of(TipoError.COGNITIVO, TipoError.HABITO, TipoError.ANSIEDAD).stream()
            .map(tipo -> { long cantidad = filtrados.stream().filter(r -> tipo.equals(r.tipo())).count();
                return new DistribucionDeError(tipo, cantidad, Porcentajes.de(cantidad, clasificados)); }).toList();
        String predominante = distribucion.stream().filter(d -> d.porcentaje().compareTo(BigDecimal.valueOf(50)) > 0)
            .map(DistribucionDeError::tipo).findFirst().orElse(null);
        var componentes = registros.stream().map(r -> new ErroresDelEstudiante.Componente(r.idComponente(), r.componente()))
            .distinct().sorted(java.util.Comparator.comparing(ErroresDelEstudiante.Componente::nombre)).toList();
        return new ErroresDelEstudiante(filtrados.size(), filtrados.size() - clasificados, distribucion,
            predominante, orientacion(predominante), componente, componentes);
    }
    private static String orientacion(String tipo) {
        if (TipoError.COGNITIVO.equals(tipo)) return "Repasa los conceptos del área y revisa la retroalimentación antes de volver a practicar.";
        if (TipoError.HABITO.equals(tipo)) return "Lee con cuidado el enunciado y verifica tu respuesta antes de enviarla.";
        if (TipoError.ANSIEDAD.equals(tipo)) return "Haz una pausa, respira y revisa tu razonamiento incluso cuando te sientas muy seguro.";
        return null;
    }
}
