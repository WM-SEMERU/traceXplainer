package edu.nd.dronology.core.vehicle.commands;

import edu.nd.dronology.core.coordinate.LlaCoordinate;

/**
 * 
 * Command sent to the GCS for sending a new waypoint.
 * 
 * 
 * @author Michael Vierhauser
 */
public class GoToCommand extends AbstractDroneCommand implements IDroneCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4420565617484849238L;

	public GoToCommand(String uavid, LlaCoordinate coord) {
		super(uavid, CommandIds.GOTO_LOCATION_COMMAND);
		data.put(ATTRIBUTE_X, coord.getLatitude());
		data.put(ATTRIBUTE_Y, coord.getLongitude());
		data.put(ATTRIBUTE_Z, coord.getAltitude());
	}

}
