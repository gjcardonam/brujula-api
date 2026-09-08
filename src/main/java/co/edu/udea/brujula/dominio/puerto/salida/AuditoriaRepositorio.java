package co.edu.udea.brujula.dominio.puerto.salida;

import co.edu.udea.brujula.dominio.modelo.Auditoria;

public interface AuditoriaRepositorio {
    void registrar(Auditoria auditoria);
}
