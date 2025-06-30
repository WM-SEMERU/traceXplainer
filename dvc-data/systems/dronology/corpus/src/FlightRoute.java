package edu.nd.dronology.services.core.items;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import edu.nd.dronology.core.Discuss;
import edu.nd.dronology.core.DronologyConstants;
import edu.nd.dronology.core.util.Waypoint;

/**
 * 
 * Implementation of a flight route, containing a series of waypoints and additional meta-data.
 * 
 * @author Michael Vierhauser
 *
 */
public class FlightRoute implements IFlightRoute, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3151158691427417882L;
	private String name;
	private String id;
	private String category = "Default";
	private LinkedList<Waypoint> waypoints;
	private double takeoffaltitude = DronologyConstants.TAKE_OFF_ALTITUDE;
	private String description;

	public double setTakeoffAltitude() {
		return takeoffaltitude;
	}

	public FlightRoute() {
		id = UUID.randomUUID().toString();
		waypoints = new LinkedList<>();
		name = id;
	}

	@Override
	public void setName(String name) {
		this.name = name;

	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;

	}

	@Override
	public void setCategory(String category) {
		this.category = category;

	}

	@Override
	public String getCategory() {
		return category;
	}

	@Override
	public List<Waypoint> getWaypoints() {
		return Collections.unmodifiableList(waypoints);
	}

	@Override
	public void addWaypoint(Waypoint waypoint) {
		waypoints.add(waypoint);
	}

	@Discuss(discuss = "this currently breaks if you add 2 identical coordinates...")
	@Override
	public int removeWaypoint(Waypoint coordinate) {
		int index = waypoints.indexOf(coordinate);
		if (index != -1) {
			waypoints.remove(coordinate);
		}
		return index;
	}

	@Override
	public void addWaypoint(Waypoint waypoint, int index) {
		waypoints.add(index, waypoint);
	}

	@Override
	public Waypoint removeWaypoint(int index) {
		return waypoints.remove(index);

	}

	@Override
	public void setTakeoffAltitude(double altitude) {
		if (altitude <= 0) {
			throw new IllegalArgumentException("Takeoff altitude must not be a postive number > 0");
		}
		this.takeoffaltitude = altitude;
	}
}
