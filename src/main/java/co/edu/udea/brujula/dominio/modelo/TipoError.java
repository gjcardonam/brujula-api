package co.edu.udea.brujula.dominio.modelo;

public record TipoError(Long id, String nombre) {
    public static final String COGNITIVO = "Error cognitivo";
    public static final String HABITO = "Error de hábito";
    public static final String ANSIEDAD = "Error de ansiedad";
}
