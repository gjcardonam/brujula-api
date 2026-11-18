package co.edu.udea.brujula.infraestructura.salida.persistencia.adaptador;

import co.edu.udea.brujula.dominio.excepcion.Conflicto;
import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.Recomendacion;
import co.edu.udea.brujula.dominio.modelo.Simulacro;
import co.edu.udea.brujula.dominio.puerto.salida.SimulacroRepositorio;
import co.edu.udea.brujula.infraestructura.salida.persistencia.Mapeador;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.RecomendacionEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.SimulacroEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.CompetenciaJpa;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.ComponenteJpa;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.DuracionSimulacroJpa;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.RecomendacionJpa;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.SimulacroJpa;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SimulacroAdaptador implements SimulacroRepositorio {

    private final SimulacroJpa simulacros;
    private final RecomendacionJpa recomendaciones;
    private final DuracionSimulacroJpa duraciones;
    private final ComponenteJpa componentes;
    private final CompetenciaJpa competencias;

    public SimulacroAdaptador(SimulacroJpa simulacros, RecomendacionJpa recomendaciones,
                              DuracionSimulacroJpa duraciones, ComponenteJpa componentes,
                              CompetenciaJpa competencias) {
        this.simulacros = simulacros;
        this.recomendaciones = recomendaciones;
        this.duraciones = duraciones;
        this.componentes = componentes;
        this.competencias = competencias;
    }

    @Override
    public Simulacro guardar(Simulacro simulacro) {
        SimulacroEntidad entidad = new SimulacroEntidad();
        entidad.setIdUsuario(simulacro.idEstudiante());
        entidad.setDuracion(duraciones.getReferenceById(simulacro.duracion().id()));
        entidad.setInicio(simulacro.inicio());
        entidad.setFin(simulacro.fin());
        entidad.setEstado(simulacro.estado());
        entidad.setTiempoUtilizadoSeg(simulacro.tiempoUtilizadoSeg());
        try {
            simulacro.asignarId(simulacros.saveAndFlush(entidad).getId());
            return simulacro;
        } catch (DataIntegrityViolationException e) {
            throw new Conflicto("SIMULACRO_EN_CURSO",
                    "Ya tienes un simulacro en curso. Continúalo o finalízalo antes de iniciar otro.");
        }
    }

    @Override
    public boolean registrarFinalizacion(Simulacro simulacro) {
        return simulacros.finalizar(simulacro.id(), simulacro.fin(), simulacro.tiempoUtilizadoSeg()) == 1;
    }

    @Override
    public Optional<Simulacro> deEstudiante(Long idSimulacro, Long idEstudiante) {
        return simulacros.findByIdAndIdUsuario(idSimulacro, idEstudiante).map(Mapeador::aDominio);
    }

    @Override
    public Optional<Simulacro> deEstudianteParaResponder(Long idSimulacro, Long idEstudiante) {
        return simulacros.bloquearDelUsuario(idSimulacro, idEstudiante).map(Mapeador::aDominio);
    }

    @Override
    public List<Simulacro> enCursoDe(Long idEstudiante) {
        return simulacros.findByIdUsuarioAndEstado(idEstudiante, Simulacro.EN_CURSO).stream()
                .map(Mapeador::aDominio).toList();
    }

    @Override
    public List<Simulacro> finalizadosDe(Long idEstudiante) {
        return simulacros.findByIdUsuarioAndEstadoOrderByInicioDesc(idEstudiante, Simulacro.FINALIZADO).stream()
                .map(Mapeador::aDominio).toList();
    }

    @Override
    public Pagina<Simulacro> finalizadosDe(Long idEstudiante, int pagina, int tamano) {
        Page<SimulacroEntidad> encontrados = simulacros.findByIdUsuarioAndEstadoOrderByInicioDescIdDesc(
                idEstudiante, Simulacro.FINALIZADO, PageRequest.of(pagina, tamano));
        return Pagina.de(encontrados.getContent().stream().map(Mapeador::aDominio).toList(),
                encontrados.getNumber(), encontrados.getSize(), encontrados.getTotalElements());
    }

    @Override
    public void guardarRecomendaciones(Long idSimulacro, List<Recomendacion> nuevas) {
        recomendaciones.saveAll(nuevas.stream().map(recomendacion -> comoEntidad(idSimulacro, recomendacion))
                .toList());
    }

    @Override
    public List<Recomendacion> recomendacionesDe(Long idSimulacro) {
        return recomendaciones.findByIdSimulacroOrderByOrdenAsc(idSimulacro).stream()
                .map(Mapeador::aDominio).toList();
    }

    private RecomendacionEntidad comoEntidad(Long idSimulacro, Recomendacion recomendacion) {
        RecomendacionEntidad entidad = new RecomendacionEntidad();
        entidad.setIdSimulacro(idSimulacro);
        entidad.setOrden((short) recomendacion.orden());
        entidad.setPorcentaje(recomendacion.porcentaje());
        entidad.setMensaje(recomendacion.mensaje());
        if (recomendacion.esDeComponente()) {
            entidad.setComponente(componentes.getReferenceById(recomendacion.idComponente()));
        } else {
            entidad.setCompetencia(competencias.getReferenceById(recomendacion.idCompetencia()));
        }
        return entidad;
    }
}
