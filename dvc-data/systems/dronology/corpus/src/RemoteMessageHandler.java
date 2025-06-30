package edu.nd.dronology.monitoring.simplechecker.monitor;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import edu.nd.dronology.core.monitoring.IMonitorableMessage;
import edu.nd.dronology.monitoring.IRemoteMonitoringMessageHandler;

public class RemoteMessageHandler extends UnicastRemoteObject implements IRemoteMonitoringMessageHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5811464159361618772L;
	private transient MonitorDialog display;



	public RemoteMessageHandler(MonitorDialog display) throws RemoteException {
		super();
		this.display =display;
	}



	@Override
	public void notifyMonitoringMessage(IMonitorableMessage message) throws RemoteException {

		display.addLine(message.toString());
		
		
	}


}
