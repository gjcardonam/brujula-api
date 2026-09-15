package co.edu.udea.brujula.aplicacion.acceso;

import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.modelo.TokenRecuperacion;
import co.edu.udea.brujula.dominio.puerto.entrada.VerificarEnlaceDeRecuperacion;
import co.edu.udea.brujula.dominio.puerto.salida.CifradorDeContrasenas;
import co.edu.udea.brujula.dominio.puerto.salida.Reloj;
import co.edu.udea.brujula.dominio.puerto.salida.TokenRecuperacionRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerificacionDelEnlaceDeRecuperacion implements VerificarEnlaceDeRecuperacion {

    private final TokenRecuperacionRepositorio tokensDeRecuperacion;
    private final CifradorDeContrasenas cifrador;
    private final Reloj reloj;

    public VerificacionDelEnlaceDeRecuperacion(TokenRecuperacionRepositorio tokensDeRecuperacion,
                                               CifradorDeContrasenas cifrador, Reloj reloj) {
        this.tokensDeRecuperacion = tokensDeRecuperacion;
        this.cifrador = cifrador;
        this.reloj = reloj;
    }

    @Override
    @Transactional(readOnly = true)
    public void verificar(String token) {
        if (token == null || token.isBlank()) {
            throw new DatosInvalidos("ENLACE_INVALIDO", TokenRecuperacion.ENLACE_INVALIDO);
        }
        tokensDeRecuperacion.porHash(cifrador.resumen(token))
                .filter(guardado -> guardado.vigente(reloj.ahora()))
                .orElseThrow(() -> new DatosInvalidos("ENLACE_INVALIDO", TokenRecuperacion.ENLACE_INVALIDO));
    }
}
