/**
* Clase PersonaService.java
* @author AITOR GONZÁLEZ FERNÁNDEZ
* @version 1.0
*/

package controlador.servicios;

import java.util.List;

import controlador.dao.ConexionBD;
import controlador.dao.PersonaDAO;
import modelo.Persona;

public class PersonaService {
	private final PersonaDAO personaDAO;

    public PersonaService(ConexionBD conexionBD) {
        this.personaDAO = new PersonaDAO(conexionBD);
    }

    public List<Persona> obtenerTodas() {
        return personaDAO.obtenerTodasLasPersonas();
    }

    public Persona obtenerPorId(Long id) {
        return personaDAO.obtenerPorId(id);
    }

    public boolean emailDisponibleParaOtro(String email, Long idPersona) {
        return !personaDAO.existeEmailParaOtraPersona(email, idPersona);
    }

    public boolean actualizarDatosPersonales(Long idPersona,
                                             String nombre,
                                             String email,
                                             String nacionalidad) {
        return personaDAO.actualizarDatosPersonales(idPersona, nombre, email, nacionalidad);
    }
}