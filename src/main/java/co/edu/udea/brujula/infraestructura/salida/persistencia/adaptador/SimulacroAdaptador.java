package co.edu.udea.brujula.infraestructura.salida.persistencia.adaptador;

import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.Recomendacion;
import co.edu.udea.brujula.dominio.modelo.Simulacro;
import co.edu.udea.brujula.dominio.puerto.salida.SimulacroRepositorio;
import co.edu.udea.brujula.infraestructura.salida.persistencia.Mapeador;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.RecomendacionEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.SimulacroEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.*;
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
        SimulacroEntidad entidad = simulacro.id() == null
                ? new SimulacroEntidad()
                : simulacros.findById(simulacro.id()).orElseGet(SimulacroEntidad::new);
        entidad.setIdUsuario(simulacro.idEstudiante());
        entidad.setDuracion(duraciones.findById(simulacro.duracion().id()).orElseThrow());
        entidad.setInicio(simulacro.inicio());
        entidad.setFin(simulacro.fin());
        entidad.setEstado(simulacro.estado());
        entidad.setTiempoUtilizadoSeg(simulacro.tiempoUtilizadoSeg());

        SimulacroEntidad guardado = simulacros.save(entidad);
        simulacro.asignarId(guardado.getId());
        return Mapeador.aDominio(guardado);
    }

    @Override
    public Optional<Simulacro> deEstudiante(Long idSimulacro, Long idEstudiante) {
        return simulacros.findByIdAndIdUsuario(idSimulacro, idEstudiante).map(Mapeador::aDominio);
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
        Page<SimulacroEntidad> encontrados = simulacros.findByIdUsuarioAndEstadoOrderByInicioDesc(
                idEstudiante, Simulacro.FINALIZADO, PageRequest.of(pagina, tamano));
        return Pagina.de(encontrados.getContent().stream().map(Mapeador::aDominio).toList(),
                encontrados.getNumber(), encontrados.getSize(), encontrados.getTotalElements());
    }

    @Override
    public void guardarRecomendaciones(Long idSimulacro, List<Recomendacion> nuevas) {
        List<RecomendacionEntidad> entidades = nuevas.stream().map(r -> {
            RecomendacionEntidad entidad = new RecomendacionEntidad();
            entidad.setIdSimulacro(idSimulacro);
            entidad.setOrden(r.orden());
            entidad.setMensaje(r.mensaje());
            entidad.setPorcentaje(r.porcentaje());
            if (r.idComponente() != null) {
                entidad.setComponente(componentes.getReferenceById(r.idComponente()));
            } else {
                entidad.setCompetencia(competencias.getReferenceById(r.idCompetencia()));
            }
            return entidad;
        }).toList();
        recomendaciones.saveAll(entidades);
    }

    @Override
    public List<Recomendacion> recomendacionesDe(Long idSimulacro) {
        return recomendaciones.findByIdSimulacroOrderByOrdenAsc(idSimulacro).stream()
                .map(Mapeador::aDominio).toList();
    }
}
