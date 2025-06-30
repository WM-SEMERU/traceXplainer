package edu.nd.dronology.services.core.items;

import edu.nd.dronology.core.CoordinateChange;
import edu.nd.dronology.core.coordinate.LlaCoordinate;

public class AssignedDrone {

	public final String droneName;
	public LlaCoordinate startCoordinate = new LlaCoordinate(0, 0, 0);

	public AssignedDrone(String droneName) {
		super();
		this.droneName = droneName;
	}

	public LlaCoordinate getStartCoordinate() {
		return startCoordinate;
	}

	@CoordinateChange
	public void setStartCoordinate(double latitude, double longitude, double altitude) {
		startCoordinate = new LlaCoordinate(latitude, longitude, altitude);

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((droneName == null) ? 0 : droneName.hashCode());
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
		AssignedDrone other = (AssignedDrone) obj;
		if (droneName == null) {
			if (other.droneName != null)
				return false;
		} else if (!droneName.equals(other.droneName))
			return false;
		return true;
	}

	public String getName() {
		return droneName;
	}

}
