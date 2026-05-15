package edu.nd.dronology.services.extensions.missionplanning.plan;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.services.core.info.FlightInfo;
import edu.nd.dronology.services.core.info.FlightRouteInfo;
import edu.nd.dronology.services.core.util.DronologyServiceException;
import edu.nd.dronology.services.extensions.missionplanning.MissionExecutionException;
import edu.nd.dronology.services.extensions.missionplanning.patterns.IFlightPattern;
import edu.nd.dronology.services.extensions.missionplanning.patterns.PatternFactory;
import edu.nd.dronology.services.extensions.missionplanning.patterns.PatternFactory.PatternType;
import edu.nd.dronology.services.extensions.missionplanning.patterns.TaskList;
import edu.nd.dronology.services.extensions.missionplanning.sync.SynchronizationManager;
import edu.nd.dronology.services.extensions.missionplanning.tasks.DelayTask;
import edu.nd.dronology.services.extensions.missionplanning.tasks.IMissionTask;
import edu.nd.dronology.services.extensions.missionplanning.tasks.LandTask;
import edu.nd.dronology.services.extensions.missionplanning.tasks.PatternTask;
import edu.nd.dronology.services.extensions.missionplanning.tasks.RouteTask;
import edu.nd.dronology.services.extensions.missionplanning.tasks.SyncTask;
import edu.nd.dronology.services.extensions.missionplanning.tasks.TakeoffTask;
import edu.nd.dronology.services.extensions.missionplanning.tasks.WaypointTask;
import edu.nd.dronology.services.instances.flightmanager.FlightManagerService;
import edu.nd.dronology.services.instances.flightroute.FlightRouteplanningService;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

/**
 * Mission plan for an individual UAV. Part of an {@link IExecuteableMissionPlan}.
 * 
 * @author Jane Cleland-Huang
 *
 */
public class UAVMissionPlan {

	private static final ILogger LOGGER = LoggerProvider.getLogger(UAVMissionPlan.class);
 
	private IMissionTask activeTask = null;
	private final String uavid;
	private List<IMissionTask> taskList;
	private LlaCoordinate homeLocation;
	private LlaCoordinate firstWaypointLocation;

	private int coordinationAltitude = 0;
	private final transient SynchronizationManager synchMgr;

	public UAVMissionPlan(String uavID, SynchronizationManager synchMgr) {
		this.uavid = uavID;
		taskList = new LinkedList<>();
		this.synchMgr = synchMgr;
	}

	/**
	 * Check if task if finished
	 * 
	 * @return
	 * @throws MissionExecutionException
	 */
	private boolean isExecutingTask() throws MissionExecutionException {
		FlightInfo info;
		try {
			info = FlightManagerService.getInstance().getFlightInfo(uavid);
		} catch (DronologyServiceException e) {
			LOGGER.error(e);
			throw new MissionExecutionException(e.getMessage());
		}
		return info.getCurrentFlights() != null;

	}

	/**
	 * Needs to actually activate the flight route. NOTE: Needs to handle ROUTE, WAYPOINT, TAKEOFF, and LAND (Synch is done internally and shouldn't need any extra coordination).
	 * 
	 * @throws Exception
	 */
	public void activateNextTask() throws MissionExecutionException {
		activeTask = null;
		if (!taskList.isEmpty()) {
			activeTask = taskList.get(0);
			taskList.remove(0);

			// Now execute the task. This will depend on whether it is a waypoint, entire
			// route, or synch point
			if (activeTask instanceof SyncTask) {
				LOGGER.missionInfo("UAV " + uavid + " waiting at SYNCH POINT: " + activeTask.getTaskName());
				synchMgr.uavVisitedSynchPoint(uavid, activeTask.getTaskName()); // Marks this uav as synched

				if (synchMgr.isFullySynched(activeTask.getTaskName())) {
					LOGGER.missionInfo("All UAVs released from " + activeTask.getTaskName());
					activeTask = null; // Free to move on.
				}

			} else if (activeTask instanceof RouteTask) {
				MissionUtil.activateRoute(activeTask);
			} else if (activeTask instanceof DelayTask) { 
				((DelayTask) activeTask).startDelayTask();
			} else if (activeTask instanceof WaypointTask || activeTask instanceof TakeoffTask
					|| activeTask instanceof LandTask) {
				LOGGER.missionInfo("ACTIVATED FLIGHT TO WAYPOINT: " + uavid + " " + activeTask.getTaskName());
				MissionUtil.activateWaypoint(activeTask);
			} else {
				throw new UnsupportedOperationException("Task '" + activeTask.getClass() + "' not supported");
			}

		}

	}

