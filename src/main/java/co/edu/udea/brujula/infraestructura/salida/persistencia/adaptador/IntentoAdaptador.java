package co.edu.udea.brujula.infraestructura.salida.persistencia.adaptador;

import co.edu.udea.brujula.dominio.excepcion.Conflicto;
import co.edu.udea.brujula.dominio.modelo.Intento;
import co.edu.udea.brujula.dominio.puerto.salida.IntentoRepositorio;
import co.edu.udea.brujula.infraestructura.salida.persistencia.Mapeador;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.IntentoEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.EjercicioJpa;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.IntentoJpa;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.OpcionJpa;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class IntentoAdaptador implements IntentoRepositorio {

    private final IntentoJpa intentos;
    private final EjercicioJpa ejercicios;
    private final OpcionJpa opciones;

    public IntentoAdaptador(IntentoJpa intentos, EjercicioJpa ejercicios, OpcionJpa opciones) {
        this.intentos = intentos;
        this.ejercicios = ejercicios;
        this.opciones = opciones;
    }

    @Override
    public Intento guardar(Intento intento) {
        IntentoEntidad entidad = new IntentoEntidad();
        entidad.setRespondidoEn(intento.respondidoEn());
        entidad.setCorrecto(intento.correcto());
        entidad.setNivelConfianza((short) intento.nivelConfianza());
        entidad.setIdUsuario(intento.idEstudiante());
        entidad.setEjercicio(ejercicios.getReferenceById(intento.ejercicio().id()));
        entidad.setOpcionSeleccionada(opciones.getReferenceById(intento.opcionSeleccionada().id()));
        entidad.setTokenIdempotencia(intento.tokenIdempotencia());
        try {
            IntentoEntidad guardado = intentos.saveAndFlush(entidad);
            return new Intento(guardado.getId(), intento.respondidoEn(), intento.correcto(),
                    intento.nivelConfianza(), intento.idEstudiante(), intento.ejercicio(),
                    intento.opcionSeleccionada(), intento.tokenIdempotencia());
        } catch (DataIntegrityViolationException e) {
            return porToken(intento.tokenIdempotencia()).orElseThrow(() -> new Conflicto("INTENTO_DUPLICADO",
                    "No fue posible registrar la respuesta. Intenta de nuevo."));
        }
    }

    @Override
    public Optional<Intento> porToken(UUID token) {
        return intentos.findByTokenIdempotencia(token).map(Mapeador::aDominio);
    }

    @Override
    public long cantidadDeIntentos(Long idEstudiante, Long idEjercicio) {
        return intentos.countByIdUsuarioAndEjercicio_Id(idEstudiante, idEjercicio);
    }
}
