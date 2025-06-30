package edu.nd.dronology.services.extensions.missionplanning.patterns;

import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.services.extensions.missionplanning.MissionExecutionException;
import edu.nd.dronology.services.extensions.missionplanning.plan.UAVMissionPlan;
import edu.nd.dronology.services.extensions.missionplanning.sync.SyncConstants;
import edu.nd.dronology.services.extensions.missionplanning.tasks.PatternTask;
import edu.nd.dronology.services.extensions.missionplanning.tasks.TaskFactory;
import edu.nd.dronology.services.extensions.missionplanning.v1.FullMissionPlan;
import edu.nd.dronology.util.NullUtil;

/**
 * 
 * Predefined pattern for coordinated take-off that is expanded as part of a
 * {@link PatternTask} in a {@link FullMissionPlan}.<br>
 * 
 * @author Michael Vierhauser
 *
 */
public class CoordinatedTakeoffPattern extends AbstractFlightPattern implements IFlightPattern {

	CoordinatedTakeoffPattern() {

	}

	@Override
	public void expandFlightPattern(UAVMissionPlan uavMission, LlaCoordinate currentLocation,
			LlaCoordinate targetLocation) throws MissionExecutionException {
		NullUtil.checkNull(uavMission, currentLocation, targetLocation);

		if (uavMission.getCoordinationAltitude() == 0) {
			uavMission.setCoordinationAltitude(synchPointMgr.getNextAltitude());
		}

		LlaCoordinate wp1 = new LlaCoordinate(currentLocation.getLatitude(), currentLocation.getLongitude(),
				uavMission.getCoordinationAltitude());

		LlaCoordinate wp2 = new LlaCoordinate(targetLocation.getLatitude(), targetLocation.getLongitude(),
				uavMission.getCoordinationAltitude());

		LlaCoordinate wp3 = new LlaCoordinate(targetLocation.getLatitude(), targetLocation.getLongitude(),
				targetLocation.getAltitude());

		addTask(TaskFactory.getTask(TaskFactory.TAKEOFF, uavMission.getUavID(), wp1));
		addTask(TaskFactory.getTask(TaskFactory.SYNC, uavMission.getUavID(), SyncConstants.TAKEOFF_ASC_REACHED));

		addTask(TaskFactory.getTask(TaskFactory.WAYPOINT, uavMission.getUavID(), wp2));
		addTask(TaskFactory.getTask(TaskFactory.SYNC, uavMission.getUavID(), SyncConstants.TAKEOFF_LATLON_REACHED));

		addTask(TaskFactory.getTask(TaskFactory.WAYPOINT, uavMission.getUavID(), wp3));
		addTask(TaskFactory.getTask(TaskFactory.SYNC, uavMission.getUavID(), SyncConstants.TAKEOFF_WP_REACHED));

	}

	@Override
	protected void doCreateSyncPoints() {
		addSyncPoint("SP-TakeOff-AscentTargetReached");
		addSyncPoint("SP-TakeOff-LonLatReached");
		addSyncPoint("SP-TakeOff-FirstWayPointReached");

	}

}
