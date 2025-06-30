package edu.nd.dronology.core.vehicle.commands;

public class SetGroundSpeedCommand extends AbstractDroneCommand implements IDroneCommand {

	/**
	 * 
	 * Command sent to the GCS for setting the groundspeed of the UAV.<br>
	 * Ground speed is specified in m/s.
	 * 
	 *  
	 * @author Michael Vierhauser
	 */
	private static final long serialVersionUID = -7987143017453423246L;

	public SetGroundSpeedCommand(String uavid, double speed) {
		super(uavid, CommandIds.SET_GROUND_SPEED_COMMAND);
		data.put(ATTRIBUTE_SPEED, speed);
	}
}
