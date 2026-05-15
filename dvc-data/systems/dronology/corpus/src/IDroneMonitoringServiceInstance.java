package edu.nd.dronology.monitoring.service;

import java.util.Set;

import edu.nd.dronology.core.monitoring.ArtifactIdentifier;
import edu.nd.dronology.monitoring.IRemoteMonitoringMessageHandler;
import edu.nd.dronology.services.core.api.IServiceInstance;
 
public interface IDroneMonitoringServiceInstance extends IServiceInstance {

	void registerMonitoringMessageHandler(IRemoteMonitoringMessageHandler handler);

	Set<IRemoteMonitoringMessageHandler> getSubscribedHandler(ArtifactIdentifier identifier);

	void registerMonitoringMessageHandler(IRemoteMonitoringMessageHandler handler,
			ArtifactIdentifier<IRemoteMonitoringMessageHandler> identifier);

	void unsubscribeHandler(IRemoteMonitoringMessageHandler handler);

}
