package edu.nd.dronology.core.flight.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.nd.dronology.core.Discuss;
import edu.nd.dronology.core.DronologyConstants;
import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.core.exceptions.FlightZoneException;
import edu.nd.dronology.core.flight.IFlightPlan;
import edu.nd.dronology.core.util.FormatUtil;
import edu.nd.dronology.core.util.Waypoint;
import edu.nd.dronology.core.vehicle.ManagedDrone;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

/**
 * Stores flight information including its waypoints and current status.
 * 
 * @author Jane Cleland-Huang
 * @version 0.1
 *
 */
public class SimpleTakeoffFlightPlan implements IFlightPlan {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3288935042986922882L;

	private transient static final ILogger LOGGER = LoggerProvider.getLogger(SimpleTakeoffFlightPlan.class);

	private static int flightNumber = 0;
	private String flightID;

	private Status status;
	private transient ManagedDrone drone = null;

	private long startTime = -1;
	private long endTime = -1;
	private String uavid;

	private double altitude;

	private List<Waypoint> wayPoints = new ArrayList<>();

	private enum Status {
		PLANNED, FLYING, COMPLETED, ON_HOLD;

		@Override
		public String toString() {
			return name().charAt(0) + name().substring(1).toLowerCase();
		}

	}

	public SimpleTakeoffFlightPlan(ManagedDrone drone, String planName, double altitude) {
		this.altitude = altitude;
		if (altitude > DronologyConstants.MAX_ALTITUDE) {
			altitude = DronologyConstants.MAX_ALTITUDE;
			LOGGER.warn("Altitude override - Takeoff altitude exceeding max altitude of "
					+ DronologyConstants.MAX_ALTITUDE + "m");
		}
		this.drone = drone; 
		this.uavid = drone.getDroneName();
		LlaCoordinate current = drone.getCoordinates();
		wayPoints.add(new Waypoint(new LlaCoordinate(current.getLatitude(), current.getLongitude(), altitude)));
		this.flightID = "DF-" + Integer.toString(++flightNumber) + " - " + planName;
		status = Status.PLANNED;

	}

	/**
	 * 
	 * @return flight ID
	 */
	@Override
	public String getFlightID() {
		return flightID;
	}

	/**
	 * 
	 * @return Starting Coordinates
	 */
	@Override
	public LlaCoordinate getStartLocation() {
		return null;
	}

	/**
	 * 
	 * @return Ending Coordinates
	 */
	@Override
	public LlaCoordinate getEndLocation() {
		return null;
	}

	/**
	 * Returns the drone assigned to the flight plan. Will return null if no drone
	 * is yet assigned.
	 * 
	 * @return iDrone
	 */
	@Override
	public ManagedDrone getAssignedDrone() {
		return drone;
	}

	@Override
	public void clearAssignedDrone() {
		drone = null;
	}

	/**
	 * 
	 * @param drone
	 * @return true if drone is currently flying, false otherwise.
	 * @throws FlightZoneException
	 */
	@Override
	public boolean setStatusToFlying(ManagedDrone drone) throws FlightZoneException {
		if (status == Status.PLANNED) {
			status = Status.FLYING;
			startTime = System.currentTimeMillis();
			this.drone = drone;
			LOGGER.missionInfo("Flight Plan '" + getFlightID() + "'" + drone.getDroneName() + "' started ");
			return true;
		} else
			throw new FlightZoneException("Only currently planned flights can have their status changed to flying");
	}

	/**
	 * Sets flightplan status to completed when called.
	 * 
	 * @return true
	 * @throws FlightZoneException
	 */
	@Override
	public boolean setStatusToCompleted() throws FlightZoneException {
		if (status == Status.FLYING) {
			status = Status.COMPLETED;
			endTime = System.currentTimeMillis();
			LOGGER.missionInfo("Flight Plan '" + getFlightID() + "'" + drone.getDroneName() + "' completed "
					+ FormatUtil.formatTimestamp(startTime) + "-" + FormatUtil.formatTimestamp(endTime));
			return true; // success (may add real check here later)
		} else
			throw new FlightZoneException("Only currently flying flights can have their status changed to completed");
	}

	/**
	 * Returns current flightplan status (Planned, Flying, Completed)
	 * 
	 * @return status
	 */
	public String getStatus() {

		return status.toString();

	}

	@Override
	public String toString() {
		return flightID + "\n" + getStartLocation() + " - " + getEndLocation() + "\n" + getStatus();
	}

	/**
	 * Returns way points
	 * 
	 * @return List<Waypoint>
	 */
	@Override
	public List<Waypoint> getWayPoints() {
		return Collections.unmodifiableList(wayPoints);
	}

	/**
	 * Returns start time of flight.
	 * 
	 * @return date object
	 */
	@Override
	public long getStartTime() {
		return startTime;
	}

	/**
	 * REturns end time of flight.
	 * 
	 * @return date object
	 */
	@Override
	public long getEndTime() {
		return endTime;
	}

	@Override
	public String getDesignatedDroneId() {
		return uavid;
	}

	@Override
	@Discuss(discuss = "unessecary double check of plan complete.. needs to be fixed")
	public boolean isCompleted() {
		return status == Status.COMPLETED || getWayPoints().get(0).isReached();
	}

	@Override
	public double getTakeoffAltitude() {
		return altitude;
	}
 
	@Override
	public void setDesignatedDroneId(String uavid) {
		this.uavid=uavid;
		
	}
	
}
