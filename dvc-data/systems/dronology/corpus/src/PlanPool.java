package edu.nd.dronology.core.flight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.nd.dronology.core.exceptions.DroneException;
import edu.nd.dronology.core.exceptions.FlightZoneException;
import edu.nd.dronology.core.util.DummyLockObject;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

/**
 * The {@link PlanPool} manages pending, executing, and completed plans for a single UAV.
 * 
 * @author Michael Vierhauser
 *
 */
public class PlanPool {

	private static final ILogger LOGGER = LoggerProvider.getLogger(PlanPool.class);

	private DummyLockObject lockObject = new DummyLockObject();
	private final List<IFlightPlan> pendingPlans = Collections.synchronizedList(new ArrayList<>());
	private IFlightPlan currentPlan;
	private final List<IFlightPlan> completedPlans = Collections.synchronizedList(new ArrayList<>());
	private String id; 

	public PlanPool(String id) {
		this.id = id;
	}

	public void setCurrentFlightCompleted() throws DroneException {
		synchronized (lockObject) {
			if (currentPlan == null) {
				throw new DroneException("No active Plan to complete!");
			}
			completedPlans.add(currentPlan);
			try {
				currentPlan.setStatusToCompleted();
			} catch (FlightZoneException e) {
				LOGGER.error(e); 
			}
			LOGGER.trace("Plan '" + currentPlan.getFlightID() + "' completed");
			currentPlan = null;
		}

	}

	public void setNextPlanToCurrent() throws DroneException {
		synchronized (lockObject) { 
			if (currentPlan != null) {
				throw new DroneException("Current plan not completed!");
			}
			if (pendingPlans.isEmpty()) {
				throw new DroneException("No pending flights scheduled!");
			}
			currentPlan = pendingPlans.remove(0);

			LOGGER.info(id + " - Plan '" + currentPlan.getFlightID() + "' setActive");
		}

	}

	public void addPlan(IFlightPlan plan) { 
		LOGGER.info("New Flight Plan '" + plan.getFlightID() + "' scheduled for " + id);
		pendingPlans.add(plan);

	}

	public boolean hasPendingPlans() {
		return pendingPlans.size() > 0;
	}

	public IFlightPlan getCurrentPlan() {
		return currentPlan;
	}

	public List<IFlightPlan> getPendingPlans() {
		return Collections.unmodifiableList(new ArrayList<>(pendingPlans));
	}

	public List<IFlightPlan> getCompletedPlans() {
		return Collections.unmodifiableList(new ArrayList<>(completedPlans));
	}
 
	public void overridePlan(IFlightPlan masterPlan) {
		synchronized (this) {
			if (currentPlan != null) {
				LOGGER.info("Current flight plan '" + currentPlan.getFlightID() + "' cancelled");
				LOGGER.missionError("Current flight plan '" + currentPlan.getFlightID() + "' " + id + " cancelled");
				currentPlan = null;
			}
			if (pendingPlans.size() > 0) {
				LOGGER.info(pendingPlans.size() + " Pending flight plans cancelled");
				LOGGER.missionError(pendingPlans.size() + " Pending flight plans for " + id + " cancelled");
				pendingPlans.clear();
			}
			currentPlan = masterPlan;
		} 

	}

	public void cancelPendingPlans() { 
		if (pendingPlans.size() > 0) {
			LOGGER.info(pendingPlans.size() + " Pending flight plans cancelled");
			LOGGER.missionError(pendingPlans.size() + " Pending flight plans for " + id + " cancelled");
			pendingPlans.clear();
		}
	}
}
