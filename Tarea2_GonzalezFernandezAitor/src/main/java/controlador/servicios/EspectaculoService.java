/**
* Clase EspectaculoService.java
* @author AITOR GONZÁLEZ FERNÁNDEZ
* @version 1.0
*/

package controlador.servicios;


import modelo.Espectaculo;
import modelo.Numero;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import controlador.dao.ArtistaDAO;
import controlador.dao.ConexionBD;
import controlador.dao.CoordinacionDAO;
import controlador.dao.EspectaculoDAO;
import controlador.dao.NumeroDAO;
import controlador.dao.PersonaDAO;

public class EspectaculoService {


    private final EspectaculoDAO espectaculoDAO;
    private final NumeroDAO numeroDAO;
    private final ConexionBD conexionBD;
    private final CoordinacionDAO coordinacionDAO;

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public EspectaculoService(ConexionBD conexionBD) {

        this.conexionBD = conexionBD;

        Connection connection = conexionBD.getConnection();

        
        PersonaDAO personaDAO = new PersonaDAO(conexionBD);
        ArtistaDAO artistaDAO = new ArtistaDAO(connection, personaDAO);

        this.numeroDAO = new NumeroDAO(connection, conexionBD);
        this.numeroDAO.setArtistaDAO(artistaDAO);

        //para cargar numeros del espectaculo
        this.espectaculoDAO = new EspectaculoDAO(connection, conexionBD, numeroDAO);

        
        this.espectaculoDAO.setArtistaDAO(artistaDAO);

        this.coordinacionDAO = new CoordinacionDAO(conexionBD);
    }

    public CoordinacionDAO getCoordinacionDAO() {
        return this.coordinacionDAO;
    }

    //CU1
    public List<Espectaculo> obtenerTodos() {
        return espectaculoDAO.obtenerTodos();
    }

    //CU 4
    public void mostrarEspectaculoCompleto(long id) {

        try {
            Espectaculo esp = espectaculoDAO.buscarPorId(id);

            if (esp == null) {
                System.out.println("No se encontro espectaculo con ID " + id);
                return;
            }

            System.out.println("\n===============================================");
            System.out.println("        INFORME DEL ESPECTACULO");
            System.out.println("===============================================");
            System.out.println("ID: " + esp.getId());
            System.out.println("Nombre: " + esp.getNombre());
            System.out.println("Fechas: " + esp.getFechaini().format(fmt) + " - " + esp.getFechafin().format(fmt));

            //coordinador
            System.out.println("\n------------ COORDINACION -------------");

            CoordinacionService coordService =
                    new CoordinacionService(new CoordinacionDAO(conexionBD));

            var coord = coordService.obtenerCoordinadorPorId(esp.getIdCoord());

            if (coord == null) {
                System.out.println("No se encontro coordinador.");
            } else {
                System.out.println("Nombre: " + coord.getNombre());
                System.out.println("Email: " + coord.getEmail());
                System.out.println("Senior: " + (coord.isSenior() ? "Si" : "No"));
            }

            // numeros
            System.out.println("\n---------------- NUMEROS ----------------");

            Set<Numero> numeros = esp.getNumeros();

            if (numeros == null || numeros.isEmpty()) {
                System.out.println("No hay numeros registrados.");
                return;
            }

            numeros.stream()
                    .sorted(Comparator.comparingInt(Numero::getOrden))
                    .forEach(num -> {

                        System.out.println("\nNumero ID: " + num.getId());
                        System.out.println("Orden: " + num.getOrden());
                        System.out.println("Nombre: " + num.getNombre());
                        System.out.println("Duracion: " + num.getDuracion() + " min");

                        System.out.println("Artistas participantes:");

                        if (num.getArtistas() == null || num.getArtistas().isEmpty()) {
                            System.out.println("   (sin artistas)");
                            return;
                        }

                        num.getArtistas().forEach(art -> {

                            System.out.println("  - " + art.getNombre());
                            System.out.println("    Email: " + art.getEmail());
                            System.out.println("    Nacionalidad: " + art.getNacionalidad());

                            if (!art.getEspecialidades().isEmpty()) {
                                String espStr = art.getEspecialidades()
                                        .stream()
                                        .map(Enum::name)
                                        .reduce((a, b) -> a + ", " + b)
                                        .orElse("N/A");

                                System.out.println("    Especialidades: " + espStr);
                            } else {
                                System.out.println("    Especialidades: N/A");
                            }

                            String apodo = (art.getApodo() == null || art.getApodo().isBlank())
                                    ? "N/A"
                                    : art.getApodo();

                            System.out.println("    Apodo: " + apodo);
                        });
                    });

            System.out.println("\n===============================================");

        } catch (Exception e) {
            System.err.println("Error mostrando el espectaculo completo: " + e.getMessage());
        }
    }

