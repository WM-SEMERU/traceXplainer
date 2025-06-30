package edu.nd.dronology.services.instances.flightmanager;

import java.util.Collection;
import java.util.List;

import edu.nd.dronology.core.util.Waypoint;
import edu.nd.dronology.services.core.api.IServiceInstance;
import edu.nd.dronology.services.core.info.FlightInfo;
import edu.nd.dronology.services.core.info.FlightPlanInfo;
import edu.nd.dronology.services.core.util.DronologyServiceException;

public interface IFlightManagerServiceInstance extends IServiceInstance {

	public FlightInfo getFlightDetails();

	public void planFlight(String uavid, String planName, List<Waypoint> waypoints) throws Exception;

	public void planFlight(String planName, List<Waypoint> waypoints) throws Exception;

	public void returnToHome(String uavid) throws Exception;

	public void pauseFlight(String uavid) throws Exception;

	public FlightInfo getFlightInfo(String uavId) throws DronologyServiceException;

	Collection<FlightPlanInfo> getCurrentFlights();

	public void cancelPendingFlights(String uavid) throws Exception;

	public void takeoff(String uavid, double altitude) throws DronologyServiceException;

	public void emergencyStop(String uavid) throws DronologyServiceException;

}
