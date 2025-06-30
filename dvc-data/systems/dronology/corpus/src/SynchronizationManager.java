package edu.nd.dronology.services.extensions.missionplanning.sync;

import java.util.HashMap;
import java.util.Map;

import edu.nd.dronology.core.DronologyConstants;
import edu.nd.dronology.services.extensions.missionplanning.MissionExecutionException;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

/**
 * The {@link SynchronizationManager} holds a list of {@link SynchronizationPoint} and ensures that a point is fully synced<br>
 * before allowing UAVs to proceed with the next task.
 * 
 * @author Jane Cleland-Huang 
 * 
 * 
 *
 */
public class SynchronizationManager {
	private static final ILogger LOGGER = LoggerProvider.getLogger(SynchronizationManager.class);

	private Map<String, SynchronizationPoint> synchPoints;
	private int availableAltitude = DronologyConstants.MISSION_TAKEOFF_MIN_ALTITUDE;

	private static transient SynchronizationManager INSTANCE;

	private SynchronizationManager() {
		synchPoints = new HashMap<>();
	}

	public static SynchronizationManager getInstance() {
		if (INSTANCE == null) {
			synchronized (SynchronizationManager.class) {
				if (INSTANCE == null) {
					INSTANCE = new SynchronizationManager();
				}
			}
		}
		return INSTANCE;
	}

	public int getNextAltitude() throws MissionExecutionException {
		availableAltitude += DronologyConstants.MISSION_TAKEOFF_ALTITUDE_INCREMENT;
		if (availableAltitude > DronologyConstants.MISSION_MAX_TAKEOFF_DISTANCE) {
			throw new MissionExecutionException("Max altitude reached - No safe altitude available");
		}

		return availableAltitude;
	}

	// Create named synchronization point
	public SynchronizationPoint createSynchronizationPoint(String synchName) {
		if (!synchPoints.containsKey(synchName)) {
			SynchronizationPoint synchPoint = new SynchronizationPoint(synchName);
			synchPoints.put(synchName, synchPoint);
			return synchPoint;
		} else {
			return null;
		}
	}

	public boolean synchPointExists(String synchName) {
		return synchPoints.containsKey(synchName);
	}

	// Register a UAV for a synchPoint
	public boolean addSynchItem(String uavid, String synchName) {
		SynchronizationPoint synchPoint = synchPoints.get(synchName);
		if (synchPoint != null) {
			synchPoint.registerCollaborator(uavid);
			LOGGER.missionInfo("Adding '" + uavid + "' to SYNC-Point" + synchName);
			return true;
		}
		LOGGER.missionError("Sync point '" + synchName + "' not available!");
		return false;
	}

	// Activate all synchronization points (we may not always want to do this -- but
	// it works for our current coordinated takeoff/landing
	public void activateAllSynchPoints() {
		synchPoints.forEach((k, point) -> {
			point.activateSynchronizationPoint();
		});
	}

	// Remove UAV from the synch point it has visited
	public void uavVisitedSynchPoint(String uavid, String synchName) {
		SynchronizationPoint synchPoint = synchPoints.get(synchName);
		if (synchPoint != null) {
			synchPoint.removeCollaborator(uavid);
		} else {
			LOGGER.missionError("Sync point '" + synchName + "' not found");
		}
	}

	// Count number of UAVs who are yet to visit the synch point
	public int getCountOfUnsynchedUAVs(String synchName) {
		SynchronizationPoint synchPoint = synchPoints.get(synchName);
		return synchPoint.countUnsynched();
	}

	// Check if all expected UAVs have visited
	public boolean isFullySynched(String synchName) {
		SynchronizationPoint synchPoint = synchPoints.get(synchName);
		return synchPoint.isSynched();
	}

	// Remove UAV after it has visited
	public void removeUAV(String uavID) {
		for (SynchronizationPoint sp : synchPoints.values()) {
			sp.removeCollaborator(uavID);
		}
	}

	public void resetAltitudes() {
		availableAltitude = DronologyConstants.MISSION_TAKEOFF_MIN_ALTITUDE;
	}

}