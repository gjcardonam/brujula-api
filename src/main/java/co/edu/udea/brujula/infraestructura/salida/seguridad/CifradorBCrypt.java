package co.edu.udea.brujula.infraestructura.salida.seguridad;

import co.edu.udea.brujula.dominio.puerto.salida.CifradorDeContrasenas;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class CifradorBCrypt implements CifradorDeContrasenas {

    private final PasswordEncoder codificador = new BCryptPasswordEncoder();

    @Override
    public String cifrar(String contrasenaPlana) {
        return codificador.encode(contrasenaPlana);
    }

    @Override
    public boolean coincide(String contrasenaPlana, String hash) {
        return codificador.matches(contrasenaPlana, hash);
    }

    @Override
    public String resumen(String valor) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(sha256.digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
