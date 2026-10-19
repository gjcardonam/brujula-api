package co.edu.udea.brujula.apoyo.dobles;

import co.edu.udea.brujula.dominio.modelo.Auditoria;
import co.edu.udea.brujula.dominio.puerto.salida.AuditoriaRepositorio;

import java.util.ArrayList;
import java.util.List;

public class AuditoriaEnMemoria implements AuditoriaRepositorio {

    private final List<Auditoria> registros = new ArrayList<>();

    @Override
    public void registrar(Auditoria auditoria) {
        registros.add(auditoria);
    }

    public List<Auditoria> todos() {
        return registros;
    }
}
