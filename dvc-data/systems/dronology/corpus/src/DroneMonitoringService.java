package edu.nd.dronology.monitoring.service;

import java.util.Set;

import edu.nd.dronology.core.monitoring.ArtifactIdentifier;
import edu.nd.dronology.monitoring.IRemoteMonitoringMessageHandler;
import edu.nd.dronology.services.core.base.AbstractServerService;



/**
 * 
 * Service for UAV monitoring messages<br>
 * Allows registering {@link IRemoteMonitoringMessageHandler} to subscribe to certain message types or topics<br>
 * 
 *  
 * @author Michael Vierhauser
 *
 */ 
public class DroneMonitoringService extends AbstractServerService<IDroneMonitoringServiceInstance> {

	private static volatile DroneMonitoringService INSTANCE;

	protected DroneMonitoringService() {
	}

	/**
	 * @return The singleton DroneMonitoringService instance
	 */
	public static DroneMonitoringService getInstance() {
		if (INSTANCE == null) {
			synchronized (DroneMonitoringService.class) {
				if (INSTANCE == null) {
					INSTANCE = new DroneMonitoringService();
				}
			}
		}
		return INSTANCE;
	}

	@Override
	protected IDroneMonitoringServiceInstance initServiceInstance() {
		return new DroneMonitoringServiceInstance();
	}

	public void registerMonitoringMessageHandler(IRemoteMonitoringMessageHandler handler) {
		serviceInstance.registerMonitoringMessageHandler(handler);
	}

	public void registerMonitoringMessageHandler(IRemoteMonitoringMessageHandler handler,
			ArtifactIdentifier identifier) {
		serviceInstance.registerMonitoringMessageHandler(handler, identifier);
	}

	protected void setMonitoringFrequency(String uavid, Double frequency) {

	}

	public Set<IRemoteMonitoringMessageHandler> getSubscribedHandler(ArtifactIdentifier identifier) {
		return serviceInstance.getSubscribedHandler(identifier);

	}

	public void unsubscribeHandler(IRemoteMonitoringMessageHandler handler) {
		serviceInstance.unsubscribeHandler(handler);
	}

}
