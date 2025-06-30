package edu.nd.dronology.services.core.info;

import java.util.ArrayList;
import java.util.List;

import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.core.util.Waypoint;

public class FlightPlanInfo extends RemoteInfoObject {

	

	/**
	 * 
	 */
	private static final long serialVersionUID = 256865471183839829L;
	private String droneId;
	private List<Waypoint> waypoints = new ArrayList<>();
	private LlaCoordinate startLocation;
	private long startTime;
	private long endTime;

	public String getDroneId() {
		return droneId;
	}

	public List<Waypoint> getWaypoints() {
		return waypoints;
	}

	public LlaCoordinate getStartLocation() {
		return startLocation;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public FlightPlanInfo(String name, String id) {
		super(name, id);
	}

	public void setDroneId(String droneId) {
		this.droneId = droneId;

	}

	public void setWaypoints(List<Waypoint> waypoints) {
		this.waypoints.clear();
		this.waypoints.addAll(waypoints);

	}

	public void setStartLocation(LlaCoordinate startLocation) {
		this.startLocation = startLocation;

	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;

	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;

	}

	
}
