package edu.nd.dronology.gstation.connector.messages;

import java.io.Serializable;

/**
 * Monitoring message received from the GCS for a specific UAV.
 * 
 * @author Michael Vierhauser
 *
 */
public class UAVMonitoringMessage extends AbstractUAVMessage<Object> implements Serializable {

	private static final long serialVersionUID = 1502042637906425729L;
	public static final String MESSAGE_TYPE = "monitoring";

	public UAVMonitoringMessage(String messagetype, String groundstationid, String uavid) {
		super(MESSAGE_TYPE, groundstationid, uavid);
	}

}
