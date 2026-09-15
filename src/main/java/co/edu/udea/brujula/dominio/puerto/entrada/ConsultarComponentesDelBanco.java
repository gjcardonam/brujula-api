package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.consulta.ComponentesDelBanco;

public interface ConsultarComponentesDelBanco {

    ComponentesDelBanco componentes(boolean esAdministrador);
}
