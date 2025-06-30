package edu.nd.dronology.core.vehicle.commands;

/**
 * 
 * Command sent to the GCS for adjusting the state frequency.<br>
 * The frequency is sent in ms. 
 * 
 * @author Michael Vierhauser
 */
public class SetStateFrequencyCommand extends AbstractDroneCommand implements IDroneCommand {

	private static final long serialVersionUID = -3014662856674586911L;

	protected SetStateFrequencyCommand(String droneId, long frequency) {
		super(droneId, CommandIds.SET_STATE_FREQUENCY_COMMAND);
		data.put(ATTRIBUTE_FREQUENCY, frequency);
	}

}
