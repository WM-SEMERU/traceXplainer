package edu.nd.dronology.core.vehicle;

import java.util.ArrayList;
import java.util.List;

import com.github.oxo42.stateless4j.StateMachine;

import edu.nd.dronology.core.exceptions.FlightZoneException;
import edu.nd.dronology.core.monitoring.DronologyMonitoringManager;
import edu.nd.dronology.core.monitoring.MessageMarshaller;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

/**
 * Associates a drone state object with a drone. <br>
 * Normal behavior : ON_GROUND -> AWAITING_TAKEOFF_CLEARANCE -> TAKING_OFF -> FLYING -> In IN_AIR -> LANDING <br>
 * Unavailable transitions will result in an exception being thrown.
 *  
 * @author Jane Cleland-Huang
 *
 */
public class DroneFlightStateManager {

	private static final ILogger LOGGER = LoggerProvider.getLogger(DroneFlightStateManager.class);

	public enum FlightMode {
		ON_GROUND, AWAITING_TAKEOFF_CLEARANCE, TAKING_OFF, FLYING, IN_AIR, LANDING, USER_CONTROLLED
	}

	private enum FlightModeTransition {
		TO_ON_GROUND, PLAN_ASSIGNED, TAKEOFF_GRANTED, TARGET_ALTITUED_REACHED, PLAN_COMPLETE, ZERO_ALTITUED_REACHED, LANDING_GRANTED, MANUAL_TAKEOFER;
	}

	private volatile StateMachine<FlightMode, FlightModeTransition> uavStateMachine;

	private final String uavid;

	private List<IManagedDroneStateChangeListener> listeners = new ArrayList<>();

	/**
	 * Constructor States for both FlightMode and SafetyMode set to initial state
	 * 
	 * @param uav
	 *          The UAV the state manager is assigned to.
	 */
	public DroneFlightStateManager(ManagedDrone uav) {
		this.uavid = uav.getDroneName();
		buildStateMachine();
	}

	private void buildStateMachine() {
		uavStateMachine = new StateMachine<>(FlightMode.ON_GROUND);
		uavStateMachine.configure(FlightMode.ON_GROUND).permit(FlightModeTransition.PLAN_ASSIGNED,
				FlightMode.AWAITING_TAKEOFF_CLEARANCE);
		uavStateMachine.configure(FlightMode.AWAITING_TAKEOFF_CLEARANCE).permit(FlightModeTransition.TAKEOFF_GRANTED,
				FlightMode.TAKING_OFF);
		uavStateMachine.configure(FlightMode.TAKING_OFF).permit(FlightModeTransition.TARGET_ALTITUED_REACHED,
				FlightMode.FLYING);
		uavStateMachine.configure(FlightMode.FLYING).permit(FlightModeTransition.PLAN_COMPLETE, FlightMode.IN_AIR);
		uavStateMachine.configure(FlightMode.IN_AIR).permit(FlightModeTransition.PLAN_ASSIGNED, FlightMode.FLYING);
		uavStateMachine.configure(FlightMode.IN_AIR).permit(FlightModeTransition.LANDING_GRANTED, FlightMode.LANDING);
		uavStateMachine.configure(FlightMode.LANDING).permit(FlightModeTransition.ZERO_ALTITUED_REACHED,
				FlightMode.ON_GROUND);

		uavStateMachine.configure(FlightMode.ON_GROUND).permit(FlightModeTransition.MANUAL_TAKEOFER,
				FlightMode.USER_CONTROLLED);
		uavStateMachine.configure(FlightMode.AWAITING_TAKEOFF_CLEARANCE).permit(FlightModeTransition.MANUAL_TAKEOFER,
				FlightMode.USER_CONTROLLED);
		uavStateMachine.configure(FlightMode.TAKING_OFF).permit(FlightModeTransition.MANUAL_TAKEOFER,
				FlightMode.USER_CONTROLLED);
		uavStateMachine.configure(FlightMode.FLYING).permit(FlightModeTransition.MANUAL_TAKEOFER,
				FlightMode.USER_CONTROLLED);
		uavStateMachine.configure(FlightMode.IN_AIR).permit(FlightModeTransition.MANUAL_TAKEOFER,
				FlightMode.USER_CONTROLLED);
		uavStateMachine.configure(FlightMode.LANDING).permit(FlightModeTransition.MANUAL_TAKEOFER,
				FlightMode.USER_CONTROLLED);

	}

