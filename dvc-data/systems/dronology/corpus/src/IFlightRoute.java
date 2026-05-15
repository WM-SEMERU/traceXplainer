package edu.nd.dronology.services.core.items;

import java.util.List;

import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.core.util.Waypoint;

public interface IFlightRoute extends IPersistableItem {
	String getDescription();

	void setDescription(String description);

	void setCategory(String category);

	String getCategory();

	List<Waypoint> getWaypoints();

	void addWaypoint(Waypoint waypoint);

	void setTakeoffAltitude(double altitude);

	void addWaypoint(Waypoint waypoint, int index);

	int removeWaypoint(Waypoint waypoint);

	Waypoint removeWaypoint(int index);

}
