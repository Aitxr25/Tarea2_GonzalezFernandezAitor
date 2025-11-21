package vista;

import modelo.Artista;
import modelo.Coordinacion;
import modelo.Credenciales;
import modelo.Especialidad;
import modelo.Espectaculo;
import modelo.Numero;
import modelo.Perfil;
import modelo.Persona;
import servicios.ArtistaService;
import servicios.CoordinacionService;
import servicios.CredencialesService;
import servicios.EspectaculoService;
import servicios.PersonaService;
import servicios.RegistroService;
import servicios.SesionService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dao.ConexionBD;
import dao.CoordinacionDAO;

public class MetodosVista {

    private static final Scanner scanner = new Scanner(System.in);
    private static long tempIdNum;
    private static int tempNuevoOrden;

    private final CredencialesService credService = new CredencialesService();

    //CU2
    public void mostrarLogin() {
    	//no se deberia de dar este caso
        if (SesionService.isLogged()) {
            System.out.println("Ya hay una sesion iniciada. Cierra sesión primero."); 
            return;
        }
        System.out.print("Usuario: ");
        String user = scanner.nextLine().trim();
        System.out.print("Contraseña: ");
        String pass = scanner.nextLine();

        if (user.isBlank() || pass.isBlank()) {
            System.out.println("Los campos no pueden estar vacios.");
            return;
        }

        Optional<Credenciales> resultado = credService.login(user, pass);
        if (resultado.isPresent()) {
            SesionService.iniciarSesion(resultado.get());
            System.out.println("Login correcto. Perfil: " + resultado.get().getPerfil());
        } else {
            System.out.println("Credenciales incorrectas.");
        }
    }

    public static int pedirEntero(String solicitud) {
        System.out.print(solicitud);
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            return -1;
        }
    }
    
    //CU1
    public static void verEspectaculos() {

        EspectaculoService service = new EspectaculoService(ConexionBD.getInstance());

        System.out.println("\n===== LISTA DE ESPECTÁCULOS =====");

        List<Espectaculo> lista = service.obtenerTodos();

        if (lista.isEmpty()) {
            System.out.println("No hay espectaculos registrados.");
            return;
        }

        for (Espectaculo e : lista) {
            System.out.println("--------------------------------------------"); 
            System.out.println("ID: " + e.getId());
            System.out.println("Nombre: " + e.getNombre());
            System.out.println("Periodo: " + e.getFechaini() + "  hasta  " + e.getFechafin());
        }

        System.out.println("--------------------------------------------"); 
    }

//archivo xml
public static Map<String, String> cargarPaises() {
	Map<String, String> paises = new HashMap<>();
	Path rutaPaises = Paths.get("src/main/resources/paises.xml");

	try {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(rutaPaises.toFile());

		doc.getDocumentElement().normalize();

		NodeList listaPaises = doc.getElementsByTagName("pais");

		for (int i = 0; i < listaPaises.getLength(); i++) {
			Node nodo = listaPaises.item(i);
			if (nodo.getNodeType() == Node.ELEMENT_NODE) {
				Element elemento = (Element) nodo;

				String id = elemento.getElementsByTagName("id").item(0).getTextContent();
				String nombre = elemento.getElementsByTagName("nombre").item(0).getTextContent();

				paises.put(id, nombre);
			}
		}
	} catch (Exception e) {
		System.out.println("Error al cargar paises desde XML: " + e.getMessage());
	}

	return paises;
}

// metodo para elegir la nacionalidad
public static String seleccionarNacionalidad() {
	Map<String, String> paises = cargarPaises();

	if (paises.isEmpty()) {
		System.out.println("No se pudieron cargar los países.");
		return null;
	}

	System.out.println("Lista de paises disponibles:");
	for (Map.Entry<String, String> entry : paises.entrySet()) {
		System.out.printf("%s - %s%n", entry.getKey(), entry.getValue());
	}

	System.out.print("Introduce el codigo del pais (ID): ");
	String idSeleccionado = scanner.nextLine().trim().toUpperCase();

	if (!paises.containsKey(idSeleccionado)) {
		System.out.println("La ID del pais no es valida.");
		return null;
	}

	return paises.get(idSeleccionado); 
}


