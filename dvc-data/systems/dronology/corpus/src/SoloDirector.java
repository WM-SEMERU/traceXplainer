package edu.nd.dronology.core.flight.internal;

import java.util.ArrayList;
import java.util.List;

import edu.nd.dronology.util.NullUtil;
import edu.nd.dronology.core.Discuss;
import edu.nd.dronology.core.DronologyConstants;
import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.core.exceptions.FlightZoneException;
import edu.nd.dronology.core.flight.IFlightDirector;
import edu.nd.dronology.core.util.Waypoint;
import edu.nd.dronology.core.vehicle.ManagedDrone;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

/**
 * Directions for one flight containing multiple waypoints.
 * 
 * @author Jane Cleland-Huang
 * 
 */
public class SoloDirector implements IFlightDirector {

	private static final ILogger LOGGER = LoggerProvider.getLogger(SoloDirector.class);

	private final ManagedDrone drone;
	private boolean safetyDiversion = false;
	private LlaCoordinate targetPosition = null;
	private List<Waypoint> wayPoints = new ArrayList<>();
	private List<LlaCoordinate> roundaboutPath = new ArrayList<>();

	@Override
	public LlaCoordinate flyToNextPoint() {
		targetPosition = flyToNextWayPoint();
		return targetPosition;
	}

	public List<Waypoint> getWayPoints() {
		return new ArrayList<>(this.wayPoints);
	}

	/**
	 * Constructor
	 * 
	 * @param managedDrone
	 */
	public SoloDirector(ManagedDrone managedDrone) {
		this.drone = managedDrone;
	}

	@Override
	public void setWayPoints(List<Waypoint> wayPoints) {
		this.wayPoints = new ArrayList<>(wayPoints);
	}

	@Override
	public void clearWayPoints() {
		wayPoints.clear();
	}

	@Override
	public boolean hasMoreWayPoints() {
		return !wayPoints.isEmpty();
	}

	@Discuss(discuss = "this is called way to often.. needs fixing")
	private LlaCoordinate flyToNextWayPoint() {
		// LOGGER.info("Flying to next waypoint");
		if (!wayPoints.isEmpty()) {
			Waypoint nextWaypoint = wayPoints.get(0);
			drone.flyTo(nextWaypoint.getCoordinate(), nextWaypoint.getApproachingspeed());
			return nextWaypoint.getCoordinate();
		}
		return null;
	}

	@Override
	public boolean isUnderSafetyDirectives() {
		return safetyDiversion;
	}

	@Override
	public void clearCurrentWayPoint() {
		if (isUnderSafetyDirectives()) {
			if (!roundaboutPath.isEmpty()) {
				roundaboutPath.remove(0);
				if (roundaboutPath.isEmpty()) { 
					safetyDiversion = false;
				}
			}
		} else {
			if (!wayPoints.isEmpty()) {
				Waypoint wp = wayPoints.remove(0);
				wp.reached(true);
			}
			if (wayPoints.isEmpty()) {
				try { 
					drone.getFlightModeState().setModeToInAir(); 
				} catch (FlightZoneException e) {
					LOGGER.error(e);
				}
			}
		}
	}

	@Override
	public void addWayPoint(Waypoint wayPoint) {
		wayPoints.add(wayPoint);
	}

	@Override
	public void flyHome() {
		drone.flyTo(drone.getBaseCoordinates(), DronologyConstants.RETURN_TO_HOME_SPEED);

	}

	@Override
	public void returnHome(Waypoint home) {
		NullUtil.checkNull(home);
		addWayPoint(home);
		ArrayList<Waypoint> tempWayPoints = new ArrayList<>(wayPoints);

		for (Waypoint wayPoint : tempWayPoints) {
			if (!wayPoint.equals(home)) {
				wayPoints.remove(wayPoint);
			}
		}

		// Should only have one waypoint left and ready to go home!!
	}

	@Override
	public boolean readyToLand() {
		return hasMoreWayPoints();

	}

	@Override
	public boolean readyToTakeOff() {
		if (!hasMoreWayPoints()) {
			return false;
		} else {
			return true;
		}
	}

}