    // CU5 A
    public long crearEspectaculo(String nombre, LocalDate fechaini, LocalDate fechafin,
                                 long idCoord, List<Numero> numeros) {

        try {
            if (nombre == null || nombre.isBlank() || nombre.length() > 25) {
                System.out.println("El nombre del espectaculo no es valido.");
                return -1;
            }

            if (espectaculoDAO.existeNombre(nombre)) {
                System.out.println("El nombre ya esta en uso.");
                return -1;
            }

            long dias = ChronoUnit.DAYS.between(fechaini, fechafin);
            if (dias < 0 || dias > 365) {
                System.out.println("El periodo no puede superar 1 año.");
                return -1;
            }

            if (numeros == null || numeros.size() < 3) {
                System.out.println("Debe tener mínimo 3 números.");
                return -1;
            }

            if (!coordinacionDAO.existeCoordinador(idCoord)) {
                System.out.println("El coordinador no existe.");
                return -1;
            }

            Connection conn = conexionBD.getConnection();
            conn.setAutoCommit(false);

            long idEspect = espectaculoDAO.insertar(nombre, fechaini, fechafin, idCoord);
            if (idEspect <= 0) {
                conn.rollback();
                return -1;
            }

            int orden = 1;
            for (Numero n : numeros) {
                numeroDAO.insertarNumero(idEspect, orden++, n.getNombre(), n.getDuracion());
            }

            conn.commit();
            return idEspect;

        } catch (Exception e) {
            System.out.println("Error creando espectaculo: " + e.getMessage());
            return -1;
        }
    }

    // CU5 A
    public boolean actualizarEspectaculo(Espectaculo e) {

        try {
            if (e.getNombre() == null || e.getNombre().isBlank() || e.getNombre().length() > 25)
                return false;

            long dias = ChronoUnit.DAYS.between(e.getFechaini(), e.getFechafin());
            if (dias < 0 || dias > 365)
                return false;

            return espectaculoDAO.actualizar(e);

        } catch (Exception ex) {
            System.out.println("Error actualizando espectaculo: " + ex.getMessage());
            return false;
        }
    }

    //CU5 B
    public boolean reemplazarNumeros(long idEspectaculo, List<Numero> nuevosNumeros) {

        if (nuevosNumeros == null || nuevosNumeros.size() < 3) {
            System.out.println("Debe haber al menos 3 numeros.");
            return false;
        }

        return numeroDAO.reemplazarNumeros(idEspectaculo, nuevosNumeros);
    }

    //CU5 B
    public void actualizarNumero(long idNumero, int orden, String nombre, double duracion) {

        try {
            if (nombre == null || nombre.isBlank()) {
                System.out.println("Nombre inválido.");
                return;
            }

            if (duracion <= 0) {
                System.out.println("La duración debe > 0.");
                return;
            }

            numeroDAO.actualizarNumero(idNumero, orden, nombre, duracion);

        } catch (Exception e) {
            System.out.println("Error actualizando numero: " + e.getMessage());
        }
    }

    // CU5 C
    public void asignarArtistas(long idNumero, Set<Long> idArtistas) {

        try {
            if (idArtistas == null || idArtistas.isEmpty()) {
                System.out.println("Debe seleccionar artistas.");
                return;
            }

            numeroDAO.asignarArtistas(idNumero, idArtistas);

        } catch (Exception e) {
            System.out.println("Error asignando artistas: " + e.getMessage());
        }
    }

    //busca un espectaculo por su id
    public Espectaculo obtenerPorId(long id) {
        return espectaculoDAO.buscarPorId(id);
    }

}