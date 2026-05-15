package edu.nd.dronology.core.vehicle.commands;

public class EmergencyStopCommand extends AbstractDroneCommand implements IDroneCommand {

	/**
	 * 
	 * Command sent to the GCS for stopping immediately and remain hovering in place.
	 * 
	 * 
	 * @author Michael Vierhauser
	 */
	private static final long serialVersionUID = -8748426132223249721L;

	public EmergencyStopCommand(String droneId) {
		super(droneId, CommandIds.STOP_COMMAND);
	}

}
