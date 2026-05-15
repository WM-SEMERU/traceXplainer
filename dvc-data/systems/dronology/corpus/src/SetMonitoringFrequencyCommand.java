package edu.nd.dronology.core.vehicle.commands;

/**
 * 
 * Command sent to the GCS for adjusting the monitoring frequency.<br>
 * The frequency is sent in ms.
 *  
 * @author Michael Vierhauser
 */
public class SetMonitoringFrequencyCommand extends AbstractDroneCommand implements IDroneCommand {

	private static final long serialVersionUID = 4075558216191030210L;

	public SetMonitoringFrequencyCommand(String droneId, long frequency) {
		super(droneId, CommandIds.SET_MONITOR_FREQUENCY_COMMAND);
		data.put(ATTRIBUTE_FREQUENCY, frequency);
	}

}
