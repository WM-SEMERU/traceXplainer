package edu.nd.dronology.services.instances.flightmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.nd.dronology.core.flight.IFlightPlan;
import edu.nd.dronology.core.flight.PlanPoolManager;
import edu.nd.dronology.services.core.info.FlightInfo;
import edu.nd.dronology.services.core.info.FlightPlanInfo;
import edu.nd.dronology.services.core.util.DronologyServiceException;
import edu.nd.dronology.services.instances.dronesetup.DroneSetupService;

public class FlightInfoCreator {

	public static FlightInfo createInfo(String uavId) throws DronologyServiceException {
		DroneSetupService.getInstance().getActiveUAV(uavId);

		FlightInfo info = new FlightInfo(uavId, uavId);

		IFlightPlan currentFlight = PlanPoolManager.getInstance().getCurrentPlan(uavId);
		if (currentFlight != null) {
			FlightPlanInfo currPl = new FlightPlanInfo(currentFlight.getFlightID(), currentFlight.getFlightID());
			currPl.setWaypoints(currentFlight.getWayPoints());
			currPl.setDroneId(currentFlight.getDesignatedDroneId());
			info.setCurrentFlight(currPl);

		}

		List<IFlightPlan> pendingPlans = PlanPoolManager.getInstance().getPendingPlans(uavId);
		for (IFlightPlan plan : pendingPlans) {
			FlightPlanInfo pinfo = new FlightPlanInfo(plan.getFlightID(), plan.getFlightID());
			pinfo.setWaypoints(plan.getWayPoints());
			pinfo.setDroneId(plan.getDesignatedDroneId());
			info.addPending(pinfo);
		}
		List<IFlightPlan> completedPlans = PlanPoolManager.getInstance().getCompletedPlans(uavId);
		for (IFlightPlan plan : completedPlans) {
			FlightPlanInfo pinfo = new FlightPlanInfo(plan.getFlightID(), plan.getFlightID());
			pinfo.setWaypoints(plan.getWayPoints());
			pinfo.setDroneId(plan.getDesignatedDroneId());
			info.addCompleted(pinfo);
		}

		return info;
	}

	public static Collection<FlightPlanInfo> getCurrenctFlights() {
		List<IFlightPlan> current = new ArrayList<>(PlanPoolManager.getInstance().getCurrentFlights());
		List<FlightPlanInfo> planInfo = new ArrayList<>();
		for (IFlightPlan plan : current) {
			FlightPlanInfo pinfo = new FlightPlanInfo(plan.getFlightID(), plan.getFlightID());
			pinfo.setWaypoints(plan.getWayPoints());
			pinfo.setDroneId(plan.getDesignatedDroneId());
			planInfo.add(pinfo);
		}
		return planInfo;
	}

}