	public String getUavID() {
		return uavid;
	}

	public void setCoordinationAltitude(int alt) {
		coordinationAltitude = alt;
	}

	public int getCoordinationAltitude() {
		return coordinationAltitude; // Add error if null.
	}

	public boolean hasActiveTask() throws MissionExecutionException {
		if (activeTask == null) {
			return false;
		}
		if (activeTask instanceof SyncTask) {
			String synchName = activeTask.getTaskName();
			if (!synchMgr.synchPointExists(synchName)) {
				LOGGER.missionError("Missing synch point");
				return false;
			} else if (synchMgr.isFullySynched(synchName)) {
				return false;
			}
			return true;

		} else if (activeTask instanceof DelayTask) {
			return !((DelayTask) activeTask).isFinished();
		} else {// Waypoint or Route
			return isExecutingTask();
		}
	}

	public void addTask(IMissionTask task, SynchronizationManager synchMgr) throws MissionExecutionException { // Remove
		// SynchMgr
		taskList.add(task);
		if (firstWaypointLocation == null && task instanceof RouteTask) {
			firstWaypointLocation = MissionUtil.getFirstWayPoint(task.getTaskName());
		}
	}

	public void buildSynchPoints() {
		taskList.forEach(task -> {
			if (task instanceof SyncTask) {
				synchMgr.createSynchronizationPoint(task.getTaskName());
				synchMgr.addSynchItem(task.getUAVId(), task.getTaskName());
			}
		});
	}

	public boolean hasTasks() {
		return activeTask != null || taskList.size() > 0;

	}

	public int taskCount() {
		return taskList.size();
	}

	/**
	 * Expand the task list with PATTERNS. Currently supports two different patterns "SynchronizedTakeoff" and "SynchronizedLanding" Assumptions: SynchronizedTakeoff has a ROUTE definied immediately
	 * after. SynchronizedLanding has a ROUTE defined immediately prior.
	 * 
	 * @throws MissionExecutionException
	 */
	public void expandTaskList() throws MissionExecutionException {
		List<IMissionTask> allTasks = new ArrayList<>();
		for (IMissionTask task : taskList) {
			homeLocation = MissionUtil.getHome(task.getUAVId());
			if (task instanceof PatternTask) {
				if (task.getTaskName().equals("SynchronizedTakeoff")) {
					IFlightPattern pattern = PatternFactory.getPattern(PatternType.COORDINATED_TAKEOFF);
					pattern.initialize(synchMgr);
					if (firstWaypointLocation != null) // Currently it only expands IF we have at least one ROUTE. It
						// treats the first waypoint as the target waypoint.
						pattern.expandFlightPattern(this, homeLocation, firstWaypointLocation);
					TaskList list = pattern.getTaskList();
					allTasks.addAll(list.getTasks());
				} else if (task.getTaskName().equals("SynchronizedLanding")) {
					IFlightPattern pattern = PatternFactory.getPattern(PatternType.COORDINATED_LANDING);
					pattern.initialize(synchMgr);
					LlaCoordinate currentFinalWaypoint;
					currentFinalWaypoint = MissionUtil.getFinalWaypoint(allTasks);
					pattern.expandFlightPattern(this, currentFinalWaypoint, homeLocation);
					TaskList list = pattern.getTaskList();
					allTasks.addAll(list.getTasks());

				}
			} else {
				allTasks.add(task);
			}
		}
		taskList = allTasks;
	}

	@Override
	public boolean equals(Object o) {
		// self check
		if (this == o)
			return true;
		// null check
		if (o == null)
			return false;
		// type check and cast
		if (getClass() != o.getClass())
			return false;
		UAVMissionPlan that = (UAVMissionPlan) o;
		return this.uavid.equals(that.uavid);
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((uavid == null) ? 0 : uavid.hashCode());
		return result;
	}

	public LlaCoordinate getStartingRouteWaypoint() throws MissionExecutionException {
		for (IMissionTask t : taskList) {
			if (t instanceof RouteTask) {
				try {
					FlightRouteInfo route = FlightRouteplanningService.getInstance().getRouteByName(t.getTaskName());
					LlaCoordinate coord = route.getWaypoints().get(0).getCoordinate();
					return coord;
				} catch (DronologyServiceException e) {
					throw new MissionExecutionException("Error when getting initial waypoint for '" + t.getTaskName() + "'");
				}

			}
		}
		LOGGER.error("No RouteTask found in Mission");
		return taskList.get(0).getWaypoint();

	}

}