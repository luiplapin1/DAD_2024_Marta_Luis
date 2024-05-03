package es.us.lsi.dad;

import java.util.Calendar;
import java.util.Objects;


public class Sensor_humedad_Entity {
	
	protected Integer idSensor;
	protected Integer nPlaca;
	protected long timestamp; //Calendar.getInstance().getTimeInMillis()
	protected Float humedad;
	protected Float temperatura;
	protected int idGroup;

	public Sensor_humedad_Entity(Integer idSensor, Integer nPlaca, long timestamp, Float humedad, Float temperatura, int idGroup) {
		super();
		this.idSensor = idSensor;
		this.nPlaca=nPlaca;
		this.timestamp = timestamp;
		this.humedad = humedad;
		this.temperatura = temperatura;
		this.idGroup = idGroup;
	}
	
	public Sensor_humedad_Entity() {
		super();	
		timestamp=Calendar.getInstance().getTimeInMillis();
		humedad=0.0f;
		temperatura= 0.0f;
	}

	public Integer getId() {
		return idSensor;
	}

	public Integer getnPlaca() {
		return nPlaca;
	}

	public void setnPlaca(Integer nPlaca) {
		this.nPlaca = nPlaca;
	}

	public void setId(Integer idSensor) {
		this.idSensor = idSensor;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Float getHumedad() {
		return humedad;
	}

	public void setHumedad(Float humedad) {
		this.humedad = humedad;
	}

	public Float getTemperatura() {
		return temperatura;
	}

	public void setTemperatura(Float temperatura) {
		this.temperatura = temperatura;
	}
	
	public int getIdGroup() {
		return idGroup;
	}
	
	public void setIdGroup(int idGroup) {
		this.idGroup = idGroup;
	}

	@Override
	public int hashCode() {
		return Objects.hash(humedad, idGroup, idSensor, nPlaca, temperatura, timestamp);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Sensor_humedad_Entity other = (Sensor_humedad_Entity) obj;
		return Objects.equals(humedad, other.humedad) && idGroup == other.idGroup
				&& Objects.equals(idSensor, other.idSensor) && Objects.equals(nPlaca, other.nPlaca)
				&& Objects.equals(temperatura, other.temperatura) && timestamp == other.timestamp;
	}

	@Override
	public String toString() {
		return "Sensor_humedad_Entity [idSensor=" + idSensor + ", nPlaca=" + nPlaca + ", timestamp=" + timestamp
				+ ", humedad=" + humedad + ", temperatura=" + temperatura + ", idGroup=" + idGroup + "]";
	}
	

	
	
	
	

}