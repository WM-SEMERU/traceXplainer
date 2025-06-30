package edu.nd.dronology.services.extensions.missionplanning.tasks;

/**
 * A {@link PatternTask} represents a predefined pattern that is expanded when the mission is created.
 * 
 * @author Michael Vierhausers
 * 
 */
public class PatternTask extends AbstractMissionTask {

	protected PatternTask(String uavID, String taskName) {
		super(uavID, taskName);
	}
}