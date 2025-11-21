package controlador.servicios;

import modelo.Credenciales;
import modelo.Perfil;
import vista.Config;

import java.util.Optional;

import controlador.dao.ConexionBD;
import controlador.dao.CredencialesDAO;

import java.util.Objects;

public class CredencialesService {

	private final CredencialesDAO dao;

	private static final String ADMIN_USER = Config.get("admin.user");
	private static final String ADMIN_PASS = Config.get("admin.pass");

	public CredencialesService() {
		this.dao = new CredencialesDAO(ConexionBD.getInstance());
	}

	//metodo para hacer login
	public Optional<Credenciales> login(String nombre, String password) {
		Objects.requireNonNull(nombre);
		Objects.requireNonNull(password);

		// para ver si es admin o no
		if (ADMIN_USER != null && ADMIN_PASS != null && ADMIN_USER.equals(nombre) && ADMIN_PASS.equals(password)) {

			return Optional.of(new Credenciales(0L, nombre, password, Perfil.ADMIN));
		}

		// sino buscar en la base de datos
		return dao.obtenerPorNombreYPassword(nombre, password);
	}
}
