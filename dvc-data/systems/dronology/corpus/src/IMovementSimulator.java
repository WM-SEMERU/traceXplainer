package edu.nd.dronology.core.simulator;

import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.core.vehicle.internal.VirtualDrone;

/**
 * 
 * The movement simulator simulates the movement of a {@link VirtualDrone} to reach its assigned waypoints.
 * 
 * @author Michael Vierhauser
 *
 */
public interface IMovementSimulator {

	boolean move(double i);

	void setFlightPath(LlaCoordinate currentPosition, LlaCoordinate targetCoordinates);

	void checkPoint();

	boolean isDestinationReached(double distanceMovedPerTimeStep);

}
