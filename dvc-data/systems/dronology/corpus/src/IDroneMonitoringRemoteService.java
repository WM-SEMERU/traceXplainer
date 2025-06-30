package edu.nd.dronology.monitoring.service;

import java.rmi.RemoteException;

import edu.nd.dronology.core.monitoring.ArtifactIdentifier;
import edu.nd.dronology.monitoring.IRemoteMonitoringMessageHandler;
import edu.nd.dronology.services.core.remote.IRemoteableService;

/**
 *
 * 
 * @author Michael Vierhauser
 * 
 *  
 * 
 */
public interface IDroneMonitoringRemoteService extends IRemoteableService {

	public void registerMonitoringMessageHandler(IRemoteMonitoringMessageHandler handler) throws RemoteException;

	public void registerMonitoringMessageHandler(IRemoteMonitoringMessageHandler handler, ArtifactIdentifier identifier)
			throws RemoteException;

	void setMonitoringFrequency(String uavid, Double frequency) throws RemoteException;

}
