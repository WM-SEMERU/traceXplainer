package edu.nd.dronology.core.monitoring;

import java.io.Serializable;

import edu.nd.dronology.core.flight.IFlightPlan;
import edu.nd.dronology.core.monitoring.messages.UAVMonitorableMessage;
import edu.nd.dronology.core.monitoring.messages.UAVMonitorableMessage.MessageType;
import edu.nd.dronology.core.vehicle.DroneFlightStateManager.FlightMode;
import edu.nd.dronology.core.vehicle.commands.IDroneCommand;

public class MessageMarshaller {

	public static class StateWrapper implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6332164623939457896L;
		private String oldstate;
		private String newstate;
		private String uavid;

		public StateWrapper(String uavid, FlightMode oldState, FlightMode newState) {
			this.uavid = uavid;
			this.oldstate = oldState.toString();
			this.newstate = newState.toString();
		}

	}

	public static IMonitorableMessage create(String droneId, FlightMode oldState, FlightMode newState) {
		// return new UAVStateChangeMessage(droneId, oldState.toString(),
		// newState.toString());
		return new UAVMonitorableMessage(MessageType.STATE_CHANGE, droneId,
				new StateWrapper(droneId, oldState, newState));
	}

	public static IMonitorableMessage createPlanActive(IFlightPlan plan) {
		// return new UAVPlanChangeMessage(plan.getDesignatedDroneId(), "ACTIVATE",
		// plan.getFlightID(),
		// plan.getStartTime(), plan.getEndTime());
		return new UAVMonitorableMessage(MessageType.PLAN_ACTIVATED, plan.getDesignatedDroneId(), plan);
	}

	public static IMonitorableMessage createPlanCompleted(IFlightPlan plan) {
		// return new UAVPlanChangeMessage(plan.getDesignatedDroneId(), "COMPLETE",
		// plan.getFlightID(),
		// plan.getStartTime(), plan.getEndTime());

		return new UAVMonitorableMessage(MessageType.PLAN_COMPLETE, plan.getDesignatedDroneId(), plan);

	}

	public static IMonitorableMessage createMessage(IDroneCommand cmd) {
		return new UAVMonitorableMessage(MessageType.COMMAND, cmd.getUAVId(), cmd);
	}

	public static IMonitorableMessage createMessage(MessageType type, String uavid, Serializable data) {
		return new UAVMonitorableMessage(type, uavid, data);
	}

	public static IMonitorableMessage createMessage(MessageType type, String uavid) {
		return createMessage(type, uavid, null);
	}

}
