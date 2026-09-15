package co.edu.udea.brujula.dominio.modelo;

public record Rol(Long id, String nombre) {

    public static final String ADMINISTRADOR = "Administrador";
    public static final String ESTUDIANTE = "Estudiante";

    public boolean esAdministrador() {
        return ADMINISTRADOR.equals(nombre);
    }
}
