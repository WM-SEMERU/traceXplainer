package edu.nd.dronology.monitoring;

import java.rmi.Remote;
import java.rmi.RemoteException;

import edu.nd.dronology.core.monitoring.IMonitorableMessage;
 
public interface IRemoteMonitoringMessageHandler extends Remote {

	void notifyMonitoringMessage(IMonitorableMessage message) throws RemoteException;

}
