package edu.nd.dronology.core.vehicle.commands;

public class SetVelocityCommand extends AbstractDroneCommand implements IDroneCommand {

	/**
	 * 
	 * Command sent to the GCS for setting a velocity vector to the UAV.<br>
	 * The vector is specified in NED (north,east,down).
	 * 
	 * 
	 * @author Michael Vierhauser
	 */
	private static final long serialVersionUID = 2864106869131015180L;

	public SetVelocityCommand(String uavid, double x, double y, double z) {
		super(uavid, CommandIds.SET_VELOCITY_COMMAND);
		data.put(ATTRIBUTE_X, x);
		data.put(ATTRIBUTE_Y, y);
		data.put(ATTRIBUTE_Z, z);
	}

}
