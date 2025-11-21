package controlador.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import modelo.Espectaculo;
import modelo.Numero;

public class EspectaculoDAO {

	private final Connection conex;
	private final NumeroDAO numeroDAO;

	public EspectaculoDAO(Connection conex, ConexionBD conexionBD, NumeroDAO numeroDAO) {
		this.conex = conex;
		this.numeroDAO = numeroDAO;
	}

	// metodo para cambiar/introducir los artistas a un espectaculo
	public void setArtistaDAO(ArtistaDAO artistaDAO) {
		this.numeroDAO.setArtistaDAO(artistaDAO);
	}

	// metodo que comprueba si ya existe el nombre del espectaculo
	public boolean existeNombre(String nombre) {
		String sql = "SELECT 1 FROM espectaculos WHERE nombre = ?";

		try (PreparedStatement ps = conex.prepareStatement(sql)) {
			ps.setString(1, nombre);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		} catch (SQLException e) {
			System.err.println("Error comprobando el nombre del espectaculo: " + e.getMessage());
			return false;
		}
	}

	// metodo que inserta espectaculos
	public long insertar(String nombre, LocalDate fechaini, LocalDate fechafin, long idCoord) {

		String sql = """
				    INSERT INTO espectaculos(nombre, fechaini, fechafin, idCoord)
				    VALUES (?, ?, ?, ?)
				""";

		try (PreparedStatement ps = conex.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			ps.setString(1, nombre);
			ps.setDate(2, Date.valueOf(fechaini));
			ps.setDate(3, Date.valueOf(fechafin));
			ps.setLong(4, idCoord);

			ps.executeUpdate();

			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next())
					return rs.getLong(1);
			}

		} catch (SQLException e) {
			System.err.println("Error insertando el espectaculo: " + e.getMessage());
		}

		return -1;
	}

	// metodo que actualiza espectaculos
	public boolean actualizar(Espectaculo e) {

		String sql = """
				    UPDATE espectaculos
				    SET nombre = ?, fechaini = ?, fechafin = ?, idCoord = ?
				    WHERE id = ?
				""";

		try (PreparedStatement ps = conex.prepareStatement(sql)) {

			ps.setString(1, e.getNombre());
			ps.setDate(2, Date.valueOf(e.getFechaini()));
			ps.setDate(3, Date.valueOf(e.getFechafin()));
			ps.setLong(4, e.getIdCoord());
			ps.setLong(5, e.getId());

			return ps.executeUpdate() > 0;

		} catch (SQLException ex) {
			System.err.println("Error actualizando el espectaculo: " + ex.getMessage());
			return false;
		}
	}

	// metodo que busca espectaculos por su id
	public Espectaculo buscarPorId(Long id) {

		String sql = "SELECT id, nombre, fechaini, fechafin, idCoord FROM espectaculos WHERE id = ?";
		Espectaculo espectaculo = null;

		try (PreparedStatement ps = conex.prepareStatement(sql)) {

			ps.setLong(1, id);

			try (ResultSet rs = ps.executeQuery()) {

				if (rs.next()) {

					Set<Numero> numeros = numeroDAO.obtenerNumerosDeEspectaculo(id);

					espectaculo = new Espectaculo(rs.getLong("id"), rs.getString("nombre"),
							rs.getDate("fechaini").toLocalDate(), rs.getDate("fechafin").toLocalDate(),
							rs.getLong("idCoord"), numeros);
				}
			}

		} catch (SQLException e) {
			System.err.println("Error buscando el espectaculo por ID: " + e.getMessage());
		}

		return espectaculo;
	}

	// metodo que muestra todos los espectaculos de la BD
	public List<Espectaculo> obtenerTodos() {

		List<Espectaculo> lista = new ArrayList<>();

		String sql = "SELECT id FROM espectaculos";

		try (PreparedStatement ps = conex.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				long id = rs.getLong("id");
				Espectaculo esp = buscarPorId(id);
				if (esp != null)
					lista.add(esp);
			}

		} catch (SQLException e) {
			System.err.println("Error obteniendo los espectaculos: " + e.getMessage());
		}

		return lista;
	}
}
