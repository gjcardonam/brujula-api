package co.edu.udea.brujula.dominio.modelo;

public record Componente(Long id, String nombre, String estado) {

    public boolean estaActivo() {
        return "Activo".equals(estado);
    }
}
