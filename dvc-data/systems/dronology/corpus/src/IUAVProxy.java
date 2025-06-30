package edu.nd.dronology.core.vehicle;

import java.io.Serializable;
import java.util.Map;

import edu.nd.dronology.core.coordinate.LlaCoordinate;

public interface IUAVProxy extends Serializable {

	String getID();

	String getStatus();

	double getLongitude();

	double getLatitude();

	double getAltitude();

	double getBatteryLevel();

	double getVelocity();

	Map<String, String> getInfo();

	LlaCoordinate getCoordinates();

	LlaCoordinate getHomeLocation();

	String getGroundstationId();

}