package edu.nd.dronology.core.flight.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.nd.dronology.core.Discuss;
import edu.nd.dronology.core.DronologyConstants;
import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.core.exceptions.FlightZoneException;
import edu.nd.dronology.core.flight.IFlightPlan;
import edu.nd.dronology.core.monitoring.DronologyMonitoringManager;
import edu.nd.dronology.core.monitoring.MessageMarshaller;
import edu.nd.dronology.core.monitoring.messages.UAVMonitorableMessage.MessageType;
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
public class FlightPlan implements IFlightPlan {

	/**
	 * 
	 */
	private static final long serialVersionUID = -390109273842862492L;

	private static final ILogger LOGGER = LoggerProvider.getLogger(FlightPlan.class);

	private static int flightNumber = 0;
	private String flightID;

	private List<Waypoint> wayPoints;
	private LlaCoordinate startLocation;
	private LlaCoordinate endLocation;
	private Status status;
	private transient ManagedDrone drone = null;

	private long startTime = -1;
	private long endTime = -1;
	private String uavid;

	private enum Status {
		PLANNED, FLYING, COMPLETED, ON_HOLD;

		@Override
		public String toString() {
			return name().charAt(0) + name().substring(1).toLowerCase();
		}

	}

	public FlightPlan(String planName, List<Waypoint> wayPoints) {
		this(null, planName, wayPoints);

	}

	public FlightPlan(String uavid, String planName, List<Waypoint> wayPointsToAdd) {
		this.wayPoints = new ArrayList<>();
		for (Waypoint oldWP : wayPointsToAdd) {
			Waypoint newWP = new Waypoint(oldWP.getCoordinate());
			newWP.setApproachingspeed(oldWP.getApproachingspeed());
			this.wayPoints.add(newWP);
		}

		this.uavid = uavid;
		this.startLocation = wayPoints.get(0).getCoordinate();
		if (this.wayPoints.size() > 0) {
			this.endLocation = this.wayPoints.get(this.wayPoints.size() - 1).getCoordinate();
		} else {
			endLocation = startLocation;
		}
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
		return startLocation;
	}

	/**
	 * 
	 * @return Ending Coordinates
	 */
	@Override
	public LlaCoordinate getEndLocation() {
		return endLocation;
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
		return status == Status.COMPLETED || waypointsReached();
	}

	@Override
	public double getTakeoffAltitude() {
		return DronologyConstants.TAKE_OFF_ALTITUDE;
	}

	private boolean waypointsReached() {
		for (Waypoint wp : wayPoints) {
			if (!wp.isReached()) {
				return false;
			}
		} 
		return true;
	}

	@Override
	public void setDesignatedDroneId(String uavid) {
		this.uavid=uavid;
		
	}
}