// CU 3AB metodo para registrar persona
public static void registrarPersona() {

    RegistroService reg = new RegistroService(ConexionBD.getInstance());
    System.out.println("\n=== REGISTRO DE NUEVA PERSONA ===");

    // validacion nombre real
    String nombreReal;
    do {
        System.out.print("Nombre real: ");
        nombreReal = scanner.nextLine().trim();

        if (nombreReal.isEmpty() ||
            !nombreReal.matches("^[a-zA-ZÁÉÍÓÚáéíóúÑñüÜ\\s]+$")) {
            System.out.println("Nombre inválido.");
        }

    } while (nombreReal.isEmpty() ||
             !nombreReal.matches("^[a-zA-ZÁÉÍÓÚáéíóúÑñüÜ\\s]+$"));


    // validacion email
    String email;
    do {
        System.out.print("Email: ");
        email = scanner.nextLine().trim();

        boolean formatoCorrecto = email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
        boolean existe = reg.existeEmail(email);

        if (!formatoCorrecto || existe) {
            System.out.println("Email invalido o ya registrado.");
        }

        if (existe) return;

    } while (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"));


    // seleccionar nacionalidad
    String nacionalidad;
    do {
        nacionalidad = seleccionarNacionalidad();
        if (nacionalidad == null) System.out.println("Nacionalidad invalida.");
    } while (nacionalidad == null);


    // credenciales - usuario
    String usuario;
    do {
        System.out.print("Nombre de usuario (sera guardado en minusculas): ");
        usuario = scanner.nextLine().trim().toLowerCase();

        boolean ok = usuario.matches("^[a-z]+$") && usuario.length() >= 3;
        boolean existe = reg.existeUsuario(usuario);

        if (!ok || existe) {
            System.out.println("Usuario invalido o ya existente.");
        }

        if (existe) return;

    } while (!usuario.matches("^[a-z]+$") || usuario.length() < 3);


    // credenciales - contraseña
    String password;
    do {
        System.out.print("Contraseña (mín. 3 caracteres, sin espacios): ");
        password = scanner.nextLine().trim();

        if (password.length() < 3 || password.contains(" ")) {
            System.out.println("Contraseña invalida.");
        }

    } while (password.length() < 3 || password.contains(" "));


    // seleccion perfil
    Perfil perfil = null;
    do {
        System.out.print("Perfil (ARTISTA / COORDINADOR): ");
        String pf = scanner.nextLine().trim().toUpperCase();

        try {
            perfil = Perfil.valueOf(pf);
        } catch (Exception e) {
            System.out.println("Perfil invalido.");
        }

    } while (perfil == null);


    // variables coordinador/artista
    boolean esSenior = false;
    LocalDate fechaSenior = null;
    String apodo = null;
    Set<Especialidad> especialidades = new HashSet<>();


    // datos coordinador
    if (perfil == Perfil.COORDINADOR) {

        String respuesta;
        do {
            System.out.print("¿Es senior? (s/n): ");
            respuesta = scanner.nextLine().trim().toLowerCase();

            if (!respuesta.equals("s") && !respuesta.equals("n")) {
                System.out.println("Debes poner s o n.");
            }

        } while (!respuesta.equals("s") && !respuesta.equals("n"));

        esSenior = respuesta.equals("s");

        if (esSenior) {
            boolean fechaOk;
            do {
                fechaOk = true;
                System.out.print("Fecha antigüedad (dd-MM-yyyy): ");
                String fechaStr = scanner.nextLine();

                try {
                    fechaSenior = LocalDate.parse(
                            fechaStr,
                            DateTimeFormatter.ofPattern("dd-MM-yyyy")
                    );
                } catch (Exception e) {
                    System.out.println("Fecha invalida.");
                    fechaOk = false;
                }

            } while (!fechaOk);
        }
    }


    // datos artista
    if (perfil == Perfil.ARTISTA) {

        boolean apodoCorrecto;
        do {
            apodoCorrecto = true;
            System.out.print("Apodo (opcional): ");
            apodo = scanner.nextLine().trim();

            if (!apodo.isEmpty() &&
                !apodo.matches("^[a-zA-ZÁÉÍÓÚáéíóúÑñüÜ\\s]+$")) {
                System.out.println("Apodo invalido.");
                apodoCorrecto = false;
            }

        } while (!apodoCorrecto);

        boolean especialidadesOK;
        System.out.println("Especialidades: ACROBACIA, HUMOR, MAGIA, EQUILIBRISMO, MALABARISMO");

        do {
            especialidades.clear();
            especialidadesOK = true;

            System.out.print("Introduce especialidades separadas por coma: ");
            String linea = scanner.nextLine();

            try {
                for (String s : linea.toUpperCase().split(",")) {
                    especialidades.add(Especialidad.valueOf(s.trim()));
                }

            } catch (Exception e) {
                System.out.println("Una especialidad no es válida.");
                especialidadesOK = false;
            }

        } while (!especialidadesOK);
    }


    // resumen
    System.out.println("\n===== RESUMEN DE DATOS =====");
    System.out.println("Nombre: " + nombreReal);
    System.out.println("Email: " + email);
    System.out.println("Usuario: " + usuario);
    System.out.println("Nacionalidad: " + nacionalidad);
    System.out.println("Perfil: " + perfil);

    if (perfil == Perfil.COORDINADOR) {
        System.out.println("Es senior: " + (esSenior ? "Sí" : "No"));

        if (esSenior && fechaSenior != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            System.out.println("Fecha antigüedad: " + fechaSenior.format(fmt));
        }
    }


    if (perfil == Perfil.ARTISTA) {
        System.out.println("Apodo: " + (apodo.isEmpty() ? "Ninguno" : apodo));
        System.out.println("Especialidades: " +
                especialidades.stream().map(Enum::name).collect(Collectors.joining(", ")));
    }


    // confirmar
    String confirmacion;
    do {
        System.out.print("\n¿Deseas guardar esta persona? (s/n): ");
        confirmacion = scanner.nextLine().trim().toLowerCase();

        if (!confirmacion.equals("s") && !confirmacion.equals("n")) {
            System.out.println("Opción no válida.");
        }

    } while (!confirmacion.equals("s") && !confirmacion.equals("n"));

    if (confirmacion.equals("n")) {
        System.out.println("Registro cancelado por el usuario.");
        return;
    }


    // guardar
    Long idPersona = reg.registrar(
            nombreReal, email, usuario, password,
            nacionalidad, perfil, esSenior,
            fechaSenior, apodo, especialidades
    );

    if (idPersona == null) {
        System.out.println("Error inesperado en el registro.");
        return;
    }

    System.out.println("\nRegistro completado. Nuevo ID = " + idPersona);
}

//CU3C
public static void modificarDatosPersonales() {

    PersonaService personaService = new PersonaService(ConexionBD.getInstance());

    System.out.println("\n===== MODIFICAR DATOS PERSONALES =====");

    List<Persona> personas = personaService.obtenerTodas();

    if (personas.isEmpty()) {
        System.out.println("No hay personas registradas.");
        return;
    }

    for (Persona p : personas) {
        System.out.printf("%d | %s | %s | %s%n",
                p.getId(), p.getNombre(), p.getEmail(), p.getNacionalidad());
    }

    Long id = null;

    do {
        System.out.print("ID de persona: ");
        try {
            id = Long.parseLong(scanner.nextLine().trim());
        } catch (Exception e) {
            System.out.println("ID invalida.");
        }
    } while (id == null);

    Persona p = personaService.obtenerPorId(id);

    if (p == null) {
        System.out.println("No existe persona con ese ID.");
        return;
    }

    // validar nombre
    String nombre;
    do {
        System.out.print("Nombre (" + p.getNombre() + "): ");
        nombre = scanner.nextLine().trim();
        if (nombre.isEmpty()) nombre = p.getNombre();

        if (!nombre.matches("^[a-zA-ZÁÉÍÓÚáéíóúÑñüÜ\\s]+$")) {
            System.out.println("Nombre invalido.");
            nombre = null;
        }

    } while (nombre == null);

    // validar email
    String email;

    do {
        System.out.print("Email (" + p.getEmail() + "): ");
        email = scanner.nextLine().trim();
        if (email.isEmpty()) email = p.getEmail();

        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            System.out.println("Email invalido.");
            email = null;
            continue;
        }

        if (!personaService.emailDisponibleParaOtro(email, p.getId())) {
            System.out.println("Ese email ya pertenece a otra persona.");
            email = null;
        }

    } while (email == null);

    // validar nacionalidad 
    String nac = null;

    do {
        System.out.println("\nNacionalidad actual: " + p.getNacionalidad());
      

        System.out.print("¿Cambiar nacionalidad? (s/n): ");
        String cambio = scanner.nextLine().trim().toLowerCase();

        if (cambio.equals("n") || cambio.isEmpty()) {
            nac = p.getNacionalidad();
            break;
        }

        if (!cambio.equals("s")) {
            System.out.println("Opcion invalida.");
            continue;
        }

        nac = seleccionarNacionalidad();

        if (nac == null) {
            System.out.println("No se selecciono una nacionalidad valida.");
        }

    } while (nac == null);

    // actualizar persona
    boolean ok = personaService.actualizarDatosPersonales(p.getId(), nombre, email, nac);

    System.out.println(ok ? "Datos personales actualizados." : "Error actualizando.");
}

