package controlador.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import modelo.Artista;
import modelo.Especialidad;
import modelo.Numero;
import modelo.Persona;

public class ArtistaDAO {

	private final Connection conex;
	private final PersonaDAO personaDAO;
	private NumeroDAO numeroDAO;

	public ArtistaDAO(Connection conex, PersonaDAO personaDAO) {
		this.conex = conex;
		this.personaDAO = personaDAO;
	}

	public void setNumeroDAO(NumeroDAO numeroDAO) {
		this.numeroDAO = numeroDAO;
	}

	// metodo para insertar artista en la BD
	public long insertarArtista(Long idPersona, String apodo, Set<Especialidad> especialidades) {

		if (idPersona == null) {
			System.err.println("Error añadiendo al artista");
			return -1;
		}

		String sql = """
				    INSERT INTO artistas (idPersona, apodo, especialidades)
				    VALUES (?, ?, ?)
				""";

		String especStr = "";

		try {
			especStr = String.join(",", especialidades.stream().map(Enum::name).toList());
		} catch (Exception e) {
			System.err.println(" Error al formatear las especialidades: " + e.getMessage());
			return -1;
		}

		try (PreparedStatement ps = conex.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			ps.setLong(1, idPersona);
			ps.setString(2, apodo);
			ps.setString(3, especStr);

			ps.executeUpdate();

			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next())
				return rs.getLong(1);

		} catch (SQLException e) {
			System.err.println(" Error añadiendo artista: " + e.getMessage());
		}

		return -1;
	}

	// metodo para obtener por id del artista al artista de la BD
	public Artista obtenerPorId(Long idArtista) {

		if (idArtista == null) {
			System.err.println("La ID no es valida");
			return null;
		}

		String sql = """
				SELECT idArt, idPersona, apodo, especialidades
				FROM artistas
				WHERE idArt = ?
				""";

		try (PreparedStatement ps = conex.prepareStatement(sql)) {

			ps.setLong(1, idArtista);

			try (ResultSet rs = ps.executeQuery()) {

				if (!rs.next())
					return null;

				long idPersona = rs.getLong("idPersona");
				String apodo = rs.getString("apodo");
				String especStr = rs.getString("especialidades");

				Persona persona;

				try {
					persona = personaDAO.obtenerPorId(idPersona);
				} catch (Exception ex) {
					System.err.println("Error obteniendo la persona asociada: " + ex.getMessage());
					return null;
				}

				if (persona == null)
					return null;

				Set<Especialidad> especialidades = new HashSet<>();

				if (especStr != null && !especStr.isBlank()) {
					for (String esp : especStr.split(",")) {
						try {
							especialidades.add(Especialidad.valueOf(esp.trim()));
						} catch (Exception ex) {
							System.err.println("Especialidad invalida: " + esp);
						}
					}
				}

				List<Numero> numeros = new ArrayList<>();

				try {
					if (numeroDAO != null) {
						numeros = numeroDAO.obtenerNumerosDeArtista(idArtista);
					}
				} catch (Exception ex) {
					System.err.println("Error obteniendo los numeros del artista: " + ex.getMessage());
				}

				return new Artista(idPersona, persona.getNombre(), persona.getEmail(), persona.getNacionalidad(),
						idArtista, apodo, especialidades, numeros);

			}

		} catch (SQLException e) {
			System.err.println("Error obteniendo artista: " + e.getMessage());
		}

		return null;
	}

	// metodo para obtener todos los artistas de la BD
	public List<Artista> obtenerTodos() {

		List<Artista> lista = new ArrayList<>();

		String sql = "SELECT idArt FROM artistas";

		try (PreparedStatement ps = conex.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				long idArt = rs.getLong("idArt");

				Artista art = null;
				try {
					art = obtenerPorId(idArt);
				} catch (Exception ex) {
					System.err.println(" Error cargando artista " + idArt + ": " + ex.getMessage());
				}

				if (art != null)
					lista.add(art);
			}

		} catch (SQLException e) {
			System.err.println(" Error obteniendo la lista: " + e.getMessage());
		}

		return lista;
	}

	// metodo para obtener artista por su id en credenciales
	public Long obtenerIdArtPorIdCredenciales(Long idCred) {

		if (idCred == null) {
			System.err.println("Credenciales nulas");
			return null;
		}

		String sql = """
				    SELECT a.idArt
				    FROM artistas a
				    JOIN persona p ON a.idPersona = p.id
				    WHERE p.id_credenciales = ?
				""";

		try (PreparedStatement ps = conex.prepareStatement(sql)) {

			ps.setLong(1, idCred);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getLong("idArt");
			}

		} catch (SQLException e) {
			System.err.println("Error obteniendo el ID del artista: " + e.getMessage());
		}

		return null;
	}

	// metodo que actualiza al artista en la BD
	public boolean actualizarArtista(Long idPersona, String apodo, Set<Especialidad> especialidades) {

		if (idPersona == null)
			return false;

		String especStr = especialidades.stream().map(Enum::name).collect(Collectors.joining(","));

		// para comprobar si ya existe el artista
		String checkSql = "SELECT idArt FROM artistas WHERE idPersona = ?";

		try (PreparedStatement check = conex.prepareStatement(checkSql)) {
			check.setLong(1, idPersona);
			ResultSet rs = check.executeQuery();

			if (rs.next()) {

				long idArt = rs.getLong("idArt");
				String updateSql = "UPDATE artistas SET apodo = ?, especialidades = ? WHERE idArt = ?";

				try (PreparedStatement ps = conex.prepareStatement(updateSql)) {
					ps.setString(1, apodo);
					ps.setString(2, especStr);
					ps.setLong(3, idArt);
					return ps.executeUpdate() > 0;
				}
			} else {

				insertarArtista(idPersona, apodo, especialidades);
				return true;
			}

		} catch (SQLException e) {
			System.err.println("Error actualizando el artista: " + e.getMessage());
			return false;
		}
	}

}
