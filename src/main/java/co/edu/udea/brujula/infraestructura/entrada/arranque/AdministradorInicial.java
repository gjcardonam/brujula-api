package co.edu.udea.brujula.infraestructura.entrada.arranque;

import co.edu.udea.brujula.dominio.modelo.Rol;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.puerto.salida.CatalogoRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.CifradorDeContrasenas;
import co.edu.udea.brujula.dominio.puerto.salida.Reloj;
import co.edu.udea.brujula.dominio.puerto.salida.UsuarioRepositorio;
import co.edu.udea.brujula.infraestructura.configuracion.BrujulaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class AdministradorInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdministradorInicial.class);

    private final UsuarioRepositorio usuarios;
    private final CatalogoRepositorio catalogos;
    private final CifradorDeContrasenas cifrador;
    private final BrujulaProperties propiedades;
    private final Reloj reloj;

    public AdministradorInicial(UsuarioRepositorio usuarios, CatalogoRepositorio catalogos,
                                CifradorDeContrasenas cifrador, BrujulaProperties propiedades, Reloj reloj) {
        this.usuarios = usuarios;
        this.catalogos = catalogos;
        this.cifrador = cifrador;
        this.propiedades = propiedades;
        this.reloj = reloj;
    }

    @Override
    public void run(ApplicationArguments argumentos) {
        if (usuarios.existeAlgunAdministrador()) return;

        BrujulaProperties.Admin configurado = propiedades.admin();
        if (configurado == null || configurado.email() == null || configurado.email().isBlank()
                || configurado.password() == null || configurado.password().isBlank()) {
            log.warn("No hay administrador y no se configuró brujula.admin.email / password: no se creó ninguno.");
            return;
        }
        Rol rol = catalogos.rol(Rol.ADMINISTRADOR).orElseThrow();
        Usuario administrador = Usuario.crear("Administrador", "Brújula", configurado.email().trim().toLowerCase(),
                null, cifrador.cifrar(configurado.password()), rol, reloj.ahora());
        usuarios.guardar(administrador);
        log.info("Administrador inicial creado: {}", administrador.email());
    }
}
