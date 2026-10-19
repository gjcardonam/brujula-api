package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeEjercicio;

public interface ConsultarDetalleDeEjercicio {

    DetalleDeEjercicio detalle(Long idEjercicio);
}
