package edu.nd.dronology.core.vehicle;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.google.common.util.concurrent.RateLimiter;

import edu.nd.dronology.core.Discuss;
import edu.nd.dronology.core.DronologyConstants;
import edu.nd.dronology.core.collisionavoidance.DroneSnapshot;
import edu.nd.dronology.core.collisionavoidance.DroneSnapshotInternal;
import edu.nd.dronology.core.collisionavoidance.DroneSnapshotOption;
import edu.nd.dronology.core.collisionavoidance.guidancecommands.Command;
import edu.nd.dronology.core.collisionavoidance.guidancecommands.NedCommand;
import edu.nd.dronology.core.collisionavoidance.guidancecommands.StopCommand;
import edu.nd.dronology.core.collisionavoidance.guidancecommands.WaypointCommand;
import edu.nd.dronology.core.coordinate.AbstractPosition;
import edu.nd.dronology.core.goal.AbstractGoal;
import edu.nd.dronology.core.goal.AbstractGoal.GoalState;
import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.core.exceptions.DroneException;
import edu.nd.dronology.core.exceptions.FlightZoneException;
import edu.nd.dronology.core.fleet.DroneFleetManager;
import edu.nd.dronology.core.flight.FlightDirectorFactory;
import edu.nd.dronology.core.flight.IFlightDirector;
import edu.nd.dronology.core.monitoring.DronologyMonitoringManager;
import edu.nd.dronology.core.monitoring.MessageMarshaller;
import edu.nd.dronology.core.monitoring.messages.UAVMonitorableMessage.MessageType;
import edu.nd.dronology.core.goal.IGoalSnapshot;
import edu.nd.dronology.core.goal.WaypointGoal;
import edu.nd.dronology.core.util.Waypoint;
import edu.nd.dronology.core.vehicle.commands.AbstractDroneCommand;
import edu.nd.dronology.core.vehicle.commands.EmergencyStopCommand;
import edu.nd.dronology.core.vehicle.manageddroneinternal.*;
import edu.nd.dronology.core.vehicle.manageddroneinternal.message.AbstractMessage;
import edu.nd.dronology.core.vehicle.manageddroneinternal.message.AssignFlightMessage;
import edu.nd.dronology.core.vehicle.manageddroneinternal.message.GetSnapshotMessage;
import edu.nd.dronology.core.vehicle.manageddroneinternal.message.GoalUpdateMessage;
import edu.nd.dronology.core.vehicle.manageddroneinternal.message.UnassignFlightMessage;
import edu.nd.dronology.core.vehicle.manageddroneinternal.message.UpdateGuidanceMessage;
import edu.nd.dronology.core.vehicle.proxy.UAVProxyManager;
import edu.nd.dronology.util.NamedThreadFactory;
import edu.nd.dronology.util.NullUtil;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

/**
 * 
 * Handler class for both {@link VirtualDrone} and {@link PhysicalDrone}.<br>
 * Handles basic functionality that is independent of a virtual or physical endpoint.<br>
 * Contains information on coordinates, state, and flight instructions.
 *  
 *  
 * @author Jane Cleland-Huang
 */
@Discuss(discuss = "does the drone need to be observable?")
public class ManagedDrone extends Observable implements Runnable, Observer {
	// region fields
	private static final ILogger LOGGER = LoggerProvider.getLogger(ManagedDrone.class);

	private AtomicBoolean cont = new AtomicBoolean(true);

	private RateLimiter LIMITER = RateLimiter.create(10);

	private static final ExecutorService EXECUTOR_SERVICE = Executors
			.newFixedThreadPool(DronologyConstants.MAX_DRONE_THREADS, new NamedThreadFactory("ManagedDrone"));

	private final IDrone drone; // Controls primitive flight commands for drone

	private DroneFlightStateManager droneState;
	private DroneSafetyStateManager droneSafetyState;

	@Discuss(discuss = "why not final? - new flight director for each flight??")
	private IFlightDirector flightDirector = null; // Each drone can be assigned
	// a single flight plan.
	private volatile double targetAltitude = 0;

	private Timer haltTimer = new Timer();
	private HaltTimerTask currentHaltTimer;

	private List<AbstractGoal> goals;
	private Set<AbstractGoal> activeGoals = new HashSet<>();
	private List<Command> commandQueue = new ArrayList<>();
	private List<Command> completedCommands = new ArrayList<>();
	private Command currentCommand;
	private CommandExecutor currentExecutor;

