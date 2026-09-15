package co.edu.udea.brujula.infraestructura.salida.persistencia.adaptador;

import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.modelo.Opcion;
import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.consulta.ComponentesDelBanco;
import co.edu.udea.brujula.dominio.puerto.salida.EjercicioRepositorio;
import co.edu.udea.brujula.infraestructura.salida.persistencia.Mapeador;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.EjercicioEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.OpcionEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.CompetenciaJpa;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.ComponenteJpa;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.EjercicioJpa;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.NivelDificultadJpa;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.UsuarioJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EjercicioAdaptador implements EjercicioRepositorio {

    private final EjercicioJpa ejercicios;
    private final ComponenteJpa componentes;
    private final CompetenciaJpa competencias;
    private final NivelDificultadJpa niveles;
    private final UsuarioJpa usuarios;

    public EjercicioAdaptador(EjercicioJpa ejercicios, ComponenteJpa componentes, CompetenciaJpa competencias,
                              NivelDificultadJpa niveles, UsuarioJpa usuarios) {
        this.ejercicios = ejercicios;
        this.componentes = componentes;
        this.competencias = competencias;
        this.niveles = niveles;
        this.usuarios = usuarios;
    }

    @Override
    public Pagina<Ejercicio> buscar(Long idComponente, boolean soloActivos, int pagina, int tamano) {
        Page<EjercicioEntidad> encontrados = ejercicios.listar(soloActivos, idComponente,
                PageRequest.of(pagina, tamano));
        return Pagina.de(encontrados.getContent().stream().map(Mapeador::aDominioSinOpciones).toList(),
                encontrados.getNumber(), encontrados.getSize(), encontrados.getTotalElements());
    }

    @Override
    public Optional<Ejercicio> porId(Long id) {
        return ejercicios.buscarCompleto(id).map(Mapeador::aDominio);
    }

    @Override
    public ComponentesDelBanco conteoPorComponente(boolean soloActivos) {
        List<ComponentesDelBanco.Conteo> conteos = ejercicios.contarPorComponente(soloActivos).stream()
                .map(fila -> new ComponentesDelBanco.Conteo((Long) fila[0], (String) fila[1], (Long) fila[2]))
                .toList();
        return new ComponentesDelBanco(ejercicios.contarTotal(soloActivos), conteos);
    }

    @Override
    public boolean existeConEnunciado(String enunciado) {
        return ejercicios.contarConEnunciado(enunciado) > 0;
    }

    @Override
    public Optional<Long> siguienteParaPractica(Long idActual, Long idComponente, Long idEstudiante) {
        return ejercicios.siguienteParaPractica(idActual, idComponente, idEstudiante);
    }

    @Override
    public Ejercicio guardar(Ejercicio ejercicio) {
        EjercicioEntidad entidad = new EjercicioEntidad();
        entidad.setEnunciado(ejercicio.enunciado());
        entidad.setImagen(ejercicio.imagen());
        entidad.setEstado(ejercicio.estado());
        entidad.setCreadoEn(ejercicio.creadoEn());
        entidad.setCreador(usuarios.getReferenceById(ejercicio.idCreador()));
        entidad.setComponente(componentes.findById(ejercicio.componente().id()).orElseThrow());
        entidad.setCompetencia(competencias.findById(ejercicio.competencia().id()).orElseThrow());
        entidad.setNivel(niveles.findById(ejercicio.nivel().id()).orElseThrow());
        ejercicio.opciones().forEach(opcion -> entidad.getOpciones().add(comoEntidad(opcion, entidad)));
        return Mapeador.aDominio(ejercicios.saveAndFlush(entidad));
    }

    private static OpcionEntidad comoEntidad(Opcion opcion, EjercicioEntidad ejercicio) {
        OpcionEntidad entidad = new OpcionEntidad();
        entidad.setEjercicio(ejercicio);
        entidad.setOrden((short) opcion.orden());
        entidad.setTexto(opcion.texto());
        entidad.setImagen(opcion.imagen());
        entidad.setCorrecta(opcion.correcta());
        entidad.setRetroalimentacion(opcion.retroalimentacion());
        return entidad;
    }
}
