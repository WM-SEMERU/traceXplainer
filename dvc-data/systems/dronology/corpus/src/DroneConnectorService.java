package edu.nd.dronology.gstation.connector.service.connector;

import edu.nd.dronology.gstation.connector.GroundStationException;
import edu.nd.dronology.gstation.connector.GroundstationConnector;
import edu.nd.dronology.gstation.connector.messages.ConnectionRequestMessage;
import edu.nd.dronology.services.core.base.AbstractServerService;

public class DroneConnectorService extends AbstractServerService<IDroneConnectorServiceInstance> {

	private static volatile DroneConnectorService INSTANCE;

	protected DroneConnectorService() {
	}

	/**
	 * @return The singleton ConfigurationService instance
	 */
	public static DroneConnectorService getInstance() {
		if (INSTANCE == null) {
			synchronized (DroneConnectorService.class) {
				if (INSTANCE == null) {
					INSTANCE = new DroneConnectorService();
				}
			}
		}
		return INSTANCE;
	}

	@Override
	protected IDroneConnectorServiceInstance initServiceInstance() {
		return new DroneConnectorServiceInstance();
	}

	public void handleConnection(GroundstationConnector connector) {
		serviceInstance.handleConnection(connector);

	}

	public void registerConnection(GroundstationConnector connector, ConnectionRequestMessage msg)
			throws GroundStationException {
		serviceInstance.registerConnection(connector, msg);

	}

	public void unregisterConnection(String groundstationid) throws GroundStationException {
		serviceInstance.unregisterConnection(groundstationid); 

	}

}
