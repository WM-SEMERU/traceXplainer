package edu.nd.dronology.services.core.info;

import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.util.NullUtil;

public class DroneInitializationInfo extends RemoteInfoObject {

	
	public enum DroneMode {
		MODE_VIRTUAL, MODE_PHYSICAL; 

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 3002533064742692033L;
	private String type;
	private LlaCoordinate initialLocation;
	private DroneMode mode;
	private String safetyInformation;



	public DroneInitializationInfo(String id, DroneMode mode, String type, LlaCoordinate initialLocation) {
		super(id, id);
		NullUtil.checkNull(type, initialLocation,mode);
		this.type = type;
		this.mode= mode;
		this.initialLocation = initialLocation;

	}

	public DroneMode getMode() {
		return mode;
	}

	public String getType() {
		return type;
	}

	public LlaCoordinate getInitialLocation() {
		return initialLocation;
	}
	
	public String getSafetyInformation() {
		return safetyInformation;
	}

	public void setSafetyInformation(String safetyInformation) {
		this.safetyInformation = safetyInformation;
	}

}
