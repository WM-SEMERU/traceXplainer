package edu.nd.dronology.core.fleet;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;

import edu.nd.dronology.core.exceptions.DroneException;
import edu.nd.dronology.core.monitoring.DronologyMonitoringManager;
import edu.nd.dronology.core.monitoring.MessageMarshaller;
import edu.nd.dronology.core.monitoring.messages.UAVMonitorableMessage.MessageType;
import edu.nd.dronology.core.vehicle.ManagedDrone;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

/**
 * Holds a fleet of virtual or physical drones.<br>
 * When activated, UAVs are registered with the fleet manager and can be retrieved for assigning routes to them.
 * 
 *  
 * @author Michael Vierhauser
 *
 */
public class DroneFleetManager {

	private static final ILogger LOGGER = LoggerProvider.getLogger(DroneFleetManager.class);
	private static volatile DroneFleetManager INSTANCE = null;

	private ConcurrentSkipListMap<String, ManagedDrone> registeredDrones;
	private Queue<ManagedDrone> availableDrones;
	private List<ManagedDrone> busyDrones;
	private List<DroneFleetListener> listeners;

	public static DroneFleetManager getInstance() {

		if (INSTANCE == null) {
			synchronized (DroneFleetManager.class) {
				if (INSTANCE == null) {
					INSTANCE = new DroneFleetManager();
				}
			}
		}
		return INSTANCE;
	}

	/**
	 * Specifies whether virtual or physical drones will be created according to the previously specified runtime drone type. (See RuntimeDroneTypes.java)
	 */
	protected DroneFleetManager() {
		// if (RuntimeDroneTypes.getInstance().isSimulation())
		// availableDrones = VirtualDroneFleetFactory.getInstance().getDrones();
		// else
		// availableDrones =
		// PhysicalDroneFleetFactory.getInstance().getDrones();
		registeredDrones = new ConcurrentSkipListMap();
		availableDrones = new ConcurrentLinkedQueue<>();
		busyDrones = new ArrayList<>();
		listeners = new ArrayList<>();
	}

	/**
	 * Checks for an available drone from the fleet.
	 * 
	 * @return true if drone is available, false if it is not.
	 */
	public boolean hasAvailableDrone() {
		LOGGER.info("Drones available: " + availableDrones.size());
		return availableDrones.size() > 0;

	}

	/**
	 * Returns the next available drone. Currently uses FIFO to recycle drones.
	 * 
	 * @return
	 */
	public ManagedDrone getAvailableDrone() {
		if (!availableDrones.isEmpty()) {
			ManagedDrone drone = availableDrones.poll();
			busyDrones.add(drone);
			return drone;
		} else
			return null;
	}

	public ManagedDrone getAvailableDrone(String designatedDroneId) {
		synchronized (availableDrones) {
			ManagedDrone found = null;
			if (!availableDrones.isEmpty()) {
				for (ManagedDrone d : availableDrones) {
					if (d.getDroneName().equals(designatedDroneId)) {
						found = d;
						break;
					}
				}
				if (found != null) {
					boolean success = availableDrones.remove(found);
					if (success) {
						busyDrones.add(found);
						return found;
					} else {
						LOGGER.error("Error when queuing uav '" + designatedDroneId + "'");
					}
				}
				// LOGGER.error("Error when retrieving uav '" + designatedDroneId + "'");
			}
			return null;
		}
	}

	private void notifyListeners(boolean add, ManagedDrone managedDrone) {
		Consumer<DroneFleetListener> update; 
		if (add) {
			update = listener -> listener.droneAdded(managedDrone);
		} else {
			update = listener -> listener.droneRemoved(managedDrone);
		}
		listeners.forEach(update);
	}

	/**
	 * When a drone completes a mission, returns it to the pool of available drones.
	 * 
	 * @param drone
	 */
	public void returnDroneToAvailablePool(ManagedDrone drone) {
		if (busyDrones.contains(drone)) {
			busyDrones.remove(drone);
		}
		// LOGGER.info("Drone '"+drone.getDroneName()+"' added to available drone
		// pool");
		availableDrones.offer(drone);

	}

	public void addDrone(ManagedDrone managedDrone) throws DroneException {
		if (registeredDrones.containsKey(managedDrone.getDroneName())) {
			throw new DroneException("Drone '" + managedDrone.getDroneName() + "' already registered");
		}
		registeredDrones.put(managedDrone.getDroneName(), managedDrone);
		returnDroneToAvailablePool(managedDrone);
		notifyListeners(true, managedDrone);

	}

	public void removeDrone(ManagedDrone managedDrone) throws DroneException {
		ManagedDrone value = registeredDrones.remove(managedDrone.getDroneName());
		if (value == null) {
			throw new DroneException("Drone '" + managedDrone.getDroneName() + "' not found registered");
		}
		LOGGER.info("Drone '" + managedDrone.getDroneName() + "' removed from available drone pool");
		DronologyMonitoringManager.getInstance()
				.publish(MessageMarshaller.createMessage(MessageType.PHYSICAL_UAV_DEACTIVATED, managedDrone.getDroneName()));
		availableDrones.remove(value);
		// TODO change how this class uses managed drone. Stopping must be sent to the drone using a goal model.
		value.stop();
		notifyListeners(false, managedDrone);
	}

	public ManagedDrone getRegisteredDrone(String uavid) throws DroneException {
		if (!registeredDrones.containsKey(uavid)) {
			throw new DroneException("Drone '" + uavid + "' not found registered");
		}
		return registeredDrones.get(uavid);

	}

	public List<ManagedDrone> getRegisteredDrones() { 
		return new ArrayList<>(registeredDrones.values());

	}

	public void unregisterDroe(String id) throws DroneException {
		ManagedDrone managedDrone = getRegisteredDrone(id);
		removeDrone(managedDrone);
	}

	public void addDroneFleetListener(DroneFleetListener listener) {
		listeners.add(listener);
	}

}
