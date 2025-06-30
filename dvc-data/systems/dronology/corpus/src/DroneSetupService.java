package edu.nd.dronology.services.instances.dronesetup;

import java.util.Collection;

import edu.nd.dronology.core.vehicle.IUAVProxy;
import edu.nd.dronology.services.core.base.AbstractServerService;
import edu.nd.dronology.services.core.info.DroneInitializationInfo;
import edu.nd.dronology.services.core.listener.IDroneStatusChangeListener;
import edu.nd.dronology.services.core.util.DronologyServiceException;


/**
 * 
 * Service for handling UAVs.<br>
 * Allows initializing new UAVs. <br>
 * Allows retrieving active UAVs which returns a proxy ({@link IUAVProxy}) of the actual physical or virtual uav.
 * 
 * 

 * @author Michael Vierhauser
 * 
 *
 */
public class DroneSetupService extends AbstractServerService<IDroneSetupServiceInstance> {

	private static volatile DroneSetupService INSTANCE;

	protected DroneSetupService() {
	}

	/**
	 * @return The singleton ConfigurationService instance
	 */
	public static DroneSetupService getInstance() {
		if (INSTANCE == null) {
			synchronized (DroneSetupService.class) {
				if (INSTANCE == null) {
					INSTANCE = new DroneSetupService();
				}
			}
		}
		return INSTANCE;
	}

	@Override
	protected IDroneSetupServiceInstance initServiceInstance() {
		return new DroneSetupServiceInstance();
	}
 
	public void initializeDrones(DroneInitializationInfo... info) throws DronologyServiceException {
		serviceInstance.initializeDrones(info);
	}

	public void addDroneStatusChangeListener(IDroneStatusChangeListener listener) {
		serviceInstance.addDroneStatusChangeListener(listener);

	}

	public void removeDroneStatusChangeListener(IDroneStatusChangeListener listener) {
		serviceInstance.removeDroneStatusChangeListener(listener);

	}

	public Collection<IUAVProxy> getActiveUAVs() {
		return serviceInstance.getActiveUAVs();
	}

	public void deactivateDrone(IUAVProxy status) throws DronologyServiceException {
		serviceInstance.deactivateDrone(status);

	}

	public IUAVProxy getActiveUAV(String uavId) throws DronologyServiceException {
		return serviceInstance.getActiveUAV(uavId);
 
	}

	public void resendCommand(String uavid) throws DronologyServiceException {
		serviceInstance.resendCommand(uavid);
	}

}
