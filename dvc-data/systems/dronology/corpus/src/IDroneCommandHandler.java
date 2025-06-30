package edu.nd.dronology.core.vehicle;

import edu.nd.dronology.core.IUAVPropertyUpdateNotifier;
import edu.nd.dronology.core.exceptions.DroneException;
import edu.nd.dronology.core.vehicle.commands.IDroneCommand;

public interface IDroneCommandHandler {
	
	
	public void sendCommand(IDroneCommand command) throws DroneException;

	void setStatusCallbackNotifier(String id, IUAVPropertyUpdateNotifier listener) throws DroneException;
	
	public String getHandlerId();

}
