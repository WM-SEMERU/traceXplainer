package edu.nd.dronology.gstation.connector.messages;

import java.io.Serializable;

/**
 * Monitoring message received from the GCS for a specific UAV.
 * 
 * @author Michael Vierhauser
 *
 */
public class UAVModeChangeMessage extends AbstractUAVMessage<Object> implements Serializable {

	private static final long serialVersionUID = 1502042637906425729L;
	public static final String MESSAGE_TYPE = "modechange";
	public static final String DATA = "data";
	public static final transient String MODE = "mode";
	public static final transient String GS_ID = "groundstationid";
	public static final transient String UAV_ID = "uavid";

	public UAVModeChangeMessage(String groundstationid, String uavid, String mode) {
		super(MESSAGE_TYPE, groundstationid, uavid);
		data.put(MODE, mode);
	}

	@Override
	public void setType(String type) {
		this.type = type;

	}

	public String getMode() {
		return data.get(MODE).toString();
	}

}
