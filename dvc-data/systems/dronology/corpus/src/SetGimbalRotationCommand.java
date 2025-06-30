package edu.nd.dronology.core.vehicle.commands;

import edu.nd.dronology.core.CoordinateChange;
import edu.nd.dronology.core.coordinate.LlaCoordinate;

@CoordinateChange
public class SetGimbalRotationCommand extends AbstractDroneCommand implements IDroneCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3345889656265155714L;

	public SetGimbalRotationCommand(String uavid, LlaCoordinate coord) {
		super(uavid, "");
	}

}
