package controlador.dao;

import modelo.Credenciales;
import modelo.Perfil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;


public class CredencialesDAO {

    private final ConexionBD conexionBD;

    public CredencialesDAO(ConexionBD conexionBD) {
        this.conexionBD = conexionBD;
    }

    public Optional<Credenciales> obtenerPorNombreYPassword(String nombre, String password) {
        String sql = "SELECT id, nombre, password, perfil FROM credenciales WHERE nombre = ? AND password = ?";
        Connection conn = conexionBD.getConnection();
        if (conn == null) {
            System.err.println(" Conexión nula"); 
            return Optional.empty();
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Long id = rs.getLong("id");
                    String nombreBD = rs.getString("nombre");
                    String pwd = rs.getString("password");
                    String perfilStr = rs.getString("perfil"); 
                    Perfil perfil;
                    try {
                        perfil = Perfil.valueOf(perfilStr.toUpperCase());
                    } catch (Exception e) {
                       
                        perfil = Perfil.COORDINADOR;
                    }
                    return Optional.of(new Credenciales(id, nombreBD, pwd, perfil));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error SQL: " + e.getMessage());
        }
        return Optional.empty();
    }
    public long insertar(String nombre, String password, Perfil perfil) {
        String sql = "INSERT INTO credenciales (nombre, password, perfil) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conexionBD.getConnection()
                    .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nombre);
            ps.setString(2, password);
            ps.setString(3, perfil.name());

            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getLong(1);

        } catch (SQLException e) {
            System.err.println("Error insertando las credenciales: " + e.getMessage());
        }

        return -1;
    }
    public boolean existeNombreUsuario(String nombreUsuario) {
        String sql = "SELECT id FROM credenciales WHERE nombre = ?";

        Connection conexion = conexionBD.getConnection();
        if (conexion == null) {
            System.err.println("No existe conexion.");
            return false;
        }

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); 
            }

        } catch (SQLException e) {
            System.err.println("Error comprobando la existencia del usuario " + e.getMessage());
            return false;
        }
    }

}
