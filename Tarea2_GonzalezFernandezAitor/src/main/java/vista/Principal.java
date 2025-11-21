package vista;


import controlador.dao.ConexionBD;
import controlador.servicios.SesionService;

public class Principal {

    public static void main(String[] args) {
        
       
    	//para inicializar conexion
        
        try {
            ConexionBD.getInstance().getConnection();
        } catch (Exception e) {
            System.err.println("No se pudo inicializar la conexión: " + e.getMessage());
        }

        
        boolean seguir = true;

        do {

            if (!SesionService.isLogged()) {

                boolean salir = Menu.menuInvitado(); 

                if (salir) {
                    seguir = false;
                }

            } else {
                Menu.mostrarMenuPorPerfil(); 
            }

        } while (seguir);
        //para cerrar la conexion en la base de datos
        ConexionBD.getInstance().cerrarConexion();

        System.out.println("Programa finalizado.");
    }
        
    }

