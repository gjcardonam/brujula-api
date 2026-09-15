package co.edu.udea.brujula.aplicacion.banco;

import co.edu.udea.brujula.dominio.modelo.consulta.ComponentesDelBanco;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarComponentesDelBanco;
import co.edu.udea.brujula.dominio.puerto.salida.EjercicioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultaDeComponentesDelBanco implements ConsultarComponentesDelBanco {

    private final EjercicioRepositorio ejercicios;

    public ConsultaDeComponentesDelBanco(EjercicioRepositorio ejercicios) {
        this.ejercicios = ejercicios;
    }

    @Override
    @Transactional(readOnly = true)
    public ComponentesDelBanco componentes(boolean esAdministrador) {
        return ejercicios.conteoPorComponente(!esAdministrador);
    }
}
