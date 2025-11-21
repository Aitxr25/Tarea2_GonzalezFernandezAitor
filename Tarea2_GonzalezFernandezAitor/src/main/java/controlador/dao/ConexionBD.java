package controlador.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import vista.Config;

public class ConexionBD {
	private Connection conex = null;
	private static ConexionBD instancia;

	private final String urlBD;
	private final String usuarioBD;
	private final String contraseniaBD;

	private ConexionBD() {
		this.urlBD = Config.get("db.url");
		this.usuarioBD = Config.get("db.user");
		this.contraseniaBD = Config.get("db.pass");
	}

	// metodo que crea la conexion si no existe
	public static ConexionBD getInstance() {
		if (instancia == null) {
			instancia = new ConexionBD();
		}
		return instancia;
	}

	// metodo que obtiene la conexion
	public Connection getConnection() {
		try {
			if (conex == null || conex.isClosed()) {

				try {
					Class.forName("com.mysql.cj.jdbc.Driver");
				} catch (ClassNotFoundException e) {

					System.err.println("JDBC Driver no encontrado: " + e.getMessage());
				}
				conex = DriverManager.getConnection(urlBD, usuarioBD, contraseniaBD);
				System.out.println("Conexion establecida a la base de datos");
			}
		} catch (SQLException e) {
			System.err.println("Error al obtener la conexión: " + e.getMessage());
		}
		return conex;
	}

	// metodo que cierra la conexion
	public void cerrarConexion() {
		try {
			if (conex != null && !conex.isClosed()) {
				conex.close();
				conex = null;
				System.out.println("Conexion cerrada.");
			}
		} catch (SQLException e) {
			System.err.println(" Error cerrando conexion: " + e.getMessage());
		}
	}
}
