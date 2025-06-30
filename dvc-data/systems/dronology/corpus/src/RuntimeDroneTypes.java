package edu.nd.dronology.core.fleet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.nd.dronology.core.exceptions.DroneException;
import edu.nd.dronology.core.exceptions.FlightZoneException;
import edu.nd.dronology.core.vehicle.IDroneCommandHandler;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

/**
 * This is a SINGLETON class. To get an instance call getInstance() Sets the runtime mode of FlightZone either as SIMULATION or PHYSICAL The mode may only be set one time during execution. Simulation
 * uses soft drone objects. Physical uses real drones controlled using Dronekit API.
 * 
 * @author Jane Cleland-Huang 
 *
 */
public class RuntimeDroneTypes {
	private enum Mode {
		SIMULATION, PHYSICAL
	}

	private static final ILogger LOGGER = LoggerProvider.getLogger(RuntimeDroneTypes.class);

	private Mode currentMode;

	Map<String, IDroneCommandHandler> commandHandlers = new ConcurrentHashMap<>();
	private static volatile RuntimeDroneTypes INSTANCE = null;

	protected RuntimeDroneTypes() {
	}

	public static RuntimeDroneTypes getInstance() {
		if (INSTANCE == null) {
			synchronized (RuntimeDroneTypes.class) {
				if (INSTANCE == null) {
					INSTANCE = new RuntimeDroneTypes();
				}
			}
		}
		return INSTANCE;
	}

	/**
	 * Sets flightmode to SIMULATION. Does not allow the flight mode to be reset after it is initially set.
	 * 
	 * @throws FlightZoneException
	 */
	public void setVirtualEnvironment() throws FlightZoneException {
		if (currentMode == null) {
			currentMode = Mode.SIMULATION;
		} else if (currentMode == Mode.PHYSICAL) {
			// Do nothing
		} else
			throw new FlightZoneException(
					"Flight mode has already been set to PHYSICAL.  You may not reset to SIMULATION runtime.");
	}

	/**
	 * Sets flightmode to PHYSICAL Does not allow the flight mode to be reset after it is initially set.
	 * 
	 * @throws FlightZoneException
	 */
	public void setPhysicalEnvironment() throws FlightZoneException {
		if (currentMode == null) {
			currentMode = Mode.PHYSICAL;
		} else if (currentMode == Mode.SIMULATION) {
			// Do nothing
		} else
			throw new FlightZoneException(
					"Flight mode has already been set to SIMULATION.  You may not reset to PHYSICAL runtime.");
	}

	/**
	 * 
	 * @return true if in SIMULATION mode. False otherwise.
	 */
	public boolean isSimulation() {
		return currentMode == Mode.SIMULATION;
	}

	/**
	 * 
	 * @return true if in PHYSICAL mode. False otherwise.
	 */
	public boolean isPhysical() {
		return currentMode == Mode.PHYSICAL;
	}

	public void registerCommandHandler(IDroneCommandHandler commandHandler) throws DroneException {
		String handlerId = commandHandler.getHandlerId();
		LOGGER.info("Drone command handler added '" + handlerId + "' - " + commandHandler.getClass());
		if (commandHandlers.containsKey(handlerId)) {
			throw new DroneException("CommandHandler with id '" + handlerId + "' already registered");
		}
		commandHandlers.put(handlerId, commandHandler);
	}

	public IDroneCommandHandler getCommandHandler(String handlerId) throws DroneException {
		if (!commandHandlers.containsKey(handlerId)) {
			throw new DroneException("CommandHandler with id '" + handlerId + "' not registered");
		}
		return commandHandlers.get(handlerId);
	}

	public void unregisterCommandHandler(String handlerId) throws DroneException {
		// String handlerId = commandHandler.getHandlerId();
		// LOGGER.info("Drone command handler added '" + handlerId + "' - " + commandHandler.getClass());
		if (!commandHandlers.containsKey(handlerId)) {
			throw new DroneException("CommandHandler with id '" + handlerId + "' not registered");
		}
		commandHandlers.remove(handlerId);
		LOGGER.info("Drone command handler removed '" + handlerId);
	}
}
