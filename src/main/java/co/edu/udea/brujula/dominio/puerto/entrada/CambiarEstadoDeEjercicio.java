package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeEjercicio;

public interface CambiarEstadoDeEjercicio {

    DetalleDeEjercicio cambiarEstado(Long idAdministrador, Long idEjercicio, String nuevoEstado);
}
