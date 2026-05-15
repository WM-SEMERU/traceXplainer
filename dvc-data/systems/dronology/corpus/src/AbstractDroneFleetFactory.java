package edu.nd.dronology.core.fleet;

import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.core.exceptions.DroneException;
import edu.nd.dronology.core.vehicle.ManagedDrone;

/**
 * Abstract factory class for drone fleet factory
 * 
 * @author Jane
 * 
 */
public abstract class AbstractDroneFleetFactory {
//	private final List<ManagedDrone> drones = new ArrayList<>();

	public AbstractDroneFleetFactory() {
	}

	protected String createDroneID(String droneID) {
		return droneID;
	}

	/**
	 * Returns list of drones
	 * 
	 * @return array list of iDrones
	 * @throws DroneException 
	 */
//	public List<ManagedDrone> getDrones() {
//		return drones;
//	}

	@Deprecated
	abstract public ManagedDrone initializeDrone(String DroneID, String DroneType, double latitude, double longitude,
			double altitude) throws DroneException;

	public void initializeDrone(String id, String type, LlaCoordinate initialLocation) throws DroneException {
		initializeDrone(id, type, initialLocation.getLatitude(), initialLocation.getLongitude(),
				initialLocation.getAltitude());

	}

}
