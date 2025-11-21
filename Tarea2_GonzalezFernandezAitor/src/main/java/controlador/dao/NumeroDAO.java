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

import modelo.Artista;
import modelo.Numero;

public class NumeroDAO {

    private final Connection conex;
    private ArtistaDAO artistaDAO;

    public NumeroDAO(Connection conex, ConexionBD conexionBD) {
        this.conex = conex;
    }

    public void setArtistaDAO(ArtistaDAO artistaDAO) {
        this.artistaDAO = artistaDAO;
    }

    //metodo para insertar numero dentro de un espectaculo
    public long insertarNumero(long idEspectaculo, int orden, String nombre, double duracion) {

        String sql = """
            INSERT INTO numeros (id_espectaculo, orden, nombre, duracion)
            VALUES (?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conex.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, idEspectaculo);
            ps.setInt(2, orden);
            ps.setString(3, nombre);
            ps.setDouble(4, duracion);

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getLong(1);

        } catch (SQLException e) {
            System.err.println(" Error insertando numero: " + e.getMessage());
        }
        return -1;
    }

    // metodo para obtener los numeros de un espectaculo por la id del espectaculo
    public Set<Numero> obtenerNumerosDeEspectaculo(Long idEspectaculo) {

        String sql = """
                SELECT id, orden, nombre, duracion
                FROM numeros
                WHERE id_espectaculo = ?
                ORDER BY orden
        """;

        Set<Numero> numeros = new HashSet<>();

        try (PreparedStatement ps = conex.prepareStatement(sql)) {

            ps.setLong(1, idEspectaculo);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                long idNumero = rs.getLong("id");
                Set<Artista> artistas = obtenerArtistasDeNumero(idNumero);

                Numero num = new Numero(
                        idNumero,
                        rs.getInt("orden"),
                        rs.getString("nombre"),
                        rs.getDouble("duracion"),
                        artistas
                );

                num.setIdEspectaculo(idEspectaculo);
                numeros.add(num);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo numeros: " + e.getMessage());
        }

        return numeros;
    }

    // metodo para obtener los artistas que estan en un numero por la id del numero
    private Set<Artista> obtenerArtistasDeNumero(Long idNumero) {

        String sql = """
                SELECT idArt
                FROM artista_numero
                WHERE id = ?
        """;

        Set<Artista> artistas = new HashSet<>();

        try (PreparedStatement ps = conex.prepareStatement(sql)) {

            ps.setLong(1, idNumero);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                long idArtista = rs.getLong("idArt");

                if (artistaDAO == null) return artistas;

                Artista art = artistaDAO.obtenerPorId(idArtista);

                if (art != null) artistas.add(art);
            }

        } catch (SQLException e) {
            System.err.println("[NumeroDAO] Error obteniendo artistas: " + e.getMessage());
        }

        return artistas;
    }

    // metodo para obtener los numeros con la id del artista
    public List<Numero> obtenerNumerosDeArtista(Long idArtista) {

        String sql = """
            SELECT n.id, n.nombre, n.duracion, n.orden, n.id_espectaculo
            FROM artista_numero an
            JOIN numeros n ON an.id = n.id
            WHERE an.idArt = ?
            ORDER BY n.orden
        """;

        List<Numero> lista = new ArrayList<>();

        try (PreparedStatement ps = conex.prepareStatement(sql)) {

            ps.setLong(1, idArtista);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Numero num = new Numero(
                        rs.getLong("id"),
                        rs.getInt("orden"),
                        rs.getString("nombre"),
                        rs.getDouble("duracion"),
                        new HashSet<>()
                );

                num.setIdEspectaculo(rs.getLong("id_espectaculo"));
                lista.add(num);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo numeros del artista: " + e.getMessage());
        }

        return lista;
    }

    // metodo para actualizar numeros
    public boolean actualizarNumero(long idNumero, int orden, String nombre, double duracion) {
        String sql = """
            UPDATE numeros
            SET orden = ?, nombre = ?, duracion = ?
            WHERE id = ?
        """;

        try (PreparedStatement ps = conex.prepareStatement(sql)) {

            ps.setInt(1, orden);
            ps.setString(2, nombre);
            ps.setDouble(3, duracion);
            ps.setLong(4, idNumero);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error actualizando numero: " + e.getMessage());
        }

        return false;
    }

    // metodo para borrar numeros dentro de un espectaculo
    public void borrarNumerosDeEspectaculo(long idEspectaculo) {
        String sql = "DELETE FROM numeros WHERE id_espectaculo = ?";

        try (PreparedStatement ps = conex.prepareStatement(sql)) {
            ps.setLong(1, idEspectaculo);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error borrando numeros: " + e.getMessage());
        }
    }

    // metodo para asignar artistas a un numero
    public void asignarArtistas(long idNumero, Set<Long> idArtistas) {

        // validar numero
        String sqlCheckNum = "SELECT COUNT(*) FROM numeros WHERE id = ?";

        try (PreparedStatement ps = conex.prepareStatement(sqlCheckNum)) {
            ps.setLong(1, idNumero);
            ResultSet rs = ps.executeQuery();

            if (!rs.next() || rs.getInt(1) == 0) {
                System.out.println("El numero con ID " + idNumero + " no existe.");
                return;
            }
        } catch (SQLException e) {
            System.out.println("Error validando numero: " + e.getMessage());
            return;
        }

        if (artistaDAO == null) {
            System.out.println("Error: ArtistaDAO no esta configurado en NumeroDAO.");
            return;
        }

        // validar artista
        Set<Long> artistasValidos = new HashSet<>();

        for (Long idArt : idArtistas) {
            Artista a = artistaDAO.obtenerPorId(idArt);

            if (a == null) {
                System.out.println("El artista con ID " + idArt + " no existe. Se ignora.");
            } else {
                artistasValidos.add(idArt);
            }
        }

        if (artistasValidos.isEmpty()) {
            System.out.println("No hay artistas validos para asignar.");
            return;
        }

       
        String borrar = "DELETE FROM artista_numero WHERE id = ?";
        String insertar = "INSERT INTO artista_numero (idArt, id) VALUES (?, ?)";

        try {

            try (PreparedStatement ps = conex.prepareStatement(borrar)) {
                ps.setLong(1, idNumero);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conex.prepareStatement(insertar)) {

                for (Long idVal : artistasValidos) {
                    ps.setLong(1, idVal);
                    ps.setLong(2, idNumero);
                    ps.addBatch();
                }

                ps.executeBatch();
            }

        } catch (SQLException e) {
            System.err.println("Error asignando artistas: " + e.getMessage());
        }
    }
    public boolean reemplazarNumeros(long idEspectaculo, List<Numero> nuevosNumeros) {

        if (nuevosNumeros == null || nuevosNumeros.size() < 3) {
            System.out.println("Debe haber al menos 3 numeros.");
            return false;
        }

        try {

            conex.setAutoCommit(false);

            // borrar artistas 
            String borrarArtistas = """
                DELETE FROM artista_numero
                WHERE id IN (SELECT id FROM numeros WHERE id_espectaculo = ?)
            """;

            try (PreparedStatement ps = conex.prepareStatement(borrarArtistas)) {
                ps.setLong(1, idEspectaculo);
                ps.executeUpdate();
            }

            // borrar numeros 
            String borrarNumeros = "DELETE FROM numeros WHERE id_espectaculo = ?";

            try (PreparedStatement ps = conex.prepareStatement(borrarNumeros)) {
                ps.setLong(1, idEspectaculo);
                ps.executeUpdate();
            }

            // insertar los números nuevos
            int orden = 1;
            for (Numero n : nuevosNumeros) {
                insertarNumero(idEspectaculo, orden++, n.getNombre(), n.getDuracion());
            }

            conex.commit();
            return true;

        } catch (Exception e) {

            try { conex.rollback(); } catch (Exception ignored) {}

            System.out.println("[NumeroDAO] Error reemplazando numeros: " + e.getMessage());
            return false;

        } finally {
            try { conex.setAutoCommit(true); } catch (Exception ignored) {}
        }
    }

}
