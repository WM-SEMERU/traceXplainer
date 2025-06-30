package edu.nd.dronology.core.vehicle.commands;

import edu.nd.dronology.core.CoordinateChange;
import edu.nd.dronology.core.coordinate.LlaCoordinate;

@CoordinateChange
public class SetGimbalTargetCommand extends AbstractDroneCommand implements IDroneCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = 364759212900805189L;

	public SetGimbalTargetCommand(String uavid, LlaCoordinate coord) {
		super(uavid, "");

	}

}
