package co.edu.udea.brujula.infraestructura.configuracion;

import co.edu.udea.brujula.dominio.puerto.salida.IntentoRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.ParametrosDelSistema;
import co.edu.udea.brujula.dominio.puerto.salida.SimulacroRepositorio;
import co.edu.udea.brujula.dominio.servicio.CierreDeSimulacros;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiciosDeDominio {

    @Bean
    public CierreDeSimulacros cierreDeSimulacros(SimulacroRepositorio simulacros, IntentoRepositorio intentos,
                                                 ParametrosDelSistema parametros) {
        return new CierreDeSimulacros(simulacros, intentos, parametros);
    }
}
