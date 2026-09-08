package co.edu.udea.brujula.config;

/** Identidad mínima del usuario autenticado que viaja en el SecurityContext. */
public record UsuarioPrincipal(Long id, String email, String rol, String jti) {
    public boolean esAdministrador() {
        return "Administrador".equals(rol);
    }
}
