package co.edu.udea.brujula.infraestructura.salida.persistencia.adaptador;

import co.edu.udea.brujula.dominio.puerto.salida.ParametrosDelSistema;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.ParametroSistemaJpa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ParametrosAdaptador implements ParametrosDelSistema {

    private static final Logger log = LoggerFactory.getLogger(ParametrosAdaptador.class);

    private final ParametroSistemaJpa parametros;

    public ParametrosAdaptador(ParametroSistemaJpa parametros) {
        this.parametros = parametros;
    }

    @Override
    public int entero(String clave, int valorPorDefecto) {
        return parametros.findById(clave).map(p -> {
            try {
                return Integer.parseInt(p.getValor().trim());
            } catch (NumberFormatException e) {
                log.warn("El parámetro {} tiene el valor '{}', que no es un número; se usa {}",
                        clave, p.getValor(), valorPorDefecto);
                return valorPorDefecto;
            }
        }).orElse(valorPorDefecto);
    }
}
