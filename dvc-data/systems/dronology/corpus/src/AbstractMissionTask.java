package edu.nd.dronology.services.extensions.missionplanning.tasks;

import edu.nd.dronology.core.coordinate.LlaCoordinate;

public class AbstractMissionTask implements IMissionTask {
	private final String uavID;
	private final String taskName;

	protected AbstractMissionTask(String uavID, String taskName) {
		this.uavID = uavID;

		this.taskName = taskName;
	}

	@Override
	public String getTaskName() {
		return taskName;
	}

	@Override
	public String getUAVId() {
		return uavID;
	}

	@Override
	public LlaCoordinate getWaypoint() {
		throw new UnsupportedOperationException();
	}

}