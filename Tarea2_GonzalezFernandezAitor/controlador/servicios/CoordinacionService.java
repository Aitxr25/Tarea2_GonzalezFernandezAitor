/**
* Clase CoordinacionService.java
* @author AITOR GONZÁLEZ FERNÁNDEZ
* @version 1.0
*/

package servicios;

import java.time.LocalDate;
import java.util.List;

import dao.CoordinacionDAO;
import modelo.Coordinacion;

public class CoordinacionService {

    private final CoordinacionDAO coordinacionDAO;

    public CoordinacionService(CoordinacionDAO coordinacionDAO) {
        this.coordinacionDAO = coordinacionDAO;
    }

    //metodo que devuelve al coordinador por su id
    public Coordinacion obtenerCoordinadorPorId(Long idCoord) {
        if (idCoord == null) {
            throw new IllegalArgumentException("El ID del coordinador no puede ser null");
        }

        return coordinacionDAO.buscarCoordinadorPorId(idCoord);
    }
    
    // metodo que busca coordinador por idPersona
    public Coordinacion buscarPorIdPersona(Long idPersona) {
        return coordinacionDAO.buscarPorIdPersona(idPersona);
    }
    
    //metodo que actualiza al coordinador
    public boolean actualizarCoordinador(Long idPersona, boolean senior, LocalDate fecha) {
        return coordinacionDAO.actualizarCoordinador(idPersona, senior, fecha);
    }
    
    //metodo que devuelve lista de coordinadores
    public List<Coordinacion> obtenerTodos() {
        return coordinacionDAO.obtenerTodos();
    }
}
