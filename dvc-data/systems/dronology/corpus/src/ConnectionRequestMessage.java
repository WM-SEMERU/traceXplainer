package edu.nd.dronology.gstation.connector.messages;

import java.io.Serializable;
/**
 * When a new GCS connects to Dronology it sends a {@link ConnectionRequestMessage}.
 * 
 * @author Michael Vierhauser
 *
 */
public class ConnectionRequestMessage extends AbstractUAVMessage<Object> implements Serializable {

	private static final long serialVersionUID = 1502042637906425729L;
	public static final String MESSAGE_TYPE = "connect";

	public ConnectionRequestMessage(String groundstationId) {
		super(MESSAGE_TYPE, groundstationId, groundstationId);
	}

	@Override 
	public void setType(String type) {
		this.type = type;

	}

}
