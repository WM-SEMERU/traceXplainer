package edu.nd.dronology.core.util;

import java.io.Serializable;

import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.core.flight.internal.FlightPlan;
import edu.nd.dronology.util.NullUtil;

/**
 * 
 * Waypoint implementation used in {@link FlightPlan} and Fight Route to represent coordinates (as {@link LlaCoordinate})<br>
 * and the approaching speed towards the respective waypoint. <br>
 * <br>
 * This class (and all its constituent fields) have to be {@link Serializable} as this class is passed via the remote interface.
 * 
 * @author Michael Vierhauser
 *
 */
public class Waypoint implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5855436372355880741L;

	private final LlaCoordinate coordinate;
	private boolean destinationReached = false;

	private double approachingspeed = 0.0d;

	public LlaCoordinate getCoordinate() {
		return coordinate;
	}

	public Waypoint() {
		coordinate = new LlaCoordinate(0, 0, 0);
	}

	public Waypoint(LlaCoordinate coordinate) {
		super();
		NullUtil.checkNull(coordinate);
		this.coordinate = coordinate;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Waypoint))
			return false;
		Waypoint other = (Waypoint) obj;
		return coordinate.equals(other.getCoordinate());
	}

	public boolean isReached() {
		return destinationReached;
	}

	@Override
	public int hashCode() {
		return coordinate.hashCode();
	}

	public void reached(boolean reached) {
		this.destinationReached = reached;

	}

	public double getApproachingspeed() {
		return approachingspeed;
	}

	public void setApproachingspeed(double approachingspeed) {
		this.approachingspeed = approachingspeed;
	}

}
