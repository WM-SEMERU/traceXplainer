package edu.nd.dronology.services.extensions.missionplanning.tasks;

import edu.nd.dronology.core.coordinate.LlaCoordinate;

public class WaypointTask extends AbstractMissionTask {

	private final LlaCoordinate coordinate;

	protected WaypointTask(String uavID, LlaCoordinate coordinate) {
		super(uavID, coordinate.toString());
		this.coordinate = coordinate;
	}

	@Override
	public LlaCoordinate getWaypoint() {
		return coordinate;
	}
}