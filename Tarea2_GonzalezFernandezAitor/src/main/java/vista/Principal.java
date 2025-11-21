package vista;


import dao.ConexionBD;
import servicios.SesionService;

public class Principal {

    public static void main(String[] args) {
        
       

        
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
        ConexionBD.getInstance().cerrarConexion();

        System.out.println("Programa finalizado.");
    }
        
    }

