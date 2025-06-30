package edu.nd.dronology.services.extensions.missionplanning.sync;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * Synchronization Point for routes assigned to UAVs. <br>
 * Once a {@link SynchronizationPoint} is {@link SynchronizationState#ACTIVE} by one UAV, all collaborators (i.e., registered UAV) have to reach the sync point in order to proceed.
 * 
 * @author Jane Cleland-Huang
 *
 */
public class SynchronizationPoint {
	private String synchName;
	private Set<String> collaborators;
	private SynchronizationState synchronizationState;

	public SynchronizationPoint(String name) { 
		synchName = name;
		collaborators = new HashSet<>();
		synchronizationState = SynchronizationState.NOTACTIVATED;
	}

	@Override
	public String toString() {
		return synchName + " Members: " + collaborators.size();
	}

	public void registerCollaborator(String UAV) {
		collaborators.add(UAV);
	}

	public void activateSynchronizationPoint() {
		synchronizationState = SynchronizationState.ACTIVE;
	}

	public void removeCollaborator(String uavid) {
		if (collaborators.contains(uavid)) { //It may already have been removed. Do nothing if its already removed
			collaborators.remove(uavid);
		}

		if (collaborators.isEmpty() && synchronizationState.equals(SynchronizationState.ACTIVE))
			synchronizationState = SynchronizationState.SYNCHED; // Assumes all collaborators are added before it
		// becomes active.
	}

	public int countUnsynched() {
		return collaborators.size();
	}

	public boolean isSynched() {
		return synchronizationState == SynchronizationState.SYNCHED;
	}
}