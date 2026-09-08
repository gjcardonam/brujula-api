package co.edu.udea.brujula.infraestructura.salida.persistencia.adaptador;

import co.edu.udea.brujula.dominio.modelo.Auditoria;
import co.edu.udea.brujula.dominio.puerto.salida.AuditoriaRepositorio;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.AuditoriaEjercicioEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.AuditoriaEjercicioJpa;
import org.springframework.stereotype.Component;

@Component
public class AuditoriaAdaptador implements AuditoriaRepositorio {

    private final AuditoriaEjercicioJpa auditoria;

    public AuditoriaAdaptador(AuditoriaEjercicioJpa auditoria) {
        this.auditoria = auditoria;
    }

    @Override
    public void registrar(Auditoria registro) {
        AuditoriaEjercicioEntidad entidad = new AuditoriaEjercicioEntidad();
        entidad.setAccion(registro.accion());
        entidad.setFecha(registro.fecha());
        entidad.setIdEjercicio(registro.idEjercicio());
        entidad.setIdActor(registro.idActor());
        auditoria.save(entidad);
    }
}
