package edu.nd.dronology.services.extensions.missionplanning.service.internal;

import edu.nd.dronology.services.core.api.IFileTransmitServiceInstance;
import edu.nd.dronology.services.core.info.MissionInfo;
import edu.nd.dronology.services.core.info.UAVMappingInfo;
import edu.nd.dronology.services.core.util.DronologyServiceException;

public interface IMissionPlanningServiceInstance extends IFileTransmitServiceInstance<MissionInfo> {

	void executeMissionPlan(String mission) throws DronologyServiceException;

	void cancelMission() throws DronologyServiceException;

	void removeUAV(String uavid) throws DronologyServiceException;

	void executeMissionPlan(MissionInfo info) throws DronologyServiceException;

	void executeMissionPlan(MissionInfo mission, UAVMappingInfo mapping) throws DronologyServiceException;

}
