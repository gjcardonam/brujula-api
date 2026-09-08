package co.edu.udea.brujula.dominio.modelo;

public record Competencia(Long id, String nombre, String estado) {
    public boolean estaActiva() {
        return "Activo".equals(estado);
    }
}
