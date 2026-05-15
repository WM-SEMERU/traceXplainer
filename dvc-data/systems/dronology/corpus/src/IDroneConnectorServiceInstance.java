package edu.nd.dronology.gstation.connector.service.connector;

import edu.nd.dronology.gstation.connector.GroundStationException;
import edu.nd.dronology.gstation.connector.GroundstationConnector;
import edu.nd.dronology.gstation.connector.messages.ConnectionRequestMessage;
import edu.nd.dronology.services.core.api.IServiceInstance;

public interface IDroneConnectorServiceInstance extends IServiceInstance {

	void unregisterConnection(String groundstationid) throws GroundStationException;

	void handleConnection(GroundstationConnector handler);

	void registerConnection(GroundstationConnector connector, ConnectionRequestMessage msg)
			throws GroundStationException;

}
