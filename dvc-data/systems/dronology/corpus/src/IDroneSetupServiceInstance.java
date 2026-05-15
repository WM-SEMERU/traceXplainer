package edu.nd.dronology.services.instances.dronesetup;

import java.util.Collection;

import edu.nd.dronology.core.vehicle.IUAVProxy;
import edu.nd.dronology.services.core.api.IServiceInstance;
import edu.nd.dronology.services.core.info.DroneInitializationInfo;
import edu.nd.dronology.services.core.listener.IDroneStatusChangeListener;
import edu.nd.dronology.services.core.util.DronologyServiceException;

public interface IDroneSetupServiceInstance extends IServiceInstance {


	void initializeDrones(DroneInitializationInfo[] info) throws DronologyServiceException;

	void addDroneStatusChangeListener(IDroneStatusChangeListener listener);

	void removeDroneStatusChangeListener(IDroneStatusChangeListener listener);

	Collection<IUAVProxy> getActiveUAVs();

	void deactivateDrone(IUAVProxy status) throws DronologyServiceException;

	IUAVProxy getActiveUAV(String uavId) throws DronologyServiceException;

	void resendCommand(String uavId) throws DronologyServiceException;

}
