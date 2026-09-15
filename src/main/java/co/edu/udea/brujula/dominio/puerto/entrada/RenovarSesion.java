package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.SesionIniciada;

import java.time.Instant;

public interface RenovarSesion {

    SesionIniciada renovar(Long idUsuario, String jti, Instant expiraEn);
}