	private final ManagedDroneMessenger messenger;
	// TODO figure out what this value should be
	private static final int MAILBOX_CAPACITY = 50;
	private static final boolean FIFO_MAILBOX = true;
	private final ArrayBlockingQueue<AbstractMessage> mailbox = new ArrayBlockingQueue<>(MAILBOX_CAPACITY,
			FIFO_MAILBOX);
	private final CommandExecutorFactory executorFactory;
	private final CountDownLatch startGate = new CountDownLatch(1);
	// endregion

	/**
	 * Constructs drone
	 *
	 * @param drone
	 */
	public ManagedDrone(IDrone drone) {
		NullUtil.checkNull(drone);
		this.drone = drone;// specify
		droneState = new DroneFlightStateManager(this);
		droneSafetyState = new DroneSafetyStateManager();
		drone.getDroneStatus().setStatus(droneState.getStatus());
		this.flightDirector = FlightDirectorFactory.getFlightDirector(this); // Don't
		droneState.addStateChangeListener(() -> notifyStateChange());
		this.goals = new ArrayList<>();
		this.messenger = new ManagedDroneMessenger(mailbox);
		this.executorFactory = new CommandExecutorFactory(this.drone);
	}

	private class HaltTimerTask extends TimerTask {

		@Override
		public void run() {
			synchronized (droneSafetyState) {
				if (!droneSafetyState.isSafetyModeHalted()) {
					currentHaltTimer = null;
					return;
				}

				try {
					droneSafetyState.setSafetyModeToNormal();
					droneState.setModeToFlying();
					currentHaltTimer = null;
				} catch (FlightZoneException e) {
					LOGGER.error(e);
				}
			}
		}

	}

	// region public methods
	/**
	 * Get the messenger associated with this drone. Client code needing to access
	 * this drone's functionality must do so through the messenger.
	 * 
	 * @return The messenger that can command this managed drone to carry out
	 *         operations.
	 */
	public ManagedDroneMessenger getMessenger() {
		return messenger;
	}

