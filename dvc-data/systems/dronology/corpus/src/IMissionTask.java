package edu.nd.dronology.services.extensions.missionplanning.tasks;

import edu.nd.dronology.core.coordinate.LlaCoordinate;

/**
 * 
 * @author A specific task in a mission for a UAV.
 *
 */
public interface IMissionTask {

	/**
	 * 
	 * @return The id of the UAV the task belongs to.
	 */
	String getUAVId();

	/**
	 * 
	 * @return The name of the task.
	 */
	String getTaskName();

	/**
	 * 
	 * @return The waypoint associated with the task.
	 */
	LlaCoordinate getWaypoint();

}