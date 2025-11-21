package modelo;

public class Persona {
	protected Long id;
	private Long idCredenciales;
	protected String nombre;
	protected String email;
	protected String nacionalidad;

	protected Credenciales credenciales;

	public Persona(Long id, Long idCredenciales, String email, String nombre, String nacionalidad) {
		this.id = id;
		this.idCredenciales = idCredenciales;
		this.email = email;
		this.nombre = nombre;
		this.nacionalidad = nacionalidad;
		this.credenciales = null;
	}

	public Persona(Long id, Long idCredenciales, String email, String nombre, String nacionalidad,
			Credenciales credenciales) {
		this.id = id;
		this.idCredenciales = idCredenciales;
		this.email = email;
		this.nombre = nombre;
		this.nacionalidad = nacionalidad;
		this.credenciales = credenciales;
	}

	// constructor para invitados (sin credenciales)
	public Persona(Long id, String nombre, String email, String nacionalidad) {
		this.id = id;
		this.nombre = nombre;
		this.email = email;
		this.nacionalidad = nacionalidad;
	}

	public Long getIdCredenciales() {
		return idCredenciales;
	}

	public void setIdCredenciales(Long idCredenciales) {
		this.idCredenciales = idCredenciales;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNacionalidad() {
		return nacionalidad;
	}

	public void setNacionalidad(String nacionalidad) {
		this.nacionalidad = nacionalidad;
	}

	public Credenciales getCredenciales() {
		return credenciales;
	}

	public void setCredenciales(Credenciales credenciales) {
		this.credenciales = credenciales;
	}

}
