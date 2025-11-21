/**
 * Clase ArtistaService.java
 * @author AITOR GONZÁLEZ FERNÁNDEZ
 * @version 1.0
 */

package controlador.servicios;

import java.sql.Connection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import controlador.dao.ArtistaDAO;
import controlador.dao.ConexionBD;
import controlador.dao.EspectaculoDAO;
import controlador.dao.NumeroDAO;
import controlador.dao.PersonaDAO;
import modelo.Artista;
import modelo.Especialidad;
import modelo.Espectaculo;
import modelo.Numero;

public class ArtistaService {

	private final ArtistaDAO artistaDAO;
	private final NumeroDAO numeroDAO;
	private final EspectaculoDAO espectaculoDAO;

	public ArtistaService(ConexionBD conexionBD) {
		Connection connection = conexionBD.getConnection();
		PersonaDAO personaDAO = new PersonaDAO(conexionBD);

		this.artistaDAO = new ArtistaDAO(connection, personaDAO);

		this.numeroDAO = new NumeroDAO(connection, conexionBD);
		this.numeroDAO.setArtistaDAO(artistaDAO);

		this.espectaculoDAO = new EspectaculoDAO(connection, conexionBD, numeroDAO);
	}

	public List<Artista> obtenerTodos() {
		return artistaDAO.obtenerTodos();
	}

	public Artista obtenerPorId(long id) {
		return artistaDAO.obtenerPorId(id);
	}

	public ArtistaDAO getDAO() {
		return artistaDAO;
	}

	// metodo que muestra la ficha del artista
	public void verFichaArtista(long idArtista) {

		Artista artista = artistaDAO.obtenerPorId(idArtista);

		if (artista == null) {
			System.out.println("No se encontro el artista.");
			return;
		}

		System.out.println("\n====================================");
		System.out.println("         FICHA DEL ARTISTA");
		System.out.println("====================================");

		System.out.println("ID Artista: " + artista.getIdArt());
		System.out.println("Nombre completo: " + artista.getNombre());
		System.out.println("Email: " + artista.getEmail());
		System.out.println("Nacionalidad: " + artista.getNacionalidad());
		System.out.println(
				"Apodo: " + (artista.getApodo() == null || artista.getApodo().isBlank() ? "N/A" : artista.getApodo()));

		System.out.print("Especialidades: ");
		if (artista.getEspecialidades() == null || artista.getEspecialidades().isEmpty()) {
			System.out.println("N/A");
		} else {
			System.out.println(
					artista.getEspecialidades().stream().map(Enum::name).reduce((a, b) -> a + ", " + b).orElse("N/A"));
		}

		// para obtener los numeros en los que participo un artista
		System.out.println("\n----------- TRAYECTORIA -----------");

		List<Numero> numeros = numeroDAO.obtenerNumerosDeArtista(idArtista);

		if (numeros.isEmpty()) {
			System.out.println("Este artista no ha participado en ningun numero.");
			return;
		}

		Map<Long, List<Numero>> porEspectaculo = numeros.stream()
				.collect(Collectors.groupingBy(Numero::getIdEspectaculo));

		for (Map.Entry<Long, List<Numero>> entry : porEspectaculo.entrySet()) {

			Long idEsp = entry.getKey();
			Espectaculo esp = espectaculoDAO.buscarPorId(idEsp);

			if (esp == null)
				continue;

			System.out.println("\nEspectaculo:");
			System.out.println("  ID: " + esp.getId());
			System.out.println("  Nombre: " + esp.getNombre());

			List<Numero> nums = entry.getValue().stream().sorted(Comparator.comparingInt(Numero::getOrden)).toList();

			System.out.println("  Numeros en los que participo:");

			for (Numero n : nums) {
				System.out.println("    - Numero ID: " + n.getId());
				System.out.println("      Nombre: " + n.getNombre());
				System.out.println("      Orden: " + n.getOrden());
			}
		}

		System.out.println("====================================\n");
	}

	// metodo que actualiza los artistas
	public boolean actualizarArtista(Long idPersona, String apodo, Set<Especialidad> especialidades) {
		return artistaDAO.actualizarArtista(idPersona, apodo, especialidades);
	}

	public Set<Especialidad> convertirEspecialidades(String linea) {
		Set<Especialidad> set = new HashSet<>();
		for (String s : linea.split(",")) {
			set.add(Especialidad.valueOf(s.trim().toUpperCase()));
		}
		return set;
	}
}
