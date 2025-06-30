package edu.nd.dronology.gstation.connector.messages;

import java.io.Serializable;
import java.util.Map;

import edu.nd.dronology.core.coordinate.LlaCoordinate;

public class UAVHandshakeMessage extends AbstractUAVMessage<Object> implements Serializable {

	private static final long serialVersionUID = 1502042637906425729L;
	public static final String MESSAGE_TYPE = "handshake";
	public static final String HOME = "home";
	public static final String SAFETY_CASE = "safetycase";

	public UAVHandshakeMessage(String groundstationid, String uavid) {
		super(MESSAGE_TYPE, groundstationid, uavid); 
	}

	public LlaCoordinate getHome() {
		if (data.get(HOME) instanceof LlaCoordinate) {
			return (LlaCoordinate) data.get(HOME);
		}
		Map<String, Double> homeMap = (Map<String, Double>) data.get(HOME);
		data.put(HOME, new LlaCoordinate(homeMap.get("x"), homeMap.get("y"), homeMap.get("z")));
		return (LlaCoordinate) data.get(HOME);
	}

	public String getSafetyCase() {
		return data.get(SAFETY_CASE) != null ? data.get(SAFETY_CASE).toString() : null;
	}

	public void setHome(LlaCoordinate coordinate) {
		data.put(HOME, coordinate);

	}

}
