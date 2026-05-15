package edu.nd.dronology.core.flight;

import java.io.Serializable;
import java.util.List;

import edu.nd.dronology.core.Discuss;
import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.core.exceptions.FlightZoneException;
import edu.nd.dronology.core.util.Waypoint;
import edu.nd.dronology.core.vehicle.ManagedDrone;

@Discuss(discuss = "this interface is currently exposed - i.e. managed drone is exposed ")
public interface IFlightPlan extends Serializable {

	ManagedDrone getAssignedDrone();

	boolean setStatusToCompleted() throws FlightZoneException;

	LlaCoordinate getStartLocation();

	LlaCoordinate getEndLocation();

	void clearAssignedDrone();

	String getFlightID();

	boolean setStatusToFlying(ManagedDrone drone) throws FlightZoneException;

	List<Waypoint> getWayPoints();

	long getStartTime();

	long getEndTime();

	String getDesignatedDroneId();

	boolean isCompleted();

	double getTakeoffAltitude();

	void setDesignatedDroneId(String uavid);

}
