package controlador.servicios;

import modelo.Credenciales;

// servicio de sesion para "controlarla"
public class SesionService {
	private static Credenciales usuarioActual = null;

	// metodo para iniciar sesion
	public static void iniciarSesion(Credenciales usuario) {
		usuarioActual = usuario;
	}

	// metodo que cierra sesion
	public static void cerrarSesion() {
		usuarioActual = null;

	}

	// metodo para comprobar si esta logeado
	public static boolean isLogged() {
		return usuarioActual != null;
	}

	// metodo que devuelve las credenciales del usuario
	public static Credenciales getUsuario() {
		return usuarioActual;
	}
}
