package controlador.servicios;

import modelo.Credenciales;

// servicio de sesion para "controlarla"
public class SesionService {
    private static Credenciales usuarioActual = null;
    
    
    public static void iniciarSesion(Credenciales usuario) {
        usuarioActual = usuario;
    }

    public static void cerrarSesion() {
        usuarioActual = null;
        
    }

    public static boolean isLogged() {
        return usuarioActual != null;
    }

    public static Credenciales getUsuario() {
        return usuarioActual;
    }
}
