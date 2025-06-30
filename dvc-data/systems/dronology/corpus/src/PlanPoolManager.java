package edu.nd.dronology.core.flight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.nd.dronology.core.exceptions.DroneException;
import edu.nd.dronology.core.monitoring.DronologyMonitoringManager;
import edu.nd.dronology.core.monitoring.MessageMarshaller;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

/**
 * Management of pending plans for individual UAVs.<br>
 * Each active UAV has a {@link PlanPool} that keeps track of the assigned (and pending) flight plans.
 * 
 * @author Michael Vierhauser
 *
 */
public class PlanPoolManager {

	private static volatile PlanPoolManager INSTANCE = null;

	private static final ILogger LOGGER = LoggerProvider.getLogger(PlanPoolManager.class);

	private Map<String, PlanPool> planList = new ConcurrentHashMap<>();

	private List<IFlightPlan> pendingPlans = Collections.synchronizedList(new ArrayList<>());

	private List<IPlanStatusChangeListener> planStatusChangeListener = Collections.synchronizedList(new ArrayList<>());

	public static PlanPoolManager getInstance() {
		if (INSTANCE == null) {
			synchronized (PlanPoolManager.class) {
				if (INSTANCE == null) {
					INSTANCE = new PlanPoolManager();
				}
			}
		}
		return INSTANCE;
	}

	public void addNewPlan(IFlightPlan plan) throws DroneException {
		String uavId = plan.getDesignatedDroneId();
		if (uavId == null) {
			assignPlan(plan);
		} else {
			assignToPool(uavId, plan);
		}
	}

	private void assignPlan(IFlightPlan plan) throws DroneException {
		pendingPlans.add(plan);

	}

	private void assignToPool(String uavId, IFlightPlan plan) {
		PlanPool pp = null;
		if (planList.get(uavId) == null) {
			pp = new PlanPool(uavId);
			planList.put(uavId, pp);
		} else {
			pp = planList.get(uavId);
		}
		pp.addPlan(plan);
		pendingPlans.add(plan);
	}

	public boolean hasPendingFlights() {
		return pendingPlans.size() > 0;
	}

	public IFlightPlan getNextPendingPlan() {
		return pendingPlans.get(0);
	}

	public void activatePlan(IFlightPlan plan, String uavid) throws DroneException {
		if (planList.get(uavid) == null) {
			throw new DroneException("Error no plan for '" + uavid + "' pending");

		}
		pendingPlans.remove(plan);
		planList.get(uavid).setNextPlanToCurrent();
		plan.setDesignatedDroneId(uavid); 
		DronologyMonitoringManager.getInstance().publish(MessageMarshaller.createPlanActive(plan));
	}

	public void checkFormCompletedPlans() throws DroneException {
		for (PlanPool pp : planList.values()) {
			if (pp.getCurrentPlan() != null && pp.getCurrentPlan().isCompleted()) {
				IFlightPlan current = pp.getCurrentPlan();
				pp.setCurrentFlightCompleted();
				DronologyMonitoringManager.getInstance().publish(MessageMarshaller.createPlanCompleted(current));
				notifyPlanChange(current);
			}
		}
	}

	private void notifyPlanChange(IFlightPlan changedPlan) {
		for (IPlanStatusChangeListener listener : planStatusChangeListener) {
			listener.notifyPlanChange(changedPlan);
		}

	}

	public List<IFlightPlan> getCurrentFlights() {
		List<IFlightPlan> currentFlights = new ArrayList<>();
		for (PlanPool pp : planList.values()) {
			if (pp.getCurrentPlan() != null) {
				currentFlights.add(pp.getCurrentPlan());
			}
		}
		return currentFlights;
	}

	public void assignPlan(IFlightPlan plan, String uavId) {
		PlanPool pp = null;
		if (planList.get(uavId) == null) {
			pp = new PlanPool(uavId);
			planList.put(uavId, pp);
		} else {
			pp = planList.get(uavId);
		}
		pp.addPlan(plan);

	}

	public void addPlanStatusChangeListener(IPlanStatusChangeListener listener) {
		planStatusChangeListener.add(listener);

	}

	public IFlightPlan getCurrentPlan(String uavId) {
		if (planList.get(uavId) != null) {
			return planList.get(uavId).getCurrentPlan();
		}
		return null;
	}

	public List<IFlightPlan> getPendingPlans(String uavId) {
		if (planList.get(uavId) != null) {
			return planList.get(uavId).getPendingPlans();
		}
		return Collections.emptyList();
	}

	public List<IFlightPlan> getCompletedPlans(String uavId) {
		if (planList.get(uavId) != null) {
			return planList.get(uavId).getCompletedPlans();
		}
		return Collections.emptyList();
	}

	public void overridePlan(IFlightPlan homePlane, String uavid) throws DroneException {
		if (planList.get(uavid) == null) {
			throw new DroneException("Error no plan for '" + uavid + "' pending");

		}
		planList.get(uavid).overridePlan(homePlane);

	}

	public void cancelPendingPlans(String uavid) throws DroneException {
		if (planList.get(uavid) == null) {
			throw new DroneException("Error no plan for '" + uavid + "' pending");

		} 
		planList.get(uavid).cancelPendingPlans();
		synchronized (pendingPlans) {
			List<IFlightPlan> allPending = new ArrayList<>(pendingPlans);

			for (IFlightPlan p : allPending) {
				if (p.getDesignatedDroneId().equals(uavid)) {
					pendingPlans.remove(p);
				}
			}
		}
	}

	public List<IFlightPlan> getPendingPlans() {
		return Collections.unmodifiableList(new ArrayList<>(pendingPlans));
	}

}
