package edu.nd.dronology.services.core.remote;

import java.rmi.RemoteException;
import java.util.Collection;

import edu.nd.dronology.core.vehicle.IUAVProxy;
import edu.nd.dronology.services.core.info.DroneInitializationInfo;
import edu.nd.dronology.services.core.listener.IDroneStatusChangeListener;
import edu.nd.dronology.services.core.util.DronologyServiceException;

/**
 * 
 * Remote Interface for handling UAVs.<br>
 * Allows initializing new UAVs. <br>
 * Allows retrieving active UAVs which returns a proxy ({@link IUAVProxy}) of the actual physical or virtual uav.
 * 
 * 
 * @author Michael Vierhauser
 *
 */
public interface IDroneSetupRemoteService extends IRemoteableService {

	Collection<IUAVProxy> getActiveUAVs() throws RemoteException;

	void initializeDrones(DroneInitializationInfo... info) throws RemoteException, DronologyServiceException;

	void addDroneStatusChangeListener(IDroneStatusChangeListener listener)
			throws RemoteException, DronologyServiceException;

	void removeDroneStatusChangeListener(IDroneStatusChangeListener listener)
			throws RemoteException, DronologyServiceException;

	void resendCommand(String uavid) throws RemoteException, DronologyServiceException;

}
