package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.SesionIniciada;

public interface IniciarSesion {

    SesionIniciada iniciar(String email, String password);
}
