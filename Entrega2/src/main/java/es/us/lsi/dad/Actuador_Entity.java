package es.us.lsi.dad;

import java.util.Calendar;
import java.util.Objects;

public class Actuador_Entity {
	
	protected Integer nPlaca; // clave priamaria
	protected Integer idActuador; 
	protected long timestamp;  //Calendar.getInstance().getTimeInMillis()
	protected boolean activo;
	protected boolean encendido;
	
	public Actuador_Entity() {
		super();	
		timestamp=Calendar.getInstance().getTimeInMillis();
		activo=false;
	}
	
	public Actuador_Entity(Integer idDevise, Integer idActuador, long timestamp, boolean activo, boolean encendido) {
		super();
		this.nPlaca = idDevise;
		this.idActuador = idActuador;
		this.timestamp = timestamp;
		this.activo = activo;
		this.encendido = encendido;
	}

	public Integer getIdDevise() {
		return nPlaca;
	}

	public void setIdDevise(Integer idDevise) {
		this.nPlaca = idDevise;
	}

	public Integer getidActuador() {
		return idActuador;
	}

	public void setidActuador(Integer idActuador) {
		this.idActuador = idActuador;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public boolean getActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public boolean getEncendido() {
		return encendido;
	}

	public void setEncendido(boolean encendido) {
		this.encendido = encendido;
	}

	@Override
	public int hashCode() {
		return Objects.hash(activo, encendido, nPlaca, idActuador, timestamp);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Actuador_Entity other = (Actuador_Entity) obj;
		return activo == other.activo && encendido == other.encendido && Objects.equals(nPlaca, other.nPlaca)
				&& Objects.equals(idActuador, other.idActuador) && timestamp == other.timestamp;
	}

	@Override
	public String toString() {
		return "Actuador_Entity [idDevise=" + nPlaca + ", idSensor=" + idActuador + ", timestamp=" + timestamp
				+ ", activo=" + activo + ", encendido=" + encendido + "]";
	}
	
	
	
}