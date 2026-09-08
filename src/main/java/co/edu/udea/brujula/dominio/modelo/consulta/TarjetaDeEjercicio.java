package co.edu.udea.brujula.dominio.modelo.consulta;

/** Lo que se ve de un ejercicio en el banco. El conteo de intentos solo se llena para el administrador. */
public record TarjetaDeEjercicio(Long id, Integer numero, String componente, String competencia,
                                 String nivel, String estado, Long intentos) {
}