public static void modificarDatosArtista() {

    ConexionBD conex = ConexionBD.getInstance();
    ArtistaService artService = new ArtistaService(conex);
    

    System.out.println("\n===== MODIFICAR DATOS DE ARTISTA =====");

    List<Artista> artistas = artService.obtenerTodos();

    if (artistas.isEmpty()) {
        System.out.println("No hay artistas registrados.");
        return;
    }

    for (Artista a : artistas) {
        String espec = a.getEspecialidades()
                        .stream().map(Enum::name)
                        .collect(Collectors.joining(","));
        System.out.printf("%d | %s | %s | %s%n",
                a.getIdArt(), a.getNombre(), a.getApodo(), espec);
    }

    Long idArt = null;

    do {
        System.out.print("ID del artista: ");
        try {
            idArt = Long.parseLong(scanner.nextLine().trim());
        } catch (Exception e) {
            System.out.println("ID invalida.");
        }
    } while (idArt == null);

    Artista a = artService.obtenerPorId(idArt);

    if (a == null) {
        System.out.println("No existe artista con esa ID.");
        return;
    }

    // apodo
    String apodo;

    do {
        System.out.print("Apodo (" + a.getApodo() + "): ");
        apodo = scanner.nextLine().trim();
        if (apodo.isEmpty()) apodo = a.getApodo();

        if (!apodo.matches("^[a-zA-ZÁÉÍÓÚáéíóúÑñüÜ\\s]*$")) {
            System.out.println("Apodo invalido.");
            apodo = null;
        }

    } while (apodo == null);

    // especialidades
    Set<Especialidad> nuevas = new HashSet<>();
    boolean okEspec;

    do {
        System.out.print("Especialidades separadas por comas (" +
                a.getEspecialidades().stream().map(Enum::name)
                .collect(Collectors.joining(",")) + "): ");

        String linea = scanner.nextLine().trim();

        if (linea.isEmpty()) {
            nuevas = a.getEspecialidades();
            okEspec = true;
            break;
        }

        try {
            nuevas = Arrays.stream(linea.split(","))
                           .map(s -> Especialidad.valueOf(s.trim().toUpperCase()))
                           .collect(Collectors.toSet());
            okEspec = true;
        } catch (Exception e) {
            System.out.println("Especialidad invalida.");
            okEspec = false;
        }

    } while (!okEspec);

    boolean ok = artService.actualizarArtista(a.getId(), apodo, nuevas);

    System.out.println(ok ? "Artista actualizado." : "Error.");
}

