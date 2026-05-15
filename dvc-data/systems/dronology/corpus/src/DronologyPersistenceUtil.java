package edu.nd.dronology.services.core.persistence;

import com.thoughtworks.xstream.XStream;

import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.core.util.Waypoint;
import edu.nd.dronology.services.core.items.UAVRegistration;
import edu.nd.dronology.services.core.items.FlightRoute;

public class DronologyPersistenceUtil {

	private static final String ROUTE_ALIAS = "FlightRoute";
	private static final String COORDINATE_ALIAS = "Coordinate";
	private static final String SPEC_ALIAS = "UAVSpecification";
	private static final String WAYPOINT_ALIAS = "Waypoint";
	
	public static void preprocessStream(XStream xstream) {

		xstream.alias(ROUTE_ALIAS, FlightRoute.class);
		xstream.alias(COORDINATE_ALIAS, LlaCoordinate.class);
		xstream.alias(WAYPOINT_ALIAS, Waypoint.class);
		
		xstream.alias(SPEC_ALIAS, UAVRegistration.class);

	}

}