	/**
	 * Set Flight Mode to OnGround
	 * 
	 * @throws FlightZoneException
	 *           if mode change does not follow allowed state transition.
	 */
	public void setModeToOnGround() throws FlightZoneException {
		FlightMode oldState = uavStateMachine.getState();
		if (uavStateMachine.canFire(FlightModeTransition.ZERO_ALTITUED_REACHED)) {
			uavStateMachine.fire(FlightModeTransition.ZERO_ALTITUED_REACHED);
			notifyStateChange(oldState, uavStateMachine.getState());

		} else {
			LOGGER.error("You may not transition from '" + uavStateMachine.getState() + "' with trigger '"
					+ FlightModeTransition.ZERO_ALTITUED_REACHED + "'");
			throw new FlightZoneException(
					"You may not transition to " + FlightMode.ON_GROUND + " directly from " + uavStateMachine.getState());
		}
	}

	/**
	 * Set Flight Mode to UserControlled
	 * 
	 */ 
	public void setModeToUserControlled() throws FlightZoneException {
		FlightMode oldState = uavStateMachine.getState();
		if (uavStateMachine.canFire(FlightModeTransition.MANUAL_TAKEOFER)) {
			uavStateMachine.fire(FlightModeTransition.MANUAL_TAKEOFER);
			notifyStateChange(oldState, uavStateMachine.getState());

		} else {
			LOGGER.error("You may not transition from '" + uavStateMachine.getState() + "' with trigger '"
					+ FlightModeTransition.MANUAL_TAKEOFER + "'");
			throw new FlightZoneException(
					"You may not transition to " + FlightMode.ON_GROUND + " directly from " + uavStateMachine.getState());
		}

	}

	/**
	 * Set Flight mode to awaiting Takeoff Clearance
	 * 
	 * @throws FlightZoneException
	 *           if mode change does not follow allowed state transition.
	 */
	public void setModeToAwaitingTakeOffClearance() throws FlightZoneException {
		FlightMode oldState = uavStateMachine.getState();
		if (uavStateMachine.canFire(FlightModeTransition.PLAN_ASSIGNED)) {
			uavStateMachine.fire(FlightModeTransition.PLAN_ASSIGNED);
			notifyStateChange(oldState, uavStateMachine.getState());
		} else {
			LOGGER.error("You may not transition from '" + uavStateMachine.getState() + "' with trigger '"
					+ FlightModeTransition.PLAN_ASSIGNED + "'");
			throw new FlightZoneException("You may not transition to " + FlightMode.AWAITING_TAKEOFF_CLEARANCE
					+ " directly from " + uavStateMachine.getState());
		}

	}

	/**
	 * Set flight mode to Taking off
	 * 
	 * @throws FlightZoneException
	 *           if mode change does not follow allowed state transition.
	 */
	public void setModeToTakingOff() throws FlightZoneException {
		FlightMode oldState = uavStateMachine.getState();
		if (uavStateMachine.canFire(FlightModeTransition.TAKEOFF_GRANTED)) {
			uavStateMachine.fire(FlightModeTransition.TAKEOFF_GRANTED);
			notifyStateChange(oldState, uavStateMachine.getState());
		} else {
			LOGGER.error("You may not transition from '" + uavStateMachine.getState() + "' with trigger '"
					+ FlightModeTransition.TAKEOFF_GRANTED + "'");
			throw new FlightZoneException(
					"You may not transition to " + FlightMode.TAKING_OFF + " directly from " + uavStateMachine.getState());
		}

	}

