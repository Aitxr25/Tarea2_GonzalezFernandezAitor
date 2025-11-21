/**
* Clase CoordinacionService.java
* @author AITOR GONZÁLEZ FERNÁNDEZ
* @version 1.0
*/

package controlador.servicios;

import java.time.LocalDate;
import java.util.List;

import controlador.dao.CoordinacionDAO;
import modelo.Coordinacion;

public class CoordinacionService {

    private final CoordinacionDAO coordinacionDAO;

    public CoordinacionService(CoordinacionDAO coordinacionDAO) {
        this.coordinacionDAO = coordinacionDAO;
    }

    //metodo que devuelve al coordinador por su id
    public Coordinacion obtenerCoordinadorPorId(Long idCoord) {

        if (idCoord == null) {
            System.err.println("Error: el ID del coordinador no puede ser null.");
            return null;
        }

        try {
            return coordinacionDAO.buscarCoordinadorPorId(idCoord);
        } catch (Exception e) {
            System.err.println("Error obteniendo coordinador por ID: " + e.getMessage());
            return null;
        }
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
