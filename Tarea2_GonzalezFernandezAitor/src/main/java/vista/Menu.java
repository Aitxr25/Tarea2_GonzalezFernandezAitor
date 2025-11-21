package vista;

import controlador.dao.ConexionBD;
import controlador.servicios.ArtistaService;
import controlador.servicios.SesionService;

public class Menu {

	// menu invitado
	public static boolean menuInvitado() {

		MetodosVista mv = new MetodosVista();
		int opcion;
		boolean salir = false;

		do {
			System.out.println("\n--- MENU (INVITADO) ---");
			System.out.println("1. Ver espectáculos");
			System.out.println("2. Iniciar sesión");
			System.out.println("3. Salir del programa");

			opcion = MetodosVista.pedirEntero("Opción: ");

			switch (opcion) {

			case 1:
				MetodosVista.verEspectaculos();
				break;

			case 2:
				mv.mostrarLogin();
				if (SesionService.isLogged())
					return false;
				break;

			case 3:
				SesionService.cerrarSesion();

				return true;

			default:
				System.out.println("La opción no es válida.");
				break;
			}

		} while (!salir);
		return true;
	}

	// menu segun perfil
	public static void mostrarMenuPorPerfil() {

		switch (SesionService.getUsuario().getPerfil()) {

		case ADMIN:
			menuAdmin();
			break;

		case COORDINADOR:
			menuCoordinacion();
			break;

		case ARTISTA:
			menuArtista();
			break;

		default:
			System.out.println("Perfil desconocido.");
			break;
		}
	}

	// menu del admin
	private static void menuAdmin() {

		int opcion;
		boolean salir = false;

		do {
			System.out.println("\n--- MENU ADMIN ---");
			System.out.println("1. Registrar persona");
			System.out.println("2. Gestionar personas");
			System.out.println("3. Ver espectaculo completo");
			System.out.println("4. Gestionar espectaculos");
			System.out.println("5. Cerrar sesion");

			opcion = MetodosVista.pedirEntero("Opción: ");

			switch (opcion) {

			case 1:
				MetodosVista.registrarPersona();
				break;

			case 2:
				Menu.mostrarMenuGestion();
				break;

			case 3:
				MetodosVista.verEspectaculoCompleto();
				break;

			case 4:
				MetodosVista.gestionarEspectaculos();
				break;

			case 5:
				System.out.println("Cerrando sesión...");
				SesionService.cerrarSesion();
				salir = true;
				break;

			default:
				System.out.println("La opción no es válida");
				break;
			}

		} while (!salir);
	}

	// menu coordinador
	private static void menuCoordinacion() {

		int opcion;
		boolean salir = false;

		do {
			System.out.println("\n--- MENU COORDINACION ---");
			System.out.println("1. Ver espectaculos completos");
			System.out.println("2. Gestionar espectaculos");
			System.out.println("3. Cerrar sesion");

			opcion = MetodosVista.pedirEntero("Opcion: ");

			switch (opcion) {

			case 1:
				MetodosVista.verEspectaculoCompleto();
				break;

			case 2:
				MetodosVista.gestionarEspectaculos();
				break;

			case 3:
				SesionService.cerrarSesion();
				System.out.println("Sesion cerrada.");
				salir = true;
				break;

			default:
				System.out.println("La opcion no es valida");
				break;
			}

		} while (!salir);
	}

	// menu del artista
	private static void menuArtista() {

		ArtistaService artistaService = new ArtistaService(ConexionBD.getInstance());
		int opcion = -1;

		do {
			System.out.println("\n--- MENU ARTISTA ---");
			System.out.println("1. Ver espectaculos completos");
			System.out.println("2. Ver mi ficha");
			System.out.println("3. Cerrar sesion");

			opcion = MetodosVista.pedirEntero("Opcion: ");

			switch (opcion) {

			case 1:
				MetodosVista.verEspectaculoCompleto();
				break;

			case 2:
				Long idCred = SesionService.getUsuario().getIdCredenciales();
				Long idArt = artistaService.getDAO().obtenerIdArtPorIdCredenciales(idCred);

				if (idArt == null) {
					System.out.println("Error: No se ha encontrado su perfil de artista.");
					break;
				}

				artistaService.verFichaArtista(idArt);
				break;

			case 3:
				SesionService.cerrarSesion();
				System.out.println("Sesion cerrada.");
				return;

			default:
				System.out.println("La opcion no es valida");
				break;
			}

		} while (true);
	}

	// menu para la gestion de personas
	public static void mostrarMenuGestion() {

		int opcion = -1;

		do {
			System.out.println("\n--- MENU GESTION DE PERSONAS ---");
			System.out.println("1. Modificar datos personales");
			System.out.println("2. Modificar datos de artista");
			System.out.println("3. Modificar datos de coordinador");
			System.out.println("4. Volver al menu del administrador");

			opcion = MetodosVista.pedirEntero("Opcion: ");

			switch (opcion) {

			case 1:
				MetodosVista.modificarDatosPersonales();
				break;

			case 2:
				MetodosVista.modificarDatosArtista();
				break;

			case 3:
				MetodosVista.modificarDatosCoordinador();
				break;

			case 4:
				return;

			default:
				System.out.println("La opcion no es valida");
				break;
			}

		} while (true);
	}

}
