package edu.nd.dronology.core.vehicle.commands;

import java.io.Serializable;
/**
 * Interface for all UAV commands.
 * 
 * @author Michael Vierhauser
 *
 */
public interface IDroneCommand extends Serializable {

	public static final String ATTRIBUTE_FREQUENCY = "frequency";
	public static final String ATTRIBUTE_ALTITUDE = "altitude";
	public static final String ATTRIBUTE_SPEED = "speed";
	public static final String ATTRIBUTE_MODE = "mode";

	public static final String ATTRIBUTE_X = "x";
	public static final String ATTRIBUTE_Y = "y";
	public static final String ATTRIBUTE_Z = "z";

	public static final String ATTRIBUTE_SUCCESS = "success";

	String toJsonString();

	String getUAVId();

	void timestamp();

}
