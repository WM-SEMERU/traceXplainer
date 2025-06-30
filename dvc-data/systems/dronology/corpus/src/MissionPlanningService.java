package edu.nd.dronology.services.extensions.missionplanning.service.internal;

import edu.nd.dronology.services.core.base.AbstractFileTransmitServerService;
import edu.nd.dronology.services.core.info.MissionInfo;
import edu.nd.dronology.services.core.info.UAVMappingInfo;
import edu.nd.dronology.services.core.util.DronologyServiceException;

/**
 * 
 * Services for planning and executing missions for multiple UAVs .<br>
 * Initial implementation of mission planning capabilities. <br>
 * Allows retrieving sending a mission plan as JSON String to Dronology.
 * 
 * 
 * @author Michael Vierhauser
 *
 */
public class MissionPlanningService
		extends AbstractFileTransmitServerService<IMissionPlanningServiceInstance, MissionInfo> {

	private static volatile MissionPlanningService INSTANCE;

	protected MissionPlanningService() {
	}

	/**
	 * @return The singleton ConfigurationService instance
	 */
	public static MissionPlanningService getInstance() {
		if (INSTANCE == null) {
			synchronized (MissionPlanningService.class) {
				if (INSTANCE == null) {
					INSTANCE = new MissionPlanningService();
				}
			}
		}
		return INSTANCE;
	}

	@Override
	protected IMissionPlanningServiceInstance initServiceInstance() {
		return new MissionPlanningServiceInstance();
	}

	public void executeMissionPlan(String mission) throws DronologyServiceException {
		serviceInstance.executeMissionPlan(mission);

	}

	public void cancelMission() throws DronologyServiceException {
		serviceInstance.cancelMission();

	}

	public void removeUAV(String uavid) throws DronologyServiceException {
		serviceInstance.removeUAV(uavid);

	}

	public void executeMissionPlan(MissionInfo mission) throws DronologyServiceException {
		serviceInstance.executeMissionPlan(mission);

	}

	public void executeMissionPlan(MissionInfo mission, UAVMappingInfo mapping) throws DronologyServiceException {
		serviceInstance.executeMissionPlan(mission, mapping);

	}

}