	/**
	 * Blocks until the ManagedDrone run method has been called.
	 */
	public void awaitStart() {
		try {
			this.startGate.await();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public void returnToHome() {
		synchronized (droneSafetyState) {
			getFlightSafetyModeState().setSafetyModeToNormal();

			if (currentHaltTimer != null) {
				currentHaltTimer.cancel();
				currentHaltTimer = null;
			}

		}

	}

	/**
	 *
	 * @param targetAltitude Sets target altitude for takeoff
	 */
	public void setTargetAltitude(double targetAltitude) {
		this.targetAltitude = targetAltitude;
	}

	/**
	 * Controls takeoff of drone
	 *
	 * @throws FlightZoneException
	 */
	public void takeOff() throws FlightZoneException {
		if (targetAltitude == 0) {
			throw new FlightZoneException("Target Altitude is 0");
		}
		droneState.setModeToTakingOff();
		drone.takeOff(targetAltitude);

	}

	/**
	 * Delegates flyto behavior to virtual or physical drone
	 *
	 * @param targetCoordinates
	 * @param speed
	 */
	public void flyTo(LlaCoordinate targetCoordinates, Double speed) {
		drone.flyTo(targetCoordinates, speed);
	}

	/**
	 * Gets current coordinates from virtual or physical drone
	 *
	 * @return current coordinates
	 */
	public LlaCoordinate getCoordinates() {
		return drone.getCoordinates();
	}

	public void start() {
		// thread.start();
		LOGGER.info("Starting Drone '" + drone.getDroneName() + "'");
		EXECUTOR_SERVICE.submit(this);
	}

	public boolean isStarted() {
    	try {
			return this.startGate.await(0, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			LOGGER.error(e);
			return false;
		}
	}
		

	@Override
	public void run() {
		try {
			this.startGate.countDown();
			while (cont.get() && !Thread.currentThread().isInterrupted()) {
				LIMITER.acquire();
				
				processAllMessages();

				// Probably not necessary anymore... TODO: fix- do not try to assign point in
				// every iteration of the loop...
				if (flightDirector != null && droneState.isFlying()) {
					this.flyDrone();
					/*
					 * LlaCoordinate targetCoordinates = flightDirector.flyToNextPoint();
					 * drone.flyTo(); if (!drone.move(0.1)) {
					 * LOGGER.missionInfo(drone.getDroneName() + " - Waypoint reached - " +
					 * targetCoordinates.toString()); flightDirector.clearCurrentWayPoint(); }
					 */
					checkForEndOfFlight();
				}
				if (droneState.isTakingOff()) {
					if (Math.abs(drone.getAltitude() - targetAltitude) < DronologyConstants.THRESHOLD_TAKEOFF_HEIGHT) {
						LOGGER.info("Target Altitude reached - ready for flying");
						try {
							droneState.setModeToFlying();
						} catch (FlightZoneException e) {
							LOGGER.error(e);
						}
					}
				}
			}
		} catch (Throwable e) {
			LOGGER.error(e);
		}
		LOGGER.info("UAV-Thread '" + drone.getDroneName() + "' terminated");
		UAVProxyManager.getInstance().removeDrone(getDroneName());
	}
	// needs refactoring to improve performance...
	public boolean permissionForTakeoff() {
		double dronDistance = 0;
		List<ManagedDrone> flyingDrones = DroneFleetManager.getInstance().getRegisteredDrones();
		for (ManagedDrone drone2 : flyingDrones) {
			if (!this.equals(flyingDrones) 
					&& (drone2.getFlightModeState().isFlying() || drone2.getFlightModeState().isInAir())) {
				dronDistance = this.getCoordinates().distance(drone2.getCoordinates());
				if (dronDistance < DronologyConstants.SAFETY_ZONE) {
					LOGGER.error("Safety Distance Violation - Drone not allowed to TakeOff! distance: " + dronDistance
							+ " safety zone: " + DronologyConstants.SAFETY_ZONE + " => " + dronDistance);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 *
	 * @return unique drone ID
	 */
	public String getDroneName() {
		return drone.getDroneName();
	}

	/**
	 * Land the drone. Delegate land functions to virtual or physical drone
	 *
	 * @throws FlightZoneException
	 */
	public void land() throws FlightZoneException {
		if (!droneState.isLanding() || !droneState.isOnGround()) {
			droneState.setModeToLanding();
			drone.land();
			droneState.setModeToOnGround();
			unassignFlight();
		}
	}

	/**
	 * Temporarily Halt
	 *
	 * @param haltinms
	 */
	public void haltInPlace(int haltinms) {
		synchronized (droneSafetyState) {
			try {
				if (currentHaltTimer != null) {
					return;
					// currentHaltTimer.cancel();
					// droneSafetyState.setSafetyModeToNormal();
					// droneState.setModeToFlying();
					// currentHaltTimer = null;
				} else {
					droneSafetyState.setSafetyModeToHalted();
					droneState.setModeToInAir();
					currentHaltTimer = new HaltTimerTask();
					haltTimer.schedule(currentHaltTimer, haltinms);
				}

			} catch (FlightZoneException e) {
				LOGGER.error(e);
			}
		}
	}

	/**
	 * Temporarily Halt
	 * 
	 * @param haltinms
	 * @throws FlightZoneException
	 */
	public void resumeFlight() throws FlightZoneException {
		synchronized (droneSafetyState) {
			if (currentHaltTimer == null) {
				throw new FlightZoneException("UAV not halted");
			} else {
				currentHaltTimer.cancel();
				droneSafetyState.setSafetyModeToNormal();
				droneState.setModeToFlying();
				currentHaltTimer = null;
			}
		}
	}

	/**
	 * 
	 *
	 * return current flight mode state
	 *
	 * @return droneState
	 */
	public DroneFlightStateManager getFlightModeState() {
		return droneState;
	}

	public LlaCoordinate getBaseCoordinates() {
		return drone.getBaseCoordinates();
	}

	public void sendCommand(AbstractDroneCommand command) throws DroneException {
		drone.sendCommand(command);

	}

	public void stop() {
		if (!droneState.isOnGround()) {
			LOGGER.warn("Removing UAV '" + drone.getDroneName() + "' while in state " + droneState.getStatus());
		} else {
			LOGGER.info("Removing UAV '" + drone.getDroneName() + "'");
		}
		cont.set(false);
		haltTimer.cancel();
	}

	public void emergencyStop() throws DroneException {
		LOGGER.warn("Emergency stop for UAV '" + drone.getDroneName() + "' requested");
		sendCommand(new EmergencyStopCommand(drone.getDroneName()));

	}

	public void resendCommand() throws DroneException {
		drone.resendCommand();

	}

	public DroneSnapshot getSnapshot() {
		DroneSnapshotInternal snapshot = drone.getLatestDroneSnapshot();
		if (snapshot == null) {
			LOGGER.debug("The backend drone didn't provide a snapshot");
			return null;
		}
		snapshot.setName(drone.getDroneName());
		snapshot.getCommands().clear();
		for (Command cmd : commandQueue) {
			snapshot.getCommands().add(cmd);
		}

		snapshot.setState(droneState.getStatus());

		HashSet<IGoalSnapshot> activeGoalSnapshots = new HashSet<>();
		// need a thread safe way to copy the set of active goals
		for (AbstractGoal goal : activeGoals) {
			activeGoalSnapshots.add(goal.buildSnapshot());
		}
		snapshot.setGoals(activeGoalSnapshots);

		return snapshot;
	}

	@Override
	public void update(Observable observable, Object o) {
		if (observable instanceof AbstractGoal) {
			this.messenger.updateGoal((AbstractGoal) observable);
		}
	}
	
	private void updateGoals(AbstractGoal goal) {
		GoalState state = goal.getState();
		if (state == GoalState.ACTIVE) {
			activeGoals.add(goal);
		} else {
			activeGoals.remove(goal);
		}
	}
	// endregion

	// region private methods

	private void processAllMessages() {
		try {
			AbstractMessage msg = this.mailbox.poll(0, TimeUnit.NANOSECONDS);
			while (msg != null) {
				processesMessage(msg);
				msg = this.mailbox.poll(0, TimeUnit.NANOSECONDS);
			}
			
		} catch (Exception e) {
			LOGGER.error(e);
		}
	}

	private void processesMessage(AbstractMessage msg) {
		if (msg instanceof GetSnapshotMessage) {
			processGetSnapshotMessage((GetSnapshotMessage) msg);
		} else if (msg instanceof UpdateGuidanceMessage) {
			processUpdateGuidanceMessage((UpdateGuidanceMessage) msg);
		} else if (msg instanceof AssignFlightMessage) {
			processAssignFlightMessage((AssignFlightMessage) msg);
		} else if (msg instanceof UnassignFlightMessage) {
			processUnassignFlightMessage((UnassignFlightMessage) msg);
		} else if (msg instanceof GoalUpdateMessage) {
			processGoalUpdateMessage((GoalUpdateMessage) msg);
		}
	}

	private void processGetSnapshotMessage(GetSnapshotMessage msg) {
		try {
			msg.returnBox.put(new DroneSnapshotOption(this.getSnapshot(), this.getDroneName()));
		} catch (InterruptedException e) {
			e.printStackTrace();
			LOGGER.error(e);
		}
	}

	private void processUpdateGuidanceMessage(UpdateGuidanceMessage msg) {
		this.updateGuidance(msg.commands);
	}

	private void processAssignFlightMessage(AssignFlightMessage msg) {
		assignFlight(msg.flightDirective);
	}

	private void processUnassignFlightMessage(UnassignFlightMessage msg) {
		unassignFlight();
	}

	private void processGoalUpdateMessage(GoalUpdateMessage msg) {
		updateGoals(msg.goal);
	}

	private void notifyStateChange() {
		drone.getDroneStatus().setStatus(droneState.getStatus());
	}

		/**
	 * Assigns a flight directive to the managed drone
	 *
	 * @param flightDirective
	 */
	private void assignFlight(IFlightDirector flightDirective) {
		LOGGER.debug("Assigning flight director");
		clearGoals();
		this.flightDirector = flightDirective;

		LOGGER.trace("The flight has " + flightDirector.getWayPoints().size() + " waypoints");
		// Create waypoint goals for each waypoint in the flight director.
		for (Waypoint wp : flightDirector.getWayPoints()) {
			WaypointGoal wpg = new WaypointGoal(wp);

			// Add an observer that will update the GUI when this waypoint is reached.
			wpg.addObserver((observable, o) -> {
				GoalState state = ((AbstractGoal) observable).getState();

				if (state == GoalState.COMPLETE) {
					wp.reached(true);
					DronologyMonitoringManager.getInstance().publish( 
						MessageMarshaller.createMessage(MessageType.WAYPOINT_REACHED, drone.getDroneName(), wp.getCoordinate()));
				}
			});

			addGoal(wpg);
		}

		completedCommands.clear();
		commandQueue.clear();

		if (!goals.isEmpty()) {
			Thread tmpThread = new Thread(new Runnable(){
			
				@Override
				public void run() {
					goals.get(0).setActive();
				}
			});
			tmpThread.start();
			int lastIndex = goals.size() - 1;
			goals.get(lastIndex).addObserver((observable, o) -> {
				GoalState state = ((AbstractGoal) observable).getState();

				if (state == GoalState.COMPLETE) {
					try {
						droneState.setModeToInAir();
					} catch (FlightZoneException e) {
						LOGGER.warn(e);
					}
				}
			});
		}
	}

	/**
	 * Removes an assigned flight
	 */
	private void unassignFlight() {
		flightDirector = null; // DANGER. NEEDS FIXING. CANNOT UNASSIGN FLIGHT
		// WITHOUT RETURNING TO BASE!!!
		LOGGER.warn("Unassigned DRONE: " + getDroneName());
		clearGoals();
	}

	// Check for end of flight. Land if conditions are satisfied
	private boolean checkForEndOfFlight() {
		if (flightDirector != null && flightDirector.readyToLand())
			return false; // it should have returned here.
		if (droneState.isLanding())
			return false;
		if (droneState.isOnGround())
			return false;
		if (droneState.isInAir())
			return false;

		// Otherwise
		try {
			land();
		} catch (FlightZoneException e) {
			LOGGER.error(getDroneName() + " is not able to land!", e);
		}
		return true;
	}

	/**
	 *
	 * @return current safety mode state
	 */
	public DroneSafetyStateManager getFlightSafetyModeState() {
		return droneSafetyState;
	}

	private void flyDrone() {
		// check to see if we are flying the right command
		if (commandQueue.size() < 1) {
			commandQueue.add(new StopCommand(0.0));
		}
		if (commandQueue.get(0) != currentCommand) {
			currentCommand = commandQueue.get(0);
			currentExecutor = executorFactory.makeExecutor(currentCommand);
		}

		currentExecutor.process();

		if (currentExecutor.isFinished()) {
			Command current = commandQueue.remove(0);
			completedCommands.add(current);
			LOGGER.missionInfo(drone.getDroneName() + " command completed: " + current.toString());
		}
	}

	private boolean isCompleted(Command ref) {
		NullUtil.checkNull(ref);
		int index = 0;
		while (index < completedCommands.size()) {
			if (completedCommands.get(index) == ref) {
				return true;
			}
			++index;
		}
		return false;
	}

	private void updateGuidance(List<Command> commands) {
		LOGGER.debug("New guidance from CA: " + commands.stream().map(Object::toString).collect(Collectors.joining(", ", "[", "]")));
		NullUtil.checkNull(commands);
		// find the first item in commands that is not completed
		// what if the first command is new and a later command is completed?
		int index = 0;

		while (index < commands.size() && isCompleted(commands.get(index))) {
			++index;
		}

		commandQueue.clear();
		if (index >= commands.size()) {
			commandQueue.add(new StopCommand(0.0));
		} else {
			List<Command> remaining = commands.subList(index, commands.size());
			for (Command cmd : remaining) {
				commandQueue.add(cmd);
			}
		}
	}

	private void clearGoals() {
		goals.clear();
		activeGoals.clear();
	}

	private void addGoal(AbstractGoal goal) {
		NullUtil.checkNull(goal);

		// ManagedDrone should observe each goal.
		// When the goal changes state, ManagedDrone needs to update its active goals.
		goal.addObserver(this);
		((AbstractDrone) drone).addObserver(goal);

		// If the new goal is a WaypointGoal, it needs to observe the previous
		// WaypointGoal (if one exists).
		if (goal instanceof WaypointGoal) {
			int lastWaypointIdx = -1;

			// Find the index of the previous waypoint goal.
			for (int i = 0; i < goals.size(); i++) {
				if (goals.get(i) instanceof WaypointGoal) {
					lastWaypointIdx = i;
				}
			}

			// If there is a previous waypoint goal, make the new goal an observe of it.
			// When the previous waypoint is completed, the new goal will become active.
			if (lastWaypointIdx >= 0) {
				goals.get(lastWaypointIdx).addObserver(goal);
			}

		}

		goals.add(goal);
	}
	// endregion
}
