package edu.nd.dronology.core.vehicle.commands;

public class LandCommand extends AbstractDroneCommand implements IDroneCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8475187449318434454L;
	public static final transient String MODE_LAND = "LAND";

	public LandCommand(String uavid, String mode) {
		super(uavid, CommandIds.SET_MODE_COMMAND);
		data.put(ATTRIBUTE_MODE, mode);
	}

}
