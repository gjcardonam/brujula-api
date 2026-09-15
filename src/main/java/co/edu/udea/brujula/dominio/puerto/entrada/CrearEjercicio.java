package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.comando.DatosDeEjercicio;

public interface CrearEjercicio {

    Ejercicio crear(Long idAdministrador, DatosDeEjercicio datos);
}
