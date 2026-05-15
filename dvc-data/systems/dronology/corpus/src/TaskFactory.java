package edu.nd.dronology.services.extensions.missionplanning.tasks;

import java.io.Serializable;
import java.util.Map;

import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.services.extensions.missionplanning.MissionExecutionException;

public class TaskFactory {

	public static final String FLIGHTPATTERN = "FLIGHTPATTERN";
	public static final String ROUTE = "ROUTE";
	public static final String SYNC = "SYNC";
	public static final String WAYPOINT = "WAYPOINT";
	public static final String TAKEOFF = "TAKEOFF";
	public static final String LAND = "LAND";
	public static final String DELAY = "DELAY";

	@Deprecated
	public static IMissionTask getTask(String type, String uavid, String taskname, Object... params)
			throws MissionExecutionException {
		switch (type) {
		case FLIGHTPATTERN:
			return new PatternTask(uavid, taskname);

		case ROUTE:
			return new RouteTask(uavid, taskname);

		case SYNC:
			return new SyncTask(uavid, taskname);

		case DELAY:
			if (params.length != 1) {
				throw new MissionExecutionException("missing duration parameter");
			}
			double duration = Double.parseDouble(params[0].toString());
			return new DelayTask(uavid, taskname, duration);

		default:
			throw new MissionExecutionException("Type " + type + " not supported");
		}
	}

	
	
	public static IMissionTask getTask(String type, String uavid, String taskname,Map<String, Serializable> params)
			throws MissionExecutionException {
		switch (type) {
		case FLIGHTPATTERN:
			return new PatternTask(uavid, taskname);

		case ROUTE:
			return new RouteTask(uavid, taskname);

		case SYNC:
			return new SyncTask(uavid, taskname);

		case DELAY:
			if (!params.containsKey("duration")) {
				throw new MissionExecutionException("missing duration parameer");
			}
			double duration = Double.parseDouble(params.get("duration").toString());
			return new DelayTask(uavid, taskname, duration);

		default:
			throw new MissionExecutionException("Type " + type + " not supported");
		}
	}

	public static IMissionTask getTask(String type, String uavid, LlaCoordinate coordinate) {
		switch (type) {
		case WAYPOINT:
			return new WaypointTask(uavid, coordinate);

		case TAKEOFF:
			return new TakeoffTask(uavid, coordinate);

		case LAND:
			return new LandTask(uavid, coordinate);

		default:
			throw new IllegalArgumentException("Type " + type + " not supported");
		}
	}

}
