package edu.nd.dronology.services.core.info;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.nd.dronology.core.util.Waypoint;

public class FlightRouteInfo extends RemoteInfoObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7327376857430499641L;
	private String category;
	private List<Waypoint> waypoints = new ArrayList<>();
	private long dateCreated;
	private long dateModified;
	private double length;
	private String description;

	public FlightRouteInfo(String name, String id) {
		super(name, id);
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;

	}

	public List<Waypoint> getWaypoints() {
		return Collections.unmodifiableList(waypoints);
	}

	public void setDateModified(long dateModified) {
		this.dateModified = dateModified;
	}

	public void setDateCreated(long dateCreated) {
		this.dateCreated = dateCreated;
	}

	public void addWaypoint(Waypoint waypoint) {
		waypoints.add(waypoint);
	}

	public void removeWaypoint(Waypoint waypoint) {
		waypoints.remove(waypoint);
	}

	public double getRouteLength() {
		return length;
	}

	public long getDateCreated() {
		return dateCreated;
	}

	public long getDateModified() {
		return dateModified;
	}

	public void setRouteLength(double length) {
		this.length = length;

	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
