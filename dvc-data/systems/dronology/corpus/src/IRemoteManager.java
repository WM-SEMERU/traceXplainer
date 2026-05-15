package edu.nd.dronology.services.core.remote;

import java.rmi.RemoteException;
import java.util.List;

import edu.nd.dronology.services.core.api.IRemotable;
import edu.nd.dronology.services.core.api.ServiceInfo;
import edu.nd.dronology.services.core.util.DronologyServiceException;

/**
 * Interface for handling communication between server UI and server services.
 * 
 * 
 * @author Michael Vierhauser
 * 
 */
public interface IRemoteManager extends IRemotable {

	/**
	 * Retrieve the desired service to offer remote operations.<br>
	 * Requested service has to extends {@link IRemoteableService}.
	 * 
	 * 
	 * @param service
	 *          The Service to retrieve.
	 * @return The requested Service.
	 * @throws RemoteException
	 * @throws DronologyServiceException
	 */
	public Object getService(Class<?> service) throws RemoteException, DronologyServiceException;

	
	/**
	 * @return A list of all registered Services.
	 * @throws RemoteException
	 * @throws DronologyServiceException
	 */
	public List<ServiceInfo> getServices() throws RemoteException, DronologyServiceException;


	/**
	 * Adds a service listener to all currently available services informing on service status changes.
	 * 
	 * @param listener
	 *          The listener to be added.
	 * @throws RemoteException
	 */
	public void addServiceListener(IRemoteServiceListener listener) throws RemoteException;

	/**
	 * 
	 * @param listener
	 *          The listener to be removed.
	 * @throws RemoteException
	 */
	public void removeServiceListener(IRemoteServiceListener listener) throws RemoteException;

	public void register(RemoteInfo rInfo) throws RemoteException;

	public void unregister(RemoteInfo create) throws RemoteException;

	/**
	 * @return
	 * @throws RemoteException
	 * @throws DronologyServiceException
	 */

	List<ServiceInfo> getCoreServices() throws RemoteException, DronologyServiceException;


	public List<ServiceInfo> getFileServices() throws RemoteException, DronologyServiceException;

	List<ServiceInfo> getAllServices() throws RemoteException, DronologyServiceException;

	public void initialize() throws RemoteException, DronologyServiceException;
	public void tearDown()throws RemoteException, DronologyServiceException;


	void contributeService(Class service, IRemoteableService serviceInstance) throws RemoteException, DronologyServiceException;

}