	/**
	 * Set flight mode to Flying
	 * 
	 * @throws FlightZoneException
	 *           if mode change does not follow allowed state transition.
	 */
	public void setModeToFlying() throws FlightZoneException {
		FlightMode oldState = uavStateMachine.getState();
		if (uavStateMachine.canFire(FlightModeTransition.TARGET_ALTITUED_REACHED)) {
			uavStateMachine.fire(FlightModeTransition.TARGET_ALTITUED_REACHED);
			notifyStateChange(oldState, uavStateMachine.getState());
		} else if (uavStateMachine.canFire(FlightModeTransition.PLAN_ASSIGNED)) {
			uavStateMachine.fire(FlightModeTransition.PLAN_ASSIGNED);
			notifyStateChange(oldState, uavStateMachine.getState());
		} else {
			LOGGER.error("You may not transition from '" + uavStateMachine.getState() + "' with trigger '"
					+ FlightModeTransition.TARGET_ALTITUED_REACHED + "'");
			throw new FlightZoneException(
					"You may not transition to " + FlightMode.FLYING + " directly from " + uavStateMachine.getState());
		}

	}

	/**
	 * Set flight mode to Landing
	 * 
	 * @throws FlightZoneException
	 *           if mode change does not follow allowed state transition.
	 */
	public void setModeToLanding() throws FlightZoneException {
		FlightMode oldState = uavStateMachine.getState();
		if (uavStateMachine.canFire(FlightModeTransition.LANDING_GRANTED)) {
			uavStateMachine.fire(FlightModeTransition.LANDING_GRANTED);
			notifyStateChange(oldState, uavStateMachine.getState());
		} else {
			LOGGER.error("You may not transition from '" + uavStateMachine.getState() + "' with trigger '"
					+ FlightModeTransition.LANDING_GRANTED + "'");
			throw new FlightZoneException(
					"You may not transition to " + FlightMode.LANDING + " directly from " + uavStateMachine.getState());
		}

	}

	/**
	 * 
	 * @return true if drone is currently on the ground, false otherwise
	 */
	public boolean isOnGround() {
		return uavStateMachine.getState() == FlightMode.ON_GROUND;

	}

	/**
	 * 
	 * @return true if drone is currently in AwaitingTakeOffClearance mode, false otherwise
	 */
	public boolean isAwaitingTakeoffClearance() {
		return uavStateMachine.getState() == FlightMode.AWAITING_TAKEOFF_CLEARANCE;

	}

	/**
	 * 
	 * @return true if drone is currently taking off, false otherwise
	 */
	public boolean isTakingOff() {
		return uavStateMachine.getState() == FlightMode.TAKING_OFF;
	}

	/**
	 * 
	 * @return true if drone is currently flying, false otherwise
	 */
	public boolean isFlying() {
		return uavStateMachine.getState() == FlightMode.FLYING;

	}

	/**
	 * 
	 * @return true if drone is currently landing, false otherwise
	 */
	public boolean isLanding() {
		return uavStateMachine.getState() == FlightMode.LANDING;

	}

	/**
	 * 
	 * @return current status
	 */
	public String getStatus() {
		return uavStateMachine.getState().toString();
		// return currentFlightMode.toString();
	}

	private synchronized void notifyStateChange(FlightMode oldState, FlightMode newState) {
		LOGGER.info("Drone '" + uavid + "' set to: " + uavStateMachine.getState());
		DronologyMonitoringManager.getInstance().publish(MessageMarshaller.create(uavid, oldState, newState));
		for (IManagedDroneStateChangeListener listener : listeners) {
			listener.notifyStateChange();
		}
	}

	public boolean isInAir() {
		return uavStateMachine.getState() == FlightMode.IN_AIR;
	}

	public void setModeToInAir() throws FlightZoneException {
		FlightMode oldState = uavStateMachine.getState();
		if (uavStateMachine.canFire(FlightModeTransition.PLAN_COMPLETE)) {
			uavStateMachine.fire(FlightModeTransition.PLAN_COMPLETE);
			notifyStateChange(oldState, uavStateMachine.getState());
		} else {
			LOGGER.error("You may not transition from '" + uavStateMachine.getState() + "' with trigger '"
					+ FlightModeTransition.PLAN_COMPLETE + "'");
			throw new FlightZoneException(
					"You may not transition to " + FlightMode.IN_AIR + " directly from " + uavStateMachine.getState());
		}
	}

	public void addStateChangeListener(IManagedDroneStateChangeListener listener) {
		listeners.add(listener);

	}

}
