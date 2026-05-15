package edu.nd.dronology.core.vehicle.commands;

public class TakeoffCommand extends AbstractDroneCommand implements IDroneCommand {

	/**
	 * 
	 * Command sent to the GCS for taking off to a given alitude.<br>
	 * The altitude is specified in meter.
	 * 
	 * 
	 * @author Michael Vierhauser
	 */ 
	private static final long serialVersionUID = 7724695547149579828L;

	public TakeoffCommand(String droneId, double altitude) {
		super(droneId, CommandIds.TAKEOFF_COMMAND);
		data.put(ATTRIBUTE_ALTITUDE, altitude);
	}

}
