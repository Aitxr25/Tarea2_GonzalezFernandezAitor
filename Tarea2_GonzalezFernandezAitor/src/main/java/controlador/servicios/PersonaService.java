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

	//metodo para obtener todas las personas
	public List<Persona> obtenerTodas() {
		return personaDAO.obtenerTodasLasPersonas();
	}

	//metodo para obtener persona por su id
	public Persona obtenerPorId(Long id) {
		return personaDAO.obtenerPorId(id);
	}

	//metodo para comprobar si el email esta disponible
	public boolean emailDisponibleParaOtro(String email, Long idPersona) {
		return !personaDAO.existeEmailParaOtraPersona(email, idPersona);
	}

	//metodo para actualizar los datos
	public boolean actualizarDatosPersonales(Long idPersona, String nombre, String email, String nacionalidad) {
		return personaDAO.actualizarDatosPersonales(idPersona, nombre, email, nacionalidad);
	}
}