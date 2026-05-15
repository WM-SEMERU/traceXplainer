package edu.nd.dronology.core.fleet;

import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.core.exceptions.DroneException;
import edu.nd.dronology.core.monitoring.DronologyMonitoringManager;
import edu.nd.dronology.core.monitoring.MessageMarshaller;
import edu.nd.dronology.core.monitoring.messages.UAVMonitorableMessage.MessageType;
import edu.nd.dronology.core.vehicle.IDrone;
import edu.nd.dronology.core.vehicle.ManagedDrone;
import edu.nd.dronology.core.vehicle.internal.VirtualDrone;
/**
 * Factory class for initializing a new {@link VirtualDrone} instance.
 * 
 * @author Jane Cleland-Huang 
 *
 */
public class VirtualDroneFleetFactory extends AbstractDroneFleetFactory {

	protected VirtualDroneFleetFactory() {

	} 

	private static volatile VirtualDroneFleetFactory INSTANCE = null;

	public static VirtualDroneFleetFactory getInstance() {
		if (INSTANCE == null) {
			synchronized (VirtualDroneFleetFactory.class) {
				if (INSTANCE == null) {
					INSTANCE = new VirtualDroneFleetFactory();
				}
			}
		}
		return INSTANCE; 
	}

	@Override
	public ManagedDrone initializeDrone(String droneID, String droneType, double latitude, double longitude,
			double altitude) throws DroneException {
		IDrone drone = new VirtualDrone(createDroneID(droneID));
		ManagedDrone managedDrone = new ManagedDrone(drone);

		drone.setManagedDrone(managedDrone);
		LlaCoordinate currentPosition = new LlaCoordinate(latitude, longitude, altitude);
		drone.setBaseCoordinates(currentPosition);
		drone.setCoordinates(currentPosition.getLatitude(), currentPosition.getLongitude(),
				currentPosition.getAltitude());
		managedDrone.start(); 
		DroneFleetManager.getInstance().addDrone(managedDrone);
		DronologyMonitoringManager.getInstance().publish(MessageMarshaller
				.createMessage(MessageType.VIRTUAL_UAV_ACTIVATED, drone.getDroneName(), currentPosition));

		return managedDrone;
	}

}
