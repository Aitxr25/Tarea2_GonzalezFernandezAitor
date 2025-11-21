package controlador.servicios;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.Set;

import controlador.dao.*;
import modelo.*;

//esto lo cree para el metodo registrar persona, no se si esta bien hacerlo porque no existe una clase "Registro"
public class RegistroService {

	private final CredencialesDAO credDAO;
	private final PersonaDAO personaDAO;
	private final ArtistaDAO artistaDAO;
	private final CoordinacionDAO coordDAO;
	private final NumeroDAO numeroDAO;

	public RegistroService(ConexionBD conexion) {

		Connection conn = conexion.getConnection();

		this.credDAO = new CredencialesDAO(conexion);
		this.personaDAO = new PersonaDAO(conexion);
		this.coordDAO = new CoordinacionDAO(conexion);

		this.numeroDAO = new NumeroDAO(conn, conexion);

		this.artistaDAO = new ArtistaDAO(conn, this.personaDAO);

		this.numeroDAO.setArtistaDAO(artistaDAO);
		this.artistaDAO.setNumeroDAO(numeroDAO);
	}

	// metodo que comprueba si ya existe el nombre del usuario
	public boolean existeUsuario(String nombre) {
		return credDAO.existeNombreUsuario(nombre);
	}

	// metodo que comprueba si ya existe el email
	public boolean existeEmail(String email) {
		return personaDAO.existeEmail(email);
	}

	// metodo para registrar
	public Long registrar(String nombreReal, String email, String nombreUsuario, String password, String nacionalidad,
			Perfil perfil, boolean esSenior, LocalDate fechaSenior, String apodo, Set<Especialidad> especialidades) {

		try {
			// credenciales
			long idCred = credDAO.insertar(nombreUsuario, password, perfil);
			if (idCred <= 0)
				return null;

			// para insertar persona
			Persona p = new Persona(null, idCred, email, nombreReal, nacionalidad);

			long idPersona = personaDAO.insertarPersona(p);
			if (idPersona <= 0)
				return null;

			// diferenciar por perfil
			if (perfil == Perfil.COORDINADOR) {
				coordDAO.insertarCoordinador(idPersona, esSenior, fechaSenior);
			}

			if (perfil == Perfil.ARTISTA) {
				artistaDAO.insertarArtista(idPersona, apodo, especialidades);
			}

			return idPersona;

		} catch (Exception e) {
			System.err.println("Error el en registro: " + e.getMessage());
			return null;
		}
	}
}
