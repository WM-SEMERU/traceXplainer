package edu.nd.dronology.services.core.info;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FlightInfo extends RemoteInfoObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 256865471183839829L;
	private FlightPlanInfo currentFlight = null;
	private List<FlightPlanInfo> pending = Collections.emptyList();
	private List<FlightPlanInfo> completed = Collections.emptyList();

	public FlightInfo(String name, String id) {
		super(name, id);
		pending = new ArrayList<>();
		completed = new ArrayList<>();
	}

	public void setCurrentFlight(FlightPlanInfo currentFlight) {
		this.currentFlight = currentFlight;
	}

	public FlightPlanInfo getCurrentFlights() {
		return currentFlight;
	}

	public List<FlightPlanInfo> getPendingFlights() {
		return pending;
	}

	public List<FlightPlanInfo> getCompletedFlights() {
		return completed;
	}

	public void addPending(FlightPlanInfo planInfo) {
		pending.add(planInfo);

	}

	public void addCompleted(FlightPlanInfo planInfo) {
		completed.add(planInfo);

	}


}
