package co.edu.udea.brujula.usuario;

import co.edu.udea.brujula.catalogo.RolRepository;
import co.edu.udea.brujula.config.BrujulaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Crea el primer administrador si no existe ninguno. En PI1 no hay pantalla para asignar el rol
 * (HU-036 es de PI II), así que el primer administrador nace de la configuración.
 */
@Component
@Order(1)
public class AdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final PasswordEncoder encoder;
    private final BrujulaProperties props;

    public AdminSeeder(UsuarioRepository usuarios, RolRepository roles, PasswordEncoder encoder, BrujulaProperties props) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.encoder = encoder;
        this.props = props;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (usuarios.existsByRol_NombreRol("Administrador")) return;
        BrujulaProperties.Admin admin = props.admin();
        if (admin == null || admin.email() == null || admin.email().isBlank() || admin.password() == null || admin.password().isBlank()) {
            log.warn("No hay administrador y no se configuró brujula.admin.email / password: no se creó ninguno.");
            return;
        }
        Usuario u = new Usuario();
        u.setNombre("Administrador");
        u.setApellido("Brújula");
        u.setEmail(admin.email().trim().toLowerCase());
        u.setPasswordHash(encoder.encode(admin.password()));
        u.setAceptoTerminos(true);
        u.setFechaAceptacionTerminos(Instant.now());
        u.setCreadoEn(Instant.now());
        u.setEstado("Activo");
        u.setRol(roles.findByNombreRol("Administrador").orElseThrow());
        usuarios.save(u);
        log.info("Administrador inicial creado: {}", u.getEmail());
    }
}
