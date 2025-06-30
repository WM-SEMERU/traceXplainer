package edu.nd.dronology.services.extensions.missionplanning.plan;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.core.util.Waypoint;
import edu.nd.dronology.core.vehicle.IUAVProxy;
import edu.nd.dronology.services.core.info.FlightRouteInfo;
import edu.nd.dronology.services.core.util.DronologyServiceException;
import edu.nd.dronology.services.extensions.missionplanning.MissionExecutionException;
import edu.nd.dronology.services.extensions.missionplanning.tasks.IMissionTask;
import edu.nd.dronology.services.extensions.missionplanning.tasks.RouteTask;
import edu.nd.dronology.services.instances.dronesetup.DroneSetupService;
import edu.nd.dronology.services.instances.flightmanager.FlightManagerService;
import edu.nd.dronology.services.instances.flightroute.FlightRouteplanningService;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

public class MissionUtil {

	private static final ILogger LOGGER = LoggerProvider.getLogger(MissionUtil.class);

	public static void activateRoute(IMissionTask activeTask) throws MissionExecutionException {
		FlightRouteInfo route = null;
		Collection<FlightRouteInfo> routes;
		try {
			routes = FlightRouteplanningService.getInstance().getItems();
		} catch (RemoteException e) {
			throw new MissionExecutionException("Error when fetching routes");
		}
		for (FlightRouteInfo s : routes) {
			if (s.getName().equals(activeTask.getTaskName())) {
				route = s;
				break;
			}
		}
		if (route == null) {
			throw new MissionExecutionException("Route '" + activeTask.getTaskName() + "' not found");
		}
		try {

			ArrayList<Waypoint> wp = new ArrayList<>(route.getWaypoints());
			FlightManagerService.getInstance().planFlight(activeTask.getUAVId(), activeTask.getTaskName(), wp);
		} catch (Exception e) {
			throw new MissionExecutionException("Error when activating flight plan: " + e.getMessage());
		}
	}

	public static void activateWaypoint(IMissionTask activeTask) throws MissionExecutionException {
		Waypoint wp = new Waypoint(activeTask.getWaypoint());
		try {
			FlightManagerService.getInstance().planFlight(activeTask.getUAVId(), activeTask.getTaskName(),
					Collections.singletonList(wp));
		} catch (Exception e) {
			throw new MissionExecutionException("Error when activating flight plan: " + e.getMessage());
		}
	}

	public static LlaCoordinate getFinalWaypoint(List<IMissionTask> allTasks) throws MissionExecutionException {
		IMissionTask task = allTasks.get(allTasks.size() - 1);
		if (task instanceof RouteTask) {
			LlaCoordinate finalWP = getLastWaypoint(task.getTaskName());
			return finalWP;
		}
		throw new MissionExecutionException("Last task is not a route!");
	}

	private static LlaCoordinate getLastWaypoint(String routeName) throws MissionExecutionException {
		FlightRouteInfo route = null;
		try {
			// route = FlightRouteplanningService.getInstance().getItem(routeName);

			Collection<FlightRouteInfo> items = FlightRouteplanningService.getInstance().getItems();
			for (FlightRouteInfo i : items) {
				if (i.getName().equals(routeName)) {
					route = i;
					break;
				}
			}
			if (route == null) {
				throw new MissionExecutionException("Route '" + routeName + "' not found!");
			}

			Waypoint firstWaypoint = new LinkedList<>(route.getWaypoints()).getLast();

			return firstWaypoint.getCoordinate();
		} catch (RemoteException e) {
			throw new MissionExecutionException(e.getMessage());
		}

	}

	/**
	 * Given a routename extract the first way point
	 * 
	 * @param routeName
	 * @return
	 */
	public static LlaCoordinate getFirstWayPoint(String routeName) throws MissionExecutionException {

		FlightRouteInfo route = null;
		try {
			// route = FlightRouteplanningService.getInstance().getItem(routeName);

			Collection<FlightRouteInfo> items = FlightRouteplanningService.getInstance().getItems();
			for (FlightRouteInfo i : items) {
				if (i.getName().equals(routeName)) {
					route = i;
					break;
				}
			}
			if (route == null) {
				throw new MissionExecutionException("Route '" + routeName + "' not found!");
			}

			Waypoint firstWaypoint = route.getWaypoints().get(0);

			return firstWaypoint.getCoordinate();

		} catch (RemoteException e) {
			throw new MissionExecutionException(e.getMessage());
		}
	}

	/**
	 * Get the home coordinates of *this* UAV i.e., uavID (class variable)
	 * 
	 * @param routeName
	 * @return
	 * @throws MissionExecutionException
	 */
	public static LlaCoordinate getHome(String uavid) throws MissionExecutionException {
		Collection<IUAVProxy> uavList = DroneSetupService.getInstance().getActiveUAVs();

		for (IUAVProxy uav : uavList) {
			if (uav.getID().equals(uavid)) {

				return uav.getHomeLocation();
			}
		}
		throw new MissionExecutionException("UAV '" + uavid + "' not found!");

	}

	public static void stopUAV(String uavid) {
		try {
			FlightManagerService.getInstance().emergencyStop(uavid);
		} catch (DronologyServiceException e) {
			LOGGER.error(e);
		}

	}

}
