package co.edu.udea.brujula.dominio.puerto.entrada;

import java.time.Instant;

public interface CerrarSesion {

    void cerrar(Long idUsuario, String jti, Instant expiraEn);
}
