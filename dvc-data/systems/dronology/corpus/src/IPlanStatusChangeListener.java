package edu.nd.dronology.core.flight;

public interface IPlanStatusChangeListener {

	void notifyPlanChange(IFlightPlan changedPlan);

}
