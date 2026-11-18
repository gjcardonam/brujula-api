package co.edu.udea.brujula.aplicacion.banco;

import co.edu.udea.brujula.dominio.puerto.entrada.BuscarSiguienteEjercicio;
import co.edu.udea.brujula.dominio.puerto.salida.EjercicioRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.ParametrosDelSistema;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BusquedaDelSiguienteEjercicio implements BuscarSiguienteEjercicio {

    private static final String SIN_EJERCICIOS = "No hay más ejercicios disponibles por ahora.";
    private static final String SIN_EJERCICIOS_DEL_COMPONENTE =
            "No hay más ejercicios disponibles para este componente. Puedes volver al banco o cambiar el filtro.";

    private final EjercicioRepositorio ejercicios;
    private final ParametrosDelSistema parametros;

    public BusquedaDelSiguienteEjercicio(EjercicioRepositorio ejercicios, ParametrosDelSistema parametros) {
        this.ejercicios = ejercicios;
        this.parametros = parametros;
    }

    @Override
    @Transactional(readOnly = true)
    public Siguiente buscar(Long idEstudiante, Long idEjercicioActual, Long idComponente) {
        Optional<Long> siguiente = ejercicios.siguienteParaPractica(idEjercicioActual, idComponente, idEstudiante,
                parametros.entero(ParametrosDelSistema.MAX_INTENTOS_PRACTICA, 2),
                parametros.entero(ParametrosDelSistema.HORAS_ESPERA_REINTENTO, 24));
        if (siguiente.isPresent()) {
            return new Siguiente(true, siguiente.get(), null);
        }
        return new Siguiente(false, null, idComponente == null ? SIN_EJERCICIOS : SIN_EJERCICIOS_DEL_COMPONENTE);
    }
}
