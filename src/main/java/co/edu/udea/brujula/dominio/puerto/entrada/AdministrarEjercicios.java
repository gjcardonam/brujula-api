package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeEjercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.comando.DatosDeEjercicio;

/** HU-020 a HU-024: lo que puede hacer un administrador con el banco. */
public interface AdministrarEjercicios {

    DetalleDeEjercicio crear(Long idAdministrador, DatosDeEjercicio datos);

    DetalleDeEjercicio editar(Long idAdministrador, Long idEjercicio, DatosDeEjercicio datos);

    DetalleDeEjercicio consultarDetalle(Long idEjercicio);

    DetalleDeEjercicio cambiarEstado(Long idAdministrador, Long idEjercicio, String nuevoEstado);
}
