package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import modelo.Persona;

public class PersonaDAO {

    private final ConexionBD conexionBD;

    public PersonaDAO(ConexionBD conexionBD) {
        this.conexionBD = conexionBD;
        
    }

 // metodo que verifica si se repite el email
 
    public boolean existeEmail(String email) {
        String sql = "SELECT id FROM persona WHERE email = ?";

        try (PreparedStatement ps = conexionBD.getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("Error verificando email: " + e.getMessage());
            return false;
        }
    }

    //  metodo para insertar persona
    
    public long insertarPersona(Persona p) {
        String sql = """
            INSERT INTO persona (id_credenciales, email, nombre, nacionalidad)
            VALUES (?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conexionBD.getConnection()
                    .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, p.getIdCredenciales());
            ps.setString(2, p.getEmail());
            ps.setString(3, p.getNombre());
            ps.setString(4, p.getNacionalidad());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getLong(1);

        } catch (SQLException e) {
            System.err.println("Error insertando a la persona: " + e.getMessage());
        }

        return -1;
    }

    //metodo que obtiene a la persona por su id
  
    public Persona obtenerPorId(Long idPersona) {

        String sql = """
            SELECT id, id_credenciales, email, nombre, nacionalidad 
            FROM persona 
            WHERE id = ?
        """;

        try (PreparedStatement ps = conexionBD.getConnection().prepareStatement(sql)) {

            ps.setLong(1, idPersona);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    return new Persona(
                        rs.getLong("id"),
                        rs.getLong("id_credenciales"),
                        rs.getString("email"),
                        rs.getString("nombre"),
                        rs.getString("nacionalidad")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo a la persona: " + e.getMessage());
        }

        return null;
    }
    
    public boolean actualizarDatosPersonales(Long idPersona, String nombre, String email, String nacionalidad) {
        String sql = """
            UPDATE persona
            SET nombre = ?, email = ?, nacionalidad = ?
            WHERE id = ?
        """;

        try (PreparedStatement ps = conexionBD.getConnection().prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, email);
            ps.setString(3, nacionalidad);
            ps.setLong(4, idPersona);

            int updated = ps.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            System.err.println("Error actualizando datos personales: " + e.getMessage());
            return false;
        }
    }

    // metodo para comprobar si email existe para otra persona 
    public boolean existeEmailParaOtraPersona(String email, Long idPersona) {
        String sql = "SELECT id FROM persona WHERE email = ? AND id <> ?";

        try (PreparedStatement ps = conexionBD.getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setLong(2, idPersona);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error verificando el email para otra persona: " + e.getMessage());
            return false;
        }
    }
    
    //metodo para obtener a todas las personas
    public List<Persona> obtenerTodasLasPersonas() {

        List<Persona> lista = new ArrayList<>();

        String sql = """
            SELECT id, id_credenciales, email, nombre, nacionalidad
            FROM persona
            ORDER BY id
        """;

        try (PreparedStatement ps = conexionBD.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Persona p = new Persona(
                        rs.getLong("id"),
                        rs.getLong("id_credenciales"),
                        rs.getString("email"),
                        rs.getString("nombre"),
                        rs.getString("nacionalidad")
                );

                lista.add(p);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo la lista de personas: " + e.getMessage());
        }

        return lista;
    }

}