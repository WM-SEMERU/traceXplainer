package edu.nd.dronology.core.simulator.simplesimulator;

import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.core.util.DistanceUtil;
import edu.nd.dronology.core.vehicle.internal.VirtualDrone;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

/**
 * Computes the current position of a virtual drone as it moves during flight. Serves as a lightweight SITL for a drone.
 * 
 * @author Jane Cleland-Huang
 */
public class FlightSimulator {

	private static final ILogger LOGGER = LoggerProvider.getLogger(FlightSimulator.class);

	private LlaCoordinate currentPosition;
	private LlaCoordinate targetPosition;
	private Long previousDistance = 0L;

	private boolean reached = false;

	private VirtualDrone drone;

	/**
	 * Creates a flight simulator object for a single virtual drone
	 * 
	 * @param drone
	 */
	protected FlightSimulator(VirtualDrone drone) {
		this.drone = drone;
	}

	/**
	 * Sets current flight path from current position to a targeted position
	 * 
	 * @param currentPos
	 *          Coordinates of current position
	 * @param targetPos
	 *          Coordinates of target position
	 */
	public void setFlightPath(LlaCoordinate currentPos, LlaCoordinate targetPos) {
		if (currentPosition == null || reached) {

			this.currentPosition = currentPos;
			reached = false;
		} 

		this.targetPosition = targetPos;
		previousDistance = getRemainingDistance();
	}

	/**
	 * Computes the distance between current position and target position
	 * 
	 * @return distance remaining in degree points.
	 */
	public long getRemainingDistance() {
		return (long) Math.sqrt((Math.pow(computeLongitudeDelta(), 2)) + Math.pow(computeLatitudeDelta(), 2));
	}

	/**
	 * Computes the delta between the drones current latitude and its target latitude.
	 * 
	 * @return
	 */
	private double computeLatitudeDelta() {
		return (currentPosition.getLatitude() - targetPosition.getLatitude()) * 1000000;
	}

	/**
	 * Computes the delta between the drones current longitude and its target longitude
	 * 
	 * @return
	 */
	private double computeLongitudeDelta() {
		return (currentPosition.getLongitude() - targetPosition.getLongitude()) * 1000000;
	}

	/**
	 * Computes the angle at which a drone is flying with respect to the vertical
	 * 
	 * @return
	 */
	private double computeAngle() {
		double height = computeLatitudeDelta(); // opposite
		// double width = (computeLongitudeDelta());
		double hypotenuse = getRemainingDistance();
		double sinTheta = height / hypotenuse;
		double angle = Math.asin(sinTheta) * 180 / Math.PI;
		return Double.isNaN(angle) ? 1 : angle;
	}

	/**
	 * Computes the position of the drone following one step. Checks if destination has been reached.
	 * 
	 * @param step
	 *          : Distance in degree points to move per iteration
	 * @return isStillMoving?
	 */
	public boolean move(double step) {
		try {
			// First determine which relative quadrant the target is in -- in relation to current position at the origin of X,Y axes

			double theta = computeAngle();
			double heightIncrement = Math.abs((long) (Math.sin(theta) * step));
			double widthIncrement = Math.abs((long) (Math.cos(theta) * step));

			double scaleFactor = 0.1;

			widthIncrement *= scaleFactor;
			heightIncrement *= scaleFactor;
			double newLongit = 0;
			double newLatid = 0;

			// Latitude delta
			if (currentPosition.getLatitude() < targetPosition.getLatitude()) {
				// currentPosition.setLatitude(currentPosition.getLatitude() + heightIncrement); // Drone is south of Target
				newLatid = (currentPosition.getLatitude() * 1000000) + heightIncrement;
			} else {
				// currentPosition.setLatitude(currentPosition.getLatitude() - heightIncrement); // Drone is North (or same) as target
				newLatid = (currentPosition.getLatitude() * 1000000) - heightIncrement;
			}
			// Longitude delta
			if (currentPosition.getLongitude() < targetPosition.getLongitude()) {
				// currentPosition.setLongitude(currentPosition.getLongitude() + widthIncrement); // Drone is to the left/west of target
				newLongit = (currentPosition.getLongitude() * 1000000) + widthIncrement;
			} else {
				// currentPosition.setLongitude(currentPosition.getLongitude() - widthIncrement); // Drone is to the right/east of target
				newLongit = (currentPosition.getLongitude() * 1000000) - widthIncrement;
			}
			// double distanceMoved = Math.sqrt(Math.pow(heightIncrement,2)+Math.pow(widthIncrement,2));

			newLatid = newLatid / 1000000;
			newLongit = newLongit / 1000000;

			currentPosition = new LlaCoordinate(newLatid, newLongit, currentPosition.getAltitude());
			LOGGER.trace("Remaining Dinstance: " + DistanceUtil.distance(currentPosition, targetPosition));
			// if (previousDistance <= getRemainingDistance() && getRemainingDistance() < 200) {
			drone.setCoordinates(currentPosition);
			if (DistanceUtil.distance(currentPosition, targetPosition) < 2) {
				previousDistance = getRemainingDistance();
				// LOGGER.info(drone.getDroneName() + " ==> Waypoint reached");
				reached = true;
				return false;
			} else {
				previousDistance = getRemainingDistance();
				return true;
			}
		} catch (Exception e) {
			LOGGER.error(e);
		}
		return false;

	}

	/**
	 * Checks if a drone has reached its target destination.
	 * 
	 * @param distanceMovedPerTimeStep
	 *          Checks location with respect to target position.
	 * @return true if target position is reached.
	 */
	public boolean isDestinationReached(double distanceMovedPerTimeStep) {
		double latDistance = Math.abs(currentPosition.getLatitude() - targetPosition.getLatitude());
		double lonDistance = Math.abs(currentPosition.getLongitude() - targetPosition.getLongitude());
		return lonDistance <= distanceMovedPerTimeStep && latDistance <= distanceMovedPerTimeStep;
	}

}