public static void modificarDatosCoordinador() {

    CoordinacionService coordService =
            new CoordinacionService(new CoordinacionDAO(ConexionBD.getInstance()));

   

    System.out.println("\n===== MODIFICAR DATOS DE COORDINADOR =====");

    List<Coordinacion> lista = coordService.obtenerTodos();

    if (lista.isEmpty()) {
        System.out.println("No hay coordinadores.");
        return;
    }

    for (Coordinacion c : lista) {
        System.out.printf("%d | %s | senior=%s | fecha=%s%n",
                c.getIdCoord(),
                c.getNombre(),
                c.isSenior() ? "SI" : "NO",
                c.getFechasenior() == null ? "-" : c.getFechasenior());
    }

    Long idCoord = null;

    do {
        System.out.print("ID de coordinador: ");
        try {
            idCoord = Long.parseLong(scanner.nextLine().trim());
        } catch (Exception e) {
            System.out.println("ID invalida.");
        }
    } while (idCoord == null);

    Coordinacion c = coordService.obtenerCoordinadorPorId(idCoord);

    if (c == null) {
        System.out.println("No existe coordinador con ese ID.");
        return;
    }

    // senior
    String sen;
    do {
        System.out.print("¿Es senior? (s/n) [" + (c.isSenior() ? "s" : "n") + "]: ");
        sen = scanner.nextLine().trim().toLowerCase();
        if (sen.isEmpty()) sen = c.isSenior() ? "s" : "n";

    } while (!sen.equals("s") && !sen.equals("n"));

    boolean seniorNuevo = sen.equals("s");

    // fecha
    LocalDate fechaNueva = null;

    if (seniorNuevo) {
        boolean okFecha = false;

        do {
            System.out.print("Fecha antigüedad (dd-MM-yyyy) [" +
                    (c.getFechasenior() == null ? "-" : c.getFechasenior()) + "]: ");

            String f = scanner.nextLine().trim();

            if (f.isEmpty()) {
                fechaNueva = c.getFechasenior();
                okFecha = true;
                break;
            }

            try {
                fechaNueva = LocalDate.parse(f, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                okFecha = true;
            } catch (Exception e) {
                System.out.println("Fecha invalida.");
            }

        } while (!okFecha);
    }

    boolean ok = coordService.actualizarCoordinador(c.getIdPersona(), seniorNuevo, fechaNueva);

    System.out.println(ok ? "Coordinador actualizado." : "Error.");
}

//CU 4 para visualizar un espectaculo completo
public static void verEspectaculoCompleto() {

    EspectaculoService service = new EspectaculoService(ConexionBD.getInstance());
    System.out.println("\n===== ESPECTACULOS DISPONIBLES =====");

    List<Espectaculo> lista = service.obtenerTodos();

    if (lista.isEmpty()) {
        System.out.println("No hay espectaculos registrados.");
        return;
    }

    for (Espectaculo e : lista) {
        System.out.println("ID " + e.getId() + ": " + e.getNombre());
    }

    System.out.print("\nIntroduce el ID: ");
    long id = scanner.nextLong();
    scanner.nextLine();

    Espectaculo esp = service.obtenerPorId(id);

    if (esp == null) {
        System.out.println("No existe un espectaculo con esa ID.");
        return;
    }

    service.mostrarEspectaculoCompleto(id);
}

//CU5 Gestion de espectaculos
public static void gestionarEspectaculos() {

    EspectaculoService service = new EspectaculoService(ConexionBD.getInstance());
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    int opcion;

    do {
        System.out.println("\n========= GESTION DE ESPECTACULOS =========");
        System.out.println("1. Crear espectaculo");
        System.out.println("2. Modificar espectaculo");
        System.out.println("3. Reemplazar numeros");
        System.out.println("4. Modificar un numero");
        System.out.println("5. Asignar artistas a un numero");
        System.out.println("6. Volver");
        System.out.print("Elige una opcion: ");

        try {
            opcion = Integer.parseInt(scanner.nextLine());
        } catch (Exception e) {
            opcion = -1;
        }

        switch (opcion) {

        case 1: {
            try {
                System.out.println("\n--- CREAR ESPECTACULO ---");

                System.out.print("Nombre del espectaculo: ");
                String nombre = scanner.nextLine().trim();

                System.out.print("Fecha inicio (dd-MM-yyyy): ");
                LocalDate ini = LocalDate.parse(scanner.nextLine().trim(), fmt);

                System.out.print("Fecha fin (dd-MM-yyyy): ");
                LocalDate fin = LocalDate.parse(scanner.nextLine().trim(), fmt);

                long idCoord;

                if (SesionService.getUsuario().getPerfil() == Perfil.COORDINADOR) {

                    Long idCoordLog = service.getCoordinacionDAO()
                            .obtenerIdCoordPorIdCredenciales(SesionService.getUsuario().getIdCredenciales());

                    if (idCoordLog == null) {
                        System.out.println("No se encontro su ID de coordinador.");
                        break;
                    }

                    idCoord = idCoordLog;
                    System.out.println("Asignado automaticamente su ID de coordinador: " + idCoord);

                } else {

                    List<Coordinacion> lista = service.getCoordinacionDAO().obtenerTodos();

                    if (lista.isEmpty()) {
                        System.out.println("No hay coordinadores registrados.");
                        break;
                    }

                    System.out.println("\n===== COORDINADORES DISPONIBLES =====");
                    for (Coordinacion c : lista) {
                        System.out.println("IDCoord: " + c.getIdCoord() + " | Nombre: " + c.getNombre());
                    }
                    System.out.println("=====================================");

                    System.out.print("Introduce el ID del coordinador: ");
                    idCoord = Long.parseLong(scanner.nextLine().trim());
                }

                List<Numero> numeros = new ArrayList<>();

                int cantidad;
                do {
                    System.out.print("Cantidad de numeros (minimo 3): ");
                    cantidad = Integer.parseInt(scanner.nextLine());
                } while (cantidad < 3);

                for (int i = 1; i <= cantidad; i++) {
                    System.out.println("\nNumero " + i);

                    System.out.print("Nombre: ");
                    String nom = scanner.nextLine().trim();

                    System.out.print("Duracion (minutos): ");
                    double dur = Double.parseDouble(scanner.nextLine());

                    numeros.add(new Numero(null, i, nom, dur, new HashSet<>()));
                }

                long idNuevo = service.crearEspectaculo(nombre, ini, fin, idCoord, numeros);
                System.out.println("Espectaculo creado con ID = " + idNuevo);

            } catch (Exception e) {
                System.out.println("Error creando espectaculo: " + e.getMessage());
            }
            break;
        }

        //actualizar espectaculo
        case 2: {
            try {
                System.out.println("\n--- ACTUALIZAR ESPECTÁCULO ---");

                // para mostrar los espectaculos disponibles
                List<Espectaculo> lista = service.obtenerTodos();

                if (lista.isEmpty()) {
                    System.out.println("No hay espectaculos registrados.");
                    break;
                }

                System.out.println("\n--- LISTA DE ESPECTÁCULOS ---");
                for (Espectaculo e : lista) {
                    System.out.printf("%d | %s | %s - %s%n",
                            e.getId(),
                            e.getNombre(),
                            e.getFechaini().format(fmt),
                            e.getFechafin().format(fmt)
                    );
                }
                System.out.println("-----------------------------");

              
                System.out.print("ID del espectaculo a actualizar: ");
                long id = Long.parseLong(scanner.nextLine());

                Espectaculo esp = service.obtenerPorId(id);

                if (esp == null) {
                    System.out.println("No existe un espectaculo con esa ID.");
                    break;
                }

                System.out.print("Nuevo nombre (" + esp.getNombre() + "): ");
                String nombre = scanner.nextLine().trim();
                if (!nombre.isBlank()) esp.setNombre(nombre);

                System.out.print("Nueva fecha inicio (" + esp.getFechaini().format(fmt) + "): ");
                String f1 = scanner.nextLine().trim();
                if (!f1.isBlank()) esp.setFechaini(LocalDate.parse(f1, fmt));

                System.out.print("Nueva fecha fin (" + esp.getFechafin().format(fmt) + "): ");
                String f2 = scanner.nextLine().trim();
                if (!f2.isBlank()) esp.setFechafin(LocalDate.parse(f2, fmt));

                boolean ok = service.actualizarEspectaculo(esp);
                System.out.println(ok ? "Espectaculo actualizado." : "Error actualizando.");

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            break;
        }


            //reemplazar los numeros de un espectaculo
        case 3: {
            try {
                System.out.println("\n--- REEMPLAZAR NUMEROS ---");

                
                List<Espectaculo> lista = service.obtenerTodos();

                if (lista.isEmpty()) {
                    System.out.println("No hay espectaculos registrados.");
                    break;
                }

                System.out.println("\n===== ESPECTÁCULOS DISPONIBLES =====");
                for (Espectaculo e : lista) {
                    System.out.println("ID: " + e.getId() + " | Nombre: " + e.getNombre());
                }
                System.out.println("=====================================");

                // pedir id espectaculo
                System.out.print("\nIntroduce el ID del espectaculo: ");
                long id = Long.parseLong(scanner.nextLine().trim());

                Espectaculo esp = service.obtenerPorId(id);
                if (esp == null) {
                    System.out.println("No existe un espectaculo con ese ID.");
                    break;
                }

                // pedir cuantos numeros se desea introducir
                int cantidad = 0;

                while (cantidad < 3) {
                    System.out.print("Cantidad de numeros (mínimo 3): ");
                    try {
                        cantidad = Integer.parseInt(scanner.nextLine().trim());
                        if (cantidad < 3) {
                            System.out.println("Debes introducir al menos 3 numeros.\n");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Introduce un número valido.");
                        cantidad = 0;
                    }
                }

                //crear la nueva lista de numeros
                List<Numero> nuevos = new ArrayList<>();

                for (int i = 1; i <= cantidad; i++) {
                    System.out.println("\nNumero " + i);

                    System.out.print("Nombre: ");
                    String nom = scanner.nextLine().trim();

                    System.out.print("Duracion (minutos): ");
                    double dur = Double.parseDouble(scanner.nextLine().trim());

                    nuevos.add(new Numero(null, i, nom, dur, new HashSet<>()));
                }

                // reemplazarlos
                service.reemplazarNumeros(id, nuevos);
                System.out.println("\nNumeros reemplazados correctamente.");

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

            break;
        }

            // modificar un numero
        case 4: {
            try {
                System.out.println("\n--- MODIFICAR UN NUMERO ---");

                System.out.println("\n===== LISTA DE ESPECTACULOS =====");
                List<Espectaculo> lista = service.obtenerTodos();

                if (lista.isEmpty()) {
                    System.out.println("No hay espectaculos registrados.");
                    break;
                }

                for (Espectaculo e : lista) {
                    System.out.println("--------------------------------------------");
                    System.out.println("ID: " + e.getId());
                    System.out.println("Nombre: " + e.getNombre());
                    System.out.println("Periodo: " + e.getFechaini() + " hasta " + e.getFechafin());
                }

                System.out.print("\nIntroduce el ID del espectaculo: ");
                long idEsp = Long.parseLong(scanner.nextLine().trim());

                Espectaculo esp = service.obtenerPorId(idEsp);
                if (esp == null) {
                    System.out.println("No existe un espectaculo con ese ID.");
                    break;
                }

                System.out.println("\nNumeros del espectaculo:");
                esp.getNumeros().stream()
                        .sorted(Comparator.comparingInt(Numero::getOrden))
                        .forEach(n -> System.out.println(
                                "ID: " + n.getId() +
                                " | Orden: " + n.getOrden() +
                                " | Nombre: " + n.getNombre()
                        ));

                // validar ID
                tempIdNum = -1;
                boolean idValido = false;

                while (!idValido) {
                    System.out.print("\nIntroduce el ID del numero a modificar: ");
                    long candidato = Long.parseLong(scanner.nextLine().trim());

                    boolean existe = esp.getNumeros()
                            .stream()
                            .anyMatch(n -> n.getId() == candidato);

                    if (existe) {
                        tempIdNum = candidato;
                        idValido = true;
                    } else {
                        System.out.println("Ese ID no existe en este espectaculo. Intentalo otra vez.");
                    }
                }

                // validar orden
                tempNuevoOrden = -1;
                boolean ordenValido = false;

                while (!ordenValido) {
                    System.out.print("Nuevo orden: ");
                    tempNuevoOrden = Integer.parseInt(scanner.nextLine().trim());

                    boolean repetido = esp.getNumeros()
                            .stream()
                            .anyMatch(n -> n.getOrden() == tempNuevoOrden && n.getId() != tempIdNum);

                    if (repetido) {
                        System.out.println("Ese orden ya esta en uso. Prueba otro.");
                    } else {
                        ordenValido = true;
                    }
                }

                System.out.print("Nuevo nombre: ");
                String nom = scanner.nextLine().trim();

                System.out.print("Nueva duracion: ");
                double dur = Double.parseDouble(scanner.nextLine().trim());

                service.actualizarNumero(tempIdNum, tempNuevoOrden, nom, dur);

                System.out.println("Numero actualizado correctamente.");

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            break;
        }


            //asignar artistas
        case 5: {
            try {
                System.out.println("\n--- ASIGNAR ARTISTAS A UN NUMERO ---");

                // listar los espectaculos
                List<Espectaculo> espectaculos = service.obtenerTodos();

                if (espectaculos.isEmpty()) {
                    System.out.println("No hay espectaculos registrados.");
                    break;
                }

                System.out.println("\n===== ESPECTÁCULOS =====");
                for (Espectaculo e : espectaculos) {
                    System.out.println("ID: " + e.getId() + " | Nombre: " + e.getNombre());
                }

                System.out.print("\nIntroduce ID del espectáculo: ");
                long idEsp = Long.parseLong(scanner.nextLine());

                Espectaculo esp = service.obtenerPorId(idEsp);
                if (esp == null) {
                    System.out.println("Espectaculo no encontrado.");
                    break;
                }

                //listar los numeros
                if (esp.getNumeros().isEmpty()) {
                    System.out.println("Este espectaculo no tiene números.");
                    break;
                }

                System.out.println("\n===== NUMEROS =====");
                esp.getNumeros()
                        .stream()
                        .sorted(Comparator.comparingInt(Numero::getOrden))
                        .forEach(n ->
                                System.out.println(
                                        "ID: " + n.getId() +
                                                " | Orden: " + n.getOrden() +
                                                " | Nombre: " + n.getNombre()
                                )
                        );

                System.out.print("\nIntroduce el ID del número: ");
                long idNum = Long.parseLong(scanner.nextLine().trim());

                //verificar que ese numero existe dentro del espectaculo
                boolean numeroExiste = esp.getNumeros()
                        .stream()
                        .anyMatch(n -> n.getId() == idNum);

                if (!numeroExiste) {
                    System.out.println("El numero con ID " + idNum +
                            " no pertenece a este espectáculo o no existe.");
                    break;
                }


                //lista los artistas
                ArtistaService artistaService = new ArtistaService(ConexionBD.getInstance());
                List<Artista> artistas = artistaService.obtenerTodos();

                if (artistas.isEmpty()) {
                    System.out.println("No hay artistas registrados.");
                    break;
                }

                System.out.println("\n===== ARTISTAS DISPONIBLES =====");
                for (Artista a : artistas) {
                    System.out.println(
                        "ID del Artista: " + a.getIdArt() +
                        " | Nombre: " + a.getNombre() +
                        (a.getApodo() != null && !a.getApodo().isBlank() ? " | Apodo: " + a.getApodo() : "")
                    );
                }

                System.out.print("\nIntroduce IDs de artistas separados por comas: ");
                String linea = scanner.nextLine().trim();

                Set<Long> ids = new HashSet<>();
                for (String s : linea.split(",")) {
                    ids.add(Long.parseLong(s.trim()));
                }

                // validar los artistas
                Set<Long> idsValidos = new HashSet<>();

                for (Long idArt : ids) {
                    Artista art = artistaService.obtenerPorId(idArt);

                    if (art == null) {
                        System.out.println("El artista con ID " + idArt +
                                " no existe. ");
                    } else {
                        idsValidos.add(idArt);
                    }
                }

                if (idsValidos.isEmpty()) {
                    System.out.println("No se selecciono ningún artista valido.");
                    break;
                }

                // asignar a los artistas
                service.asignarArtistas(idNum, idsValidos);

                System.out.println("Artistas asignados correctamente.");

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            break;
        }

        case 6:
            System.out.println("Volviendo al menu anterior");
            break;

        default:
            System.out.println("Opcion invalida");
            break;
        }

    } while (opcion != 6);
}
}