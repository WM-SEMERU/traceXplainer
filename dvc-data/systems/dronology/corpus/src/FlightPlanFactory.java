package edu.nd.dronology.core.flight;

import java.util.List;

import edu.nd.dronology.core.flight.internal.FlightPlan;
import edu.nd.dronology.core.util.Waypoint;

public class FlightPlanFactory {

	public static IFlightPlan create(String uavid, String planName, List<Waypoint> waypoints) {
		return new FlightPlan(uavid, planName, waypoints);
	}

	public static IFlightPlan create(String planName, List<Waypoint> waypoints) {
		return new FlightPlan(planName, waypoints);

	}

}
