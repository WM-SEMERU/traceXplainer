package edu.nd.dronology.core.fleet;

import edu.nd.dronology.core.Discuss;
import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.core.exceptions.DroneException;
import edu.nd.dronology.core.monitoring.DronologyMonitoringManager;
import edu.nd.dronology.core.monitoring.MessageMarshaller;
import edu.nd.dronology.core.monitoring.messages.UAVMonitorableMessage.MessageType;
import edu.nd.dronology.core.vehicle.IDrone;
import edu.nd.dronology.core.vehicle.IDroneCommandHandler;
import edu.nd.dronology.core.vehicle.ManagedDrone;
import edu.nd.dronology.core.vehicle.internal.PhysicalDrone;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

/**
 * Factory class for initializing a new {@link PhysicalDrone} instance.
 *  
 * @author Jane Cleland-Huang
 *  
 */
public class PhysicalDroneFleetFactory extends AbstractDroneFleetFactory {
 
	private static final ILogger LOGGER = LoggerProvider.getLogger(PhysicalDroneFleetFactory.class);

	private static volatile PhysicalDroneFleetFactory INSTANCE = null;
	private IDroneCommandHandler commandHandler;

	protected PhysicalDroneFleetFactory() {
	}

	public void setCommandHandler(IDroneCommandHandler commandHandler) {
		this.commandHandler = commandHandler;
	}

	public static PhysicalDroneFleetFactory getInstance() {
		if (INSTANCE == null) {
			synchronized (PhysicalDroneFleetFactory.class) {
				if (INSTANCE == null) { 
					INSTANCE = new PhysicalDroneFleetFactory();
				}
			}
		}
		return INSTANCE;
	}

	@Override
	@Discuss(discuss = "todo: fligh to altitude 10... workaround just for testing purposes... needs to be fixed..")
	public ManagedDrone initializeDrone(String droneID, String droneType, double latitude, double longitude,
			double altitude) throws DroneException {

		String[] ids = droneID.split(":"); 
		if (ids.length != 2) {
			throw new DroneException("Invalid drone id '" + droneID + "' --> - droneid:groundstationid");
		}
		String drnId = ids[0];
		String groundstationid = ids[1];

		IDrone drone = new PhysicalDrone(createDroneID(drnId),
				RuntimeDroneTypes.getInstance().getCommandHandler(groundstationid));
		ManagedDrone managedDrone = new ManagedDrone(drone);
		drone.setManagedDrone(managedDrone);

		LlaCoordinate currentPosition = new LlaCoordinate(latitude, longitude, 10);
		LOGGER.info("Drone initialized at: " + currentPosition.toString());
		DronologyMonitoringManager.getInstance().publish(MessageMarshaller
				.createMessage(MessageType.PHYSICAL_UAV_ACTIVATED, drone.getDroneName(), currentPosition));

		drone.setBaseCoordinates(currentPosition);
		drone.setCoordinates(currentPosition.getLatitude(), currentPosition.getLongitude(), currentPosition.getAltitude());
		managedDrone.start();
		DroneFleetManager.getInstance().addDrone(managedDrone);
		return managedDrone;
	}
}