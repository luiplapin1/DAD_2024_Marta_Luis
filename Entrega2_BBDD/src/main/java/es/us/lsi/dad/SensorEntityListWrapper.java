package es.us.lsi.dad;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SensorEntityListWrapper {
    private List<Sensor_humedad_Entity> sensorList;

    public SensorEntityListWrapper() {
        super();
    }

    public SensorEntityListWrapper(Collection<Sensor_humedad_Entity> sensorList) {
        super();
        this.sensorList = new ArrayList<>(sensorList);
    }

    public SensorEntityListWrapper(List<Sensor_humedad_Entity> sensorList) {
        super();
        this.sensorList = new ArrayList<>(sensorList);
    }

    public List<Sensor_humedad_Entity> getSensorList() {
        return sensorList;
    }

    public void setSensorList(List<Sensor_humedad_Entity> sensorList) {
        this.sensorList = sensorList;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sensorList == null) ? 0 : sensorList.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SensorEntityListWrapper other = (SensorEntityListWrapper) obj;
        if (sensorList == null) {
            if (other.sensorList != null)
                return false;
        } else if (!sensorList.equals(other.sensorList))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SensorEntityListWrapper [sensorList=" + sensorList + "]";
    }
}
