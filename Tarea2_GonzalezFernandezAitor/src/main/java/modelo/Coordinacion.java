package modelo;

import java.time.LocalDate;

public class Coordinacion extends Persona {

	private Long idCoord;
	private boolean senior = false;
	private LocalDate fechasenior = null;

	private Long idPersona;

	public Coordinacion(Long idPersona, String nombre, String email, String nacionalidad, Long idCoord, boolean senior,
			LocalDate fechasenior) {

		super(idPersona, nombre, email, nacionalidad);
		this.idPersona = idPersona;
		this.idCoord = idCoord;
		this.senior = senior;
		this.fechasenior = fechasenior;
	}

	public Long getIdPersona() {
		return idPersona;
	}

	public void setIdPersona(Long idPersona) {
		this.idPersona = idPersona;
	}

	// datos que nos interesa guardar
	public Coordinacion(Long id, String nombre, String email, String nacionalidad) {
		super(id, nombre, email, nacionalidad);
		// TODO Auto-generated constructor stub
	}

	public Long getIdCoord() {
		return idCoord;
	}

	public void setIdCoord(Long idCoord) {
		this.idCoord = idCoord;
	}

	public boolean isSenior() {
		return senior;
	}

	public void setSenior(boolean senior) {
		this.senior = senior;
	}

	public LocalDate getFechasenior() {
		return fechasenior;
	}

	public void setFechasenior(LocalDate fechasenior) {
		this.fechasenior = fechasenior;
	}

}
