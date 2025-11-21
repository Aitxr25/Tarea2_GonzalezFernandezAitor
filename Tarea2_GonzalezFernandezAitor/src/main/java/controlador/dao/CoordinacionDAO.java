package controlador.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import modelo.Coordinacion;
import modelo.Persona;

public class CoordinacionDAO {

	private final ConexionBD conex;

	public CoordinacionDAO(ConexionBD conex) {
		this.conex = conex;
	}

	// metodo para buscar el coordinador por la id
	public Coordinacion buscarCoordinadorPorId(Long idCoord) {

		String sql = "SELECT idCoord, idPersona, senior, fechasenior "
				+ "FROM coordinacion WHERE idCoord = ?";

		try (PreparedStatement ps = conex.getConnection().prepareStatement(sql)) {

			ps.setLong(1, idCoord);

			try (ResultSet rs = ps.executeQuery()) {

				if (rs.next()) {

					Long idPersona = rs.getLong("idPersona");


					PersonaDAO personaDAO = new PersonaDAO(conex);
					Persona p = personaDAO.obtenerPorId(idPersona);

					if (p == null) {
						System.err.println("No se encontro la persona asociada al coordinador.");
						return null;
					}

					return new Coordinacion(
							idPersona,                   
							p.getNombre(),
							p.getEmail(),
							p.getNacionalidad(),
							idCoord,                     
							rs.getBoolean("senior"),
							rs.getDate("fechasenior") != null
							? rs.getDate("fechasenior").toLocalDate()
									: null
							);

				}
			}

		} catch (SQLException e) {
			System.err.println("Error buscando coordinador: " + e.getMessage());
		}

		return null;
	}

	// metodo para ver si existe el coordinador
	public boolean existeCoordinador(long idCoord) {

		String sql = "SELECT COUNT(*) FROM coordinacion WHERE idCoord = ?";

		try (PreparedStatement ps = conex.getConnection().prepareStatement(sql)) {

			ps.setLong(1, idCoord);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) return rs.getInt(1) > 0;
			}

		} catch (SQLException e) {
			System.err.println("Error comprobando coordinador: " + e.getMessage());
		}

		return false;
	}

	// metodo para insertar coordinador
	public void insertarCoordinador(long idPersona, boolean senior, LocalDate fecha) {

		String sql = "INSERT INTO coordinacion (idPersona, senior, fechasenior) "
				+ "VALUES (?, ?, ?)";

		try (PreparedStatement ps = conex.getConnection().prepareStatement(sql)) {

			ps.setLong(1, idPersona);
			ps.setBoolean(2, senior);

			if (fecha != null)
				ps.setDate(3, java.sql.Date.valueOf(fecha));
			else
				ps.setNull(3, Types.DATE);

			ps.executeUpdate();

		} catch (SQLException e) {
			System.err.println("Error insertando coordinador: " + e.getMessage());
		}
	}

	//metodo para obtener la id de los coordinadores por su id de credenciales
	public Long obtenerIdCoordPorIdCredenciales(long idCred) {

		String sql = """
				    SELECT c.idCoord
				    FROM coordinacion c
				    JOIN persona p ON c.idPersona = p.id
				    WHERE p.id_credenciales = ?
				""";

		try (PreparedStatement ps = conex.getConnection().prepareStatement(sql)) {

			ps.setLong(1, idCred);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) return rs.getLong("idCoord");

		} catch (Exception e) {
			System.out.println("Error obteniendo idCoord: " + e.getMessage());
		}

		return null;
	}
	//metodo para obtener todos los coordinadores
	public List<Coordinacion> obtenerTodos() {

		List<Coordinacion> lista = new ArrayList<>();

		String sql = """
				    SELECT idCoord
				    FROM coordinacion
				""";

		try (PreparedStatement ps = conex.getConnection().prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				long idCoord = rs.getLong("idCoord");

				Coordinacion c = buscarCoordinadorPorId(idCoord);
				if (c != null) lista.add(c);
			}

		} catch (Exception e) {
			System.out.println("Error obteniendo lista de coordinadores: " + e.getMessage());
		}

		return lista;
	}

	//busca coordinador por su id persona
	public Coordinacion buscarPorIdPersona(Long idPersona) {

		String sql = "SELECT idCoord FROM coordinacion WHERE idPersona = ?";

		try (PreparedStatement ps = conex.getConnection().prepareStatement(sql)) {

			ps.setLong(1, idPersona);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				long idCoord = rs.getLong("idCoord");
				return buscarCoordinadorPorId(idCoord);
			}

		} catch (Exception e) {
			System.out.println("Error buscando coordinador por persona: " + e.getMessage());
		}

		return null;
	}

	//metodo que actualiza el coordinador
	public boolean actualizarCoordinador(Long idPersona, boolean senior, LocalDate fecha) {

		// comprobar si existe
		Coordinacion coord = buscarPorIdPersona(idPersona);

		if (coord == null) {

			insertarCoordinador(idPersona, senior, fecha);
			return true;
		}

		String sql = "UPDATE coordinacion SET senior = ?, fechasenior = ? WHERE idCoord = ?";

		try (PreparedStatement ps = conex.getConnection().prepareStatement(sql)) {

			ps.setBoolean(1, senior);

			if (fecha != null)
				ps.setDate(2, java.sql.Date.valueOf(fecha));
			else
				ps.setNull(2, Types.DATE);

			ps.setLong(3, coord.getIdCoord());

			return ps.executeUpdate() > 0;

		} catch (SQLException e) {
			System.err.println("Error actualizando coordinador: " + e.getMessage());
			return false;
		}
	}
}
