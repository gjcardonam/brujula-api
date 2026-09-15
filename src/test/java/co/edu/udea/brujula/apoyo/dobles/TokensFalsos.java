package co.edu.udea.brujula.apoyo.dobles;

import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.puerto.salida.ProveedorDeTokens;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TokensFalsos implements ProveedorDeTokens {

    private final Map<String, SesionLeida> sesiones = new HashMap<>();
    private final Map<String, RegistroPendiente> registros = new HashMap<>();
    private final RelojFijo reloj;
    private int contador;

    public TokensFalsos(RelojFijo reloj) {
        this.reloj = reloj;
    }

    @Override
    public Sesion emitirSesion(Usuario usuario) {
        String jti = "jti-" + (++contador);
        Instant ahora = reloj.ahora();
        Instant expira = ahora.plus(Duration.ofHours(2));
        sesiones.put("token-" + jti, new SesionLeida(jti, usuario.id(), ahora, expira));
        return new Sesion("token-" + jti, jti, expira);
    }

    @Override
    public Optional<SesionLeida> leerSesion(String token) {
        return Optional.ofNullable(sesiones.get(token));
    }

    @Override
    public String emitirRegistro(RegistroPendiente registro) {
        String token = "registro-" + registro.email();
        registros.put(token, registro);
        return token;
    }

    @Override
    public Optional<RegistroPendiente> leerRegistro(String token) {
        return Optional.ofNullable(registros.get(token));
    }
}
