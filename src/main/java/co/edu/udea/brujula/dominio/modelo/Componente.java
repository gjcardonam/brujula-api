package co.edu.udea.brujula.dominio.modelo;

/** Categoría de contenido de la prueba: Estadística, Geometría, o Álgebra y cálculo. */
public record Componente(Long id, String nombre, String estado) {
    public boolean estaActivo() {
        return "Activo".equals(estado);
    }
}
