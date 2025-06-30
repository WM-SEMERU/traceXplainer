package edu.nd.dronology.core.vehicle;

import edu.nd.dronology.core.collisionavoidance.DroneSnapshotInternal;
import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.core.exceptions.DroneException;
import edu.nd.dronology.core.exceptions.FlightZoneException;
import edu.nd.dronology.core.vehicle.commands.AbstractDroneCommand;
import edu.nd.dronology.core.vehicle.proxy.UAVProxy;


/**
 * iDrone interface
 * 
 * @author Jane Cleland-Huang
 * @version 0.01
 *
 */
public interface IDrone {

	public UAVProxy getDroneStatus();

	/**
	 * 
	 * @return latitude of drone
	 */
	public double getLatitude();

	/**
	 * 
	 * @return longitude of drone
	 */
	public double getLongitude();

	/**
	 * 
	 * @return altitude of drone
	 */
	public double getAltitude();

	/**
	 * Fly drone to target coordinates
	 * 
	 * @param targetCoordinates
	 * @param speed
	 */
	public void flyTo(LlaCoordinate targetCoordinates, Double speed);

	/**
	 * 
	 * @return current coordinates
	 */
	public LlaCoordinate getCoordinates();

	/**
	 * 
	 * @return unique name of drone
	 */
	public String getDroneName();

	/**
	 * Land the drone. Update status.
	 * 
	 * @throws FlightZoneException
	 */
	void land() throws FlightZoneException;

	/**
	 * Takeoff. Update status.
	 * 
	 * @throws FlightZoneException
	 */
	void takeOff(double altitude) throws FlightZoneException;

	/**
	 * Sets drones coordinates
	 * 
	 * @param lat
	 *          latitude
	 * @param lon
	 *          Longitude
	 * @param alt
	 *          Altitude
	 */
	public void setCoordinates(double lat, double lon, double alt);

	public double getBatteryStatus();

	public boolean move(double i);

	public void setVoltageCheckPoint();

	public boolean isDestinationReached(int distanceMovedPerTimeStep);

	void setBaseCoordinates(LlaCoordinate basePosition);

	public LlaCoordinate getBaseCoordinates();

	public void setGroundSpeed(double speed);

	public void setVelocity(double x, double y, double z);

	void setCoordinates(LlaCoordinate coordinate);

	public void sendCommand(AbstractDroneCommand command) throws DroneException;

	public DroneSnapshotInternal getLatestDroneSnapshot();

	public void setManagedDrone(ManagedDrone managedDrone);

	public void resendCommand() throws DroneException;

}
