package edu.nd.dronology.services.extensions.missionplanning.patterns;

import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.services.extensions.missionplanning.MissionExecutionException;
import edu.nd.dronology.services.extensions.missionplanning.plan.UAVMissionPlan;
import edu.nd.dronology.services.extensions.missionplanning.sync.SynchronizationManager;
import edu.nd.dronology.services.extensions.missionplanning.tasks.IMissionTask;

/**
 * Interface for all flight pattern implementations that can be expanded into several different {@link IMissionTask}.
 * 
 * @author Michael Vierhauser
 *
 */
public interface IFlightPattern {

	void initialize(SynchronizationManager synchMgr);

	/**
	 * Coordinates the flight of multiple UAVs across a potentially overlapping space consisting of a currentLocation and a targetLocation for each UAV. FOR NOW: We assume that currentCoordinates and
	 * targetWayPoints for all UAVs are unique and sufficiently separated from each other. Paths to move from current to target positions may overlap. We should add this check in later on.
	 * 
	 * @param uavMission
	 *          The mission plan the pattern belongs to.
	 * @param currentLocation
	 *          The current location from which the pattern starts.
	 * @param targetLocation
	 *          The target location when the pattern is completed.
	 * @throws MissionExecutionException
	 */
	public void expandFlightPattern(UAVMissionPlan uavMission, LlaCoordinate currentLocation,
			LlaCoordinate targetLocation) throws MissionExecutionException;

	/**
	 * 
	 * @return A {@link TaskList } containing the tasks part of that pattern.
	 */
	public TaskList getTaskList();

}
