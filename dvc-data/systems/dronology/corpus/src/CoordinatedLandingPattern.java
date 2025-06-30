package edu.nd.dronology.services.extensions.missionplanning.patterns;

import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.core.monitoring.DronologyMonitoringManager;
import edu.nd.dronology.core.monitoring.MessageMarshaller;
import edu.nd.dronology.core.monitoring.messages.UAVMonitorableMessage.MessageType;
import edu.nd.dronology.services.extensions.missionplanning.MissionExecutionException;
import edu.nd.dronology.services.extensions.missionplanning.plan.UAVMissionPlan;
import edu.nd.dronology.services.extensions.missionplanning.sync.SyncConstants;
import edu.nd.dronology.services.extensions.missionplanning.tasks.PatternTask;
import edu.nd.dronology.services.extensions.missionplanning.tasks.TaskFactory;
import edu.nd.dronology.services.extensions.missionplanning.v1.FullMissionPlan;

/**
 * 
 * Predefined pattern for coordinated landing that is expanded as part of a {@link PatternTask} in a {@link FullMissionPlan}.<br>
 * 
 * @author Michael Vierhauser
 *
 */
public class CoordinatedLandingPattern extends AbstractFlightPattern implements IFlightPattern {
	CoordinatedLandingPattern() {

	}

	@Override
	public void expandFlightPattern(UAVMissionPlan uavMission, LlaCoordinate currentLocation,
			LlaCoordinate targetLocation) throws MissionExecutionException {

		if (uavMission.getCoordinationAltitude() == 0) {
			uavMission.setCoordinationAltitude(synchPointMgr.getNextAltitude());
		}

		LlaCoordinate targetWaypoint1 = new LlaCoordinate(currentLocation.getLatitude(), currentLocation.getLongitude(),
				uavMission.getCoordinationAltitude());
		DronologyMonitoringManager.getInstance()
				.publish(MessageMarshaller.createMessage(MessageType.MISSION_WAYPOINT, uavMission.getUavID(), targetWaypoint1));

		LlaCoordinate targetWaypoint2 = new LlaCoordinate(targetLocation.getLatitude(), targetLocation.getLongitude(),
				uavMission.getCoordinationAltitude());
		DronologyMonitoringManager.getInstance()
				.publish(MessageMarshaller.createMessage(MessageType.MISSION_WAYPOINT, uavMission.getUavID(), targetWaypoint2));

		LlaCoordinate landWaypoint = new LlaCoordinate(targetLocation.getLatitude(), targetLocation.getLongitude(),
				targetLocation.getAltitude());
		DronologyMonitoringManager.getInstance()
				.publish(MessageMarshaller.createMessage(MessageType.MISSION_WAYPOINT, uavMission.getUavID(), landWaypoint));

		addTask(TaskFactory.getTask(TaskFactory.WAYPOINT, uavMission.getUavID(), targetWaypoint1));
		addTask(TaskFactory.getTask(TaskFactory.SYNC, uavMission.getUavID(), SyncConstants.LANDING_ASC_REACHED));

		addTask(TaskFactory.getTask(TaskFactory.WAYPOINT, uavMission.getUavID(), targetWaypoint2));
		addTask(TaskFactory.getTask(TaskFactory.SYNC, uavMission.getUavID(), SyncConstants.LANDING_LONLAT_REACHED));

		addTask(TaskFactory.getTask(TaskFactory.LAND, uavMission.getUavID(), landWaypoint));
		addTask(TaskFactory.getTask(TaskFactory.SYNC, uavMission.getUavID(), SyncConstants.LANDING_HOME_REACHED));

	}

	@Override
	protected void doCreateSyncPoints() {
		addSyncPoint("SP-TakeOff-AscentTargetReached");
		addSyncPoint("SP-TakeOff-LonLatReached");
		addSyncPoint("SP-TakeOff-FirstWayPointReached");

	}

}
