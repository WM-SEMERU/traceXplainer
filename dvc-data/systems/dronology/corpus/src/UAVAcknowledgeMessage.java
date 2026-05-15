package edu.nd.dronology.gstation.connector.messages;

import java.io.Serializable;

public class UAVAcknowledgeMessage extends AbstractUAVMessage<Object> implements Serializable {

	private static final long serialVersionUID = 1502042637906425729L;
	public static final String MESSAGE_TYPE = "ack";

	public UAVAcknowledgeMessage(String messagetype, String groundstationid, String uavid) {
		super(MESSAGE_TYPE, groundstationid, uavid);
	}

}
