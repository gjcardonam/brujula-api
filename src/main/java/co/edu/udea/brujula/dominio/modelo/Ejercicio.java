package co.edu.udea.brujula.dominio.modelo;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public record Ejercicio(Long id, Integer numero, String enunciado, String imagen, NivelDificultad nivel,
                        Componente componente, Competencia competencia, String estado, Instant creadoEn,
                        Long idCreador, String nombreCreador, List<Opcion> opciones) {

    public static final String ACTIVO = "Activo";

    public Ejercicio {
        opciones = opciones == null ? List.of() : List.copyOf(opciones);
    }

    public static Ejercicio nuevo(String enunciado, String imagen, Componente componente, Competencia competencia,
                                  NivelDificultad nivel, List<Opcion> opciones, Long idCreador,
                                  String nombreCreador, Instant ahora) {
        return new Ejercicio(null, null, enunciado, imagen, nivel, componente, competencia, ACTIVO, ahora,
                idCreador, nombreCreador, opciones);
    }

    public boolean estaActivo() {
        return ACTIVO.equals(estado);
    }

    public Optional<Opcion> opcionCorrecta() {
        return opciones.stream().filter(Opcion::correcta).findFirst();
    }

    public Optional<Opcion> opcion(Long idOpcion) {
        return opciones.stream().filter(o -> o.id().equals(idOpcion)).findFirst();
    }
}
