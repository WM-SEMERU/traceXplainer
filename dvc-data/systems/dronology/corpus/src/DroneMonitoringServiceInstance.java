package edu.nd.dronology.monitoring.service;

import java.util.HashSet;
import java.util.Set;

import edu.nd.dronology.core.monitoring.ArtifactIdentifier;
import edu.nd.dronology.monitoring.IRemoteMonitoringMessageHandler;
import edu.nd.dronology.monitoring.tree.ArtifactIdentifierTree;
import edu.nd.dronology.services.core.base.AbstractServiceInstance;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

@SuppressWarnings("rawtypes")
public class DroneMonitoringServiceInstance extends AbstractServiceInstance implements IDroneMonitoringServiceInstance {

	private static final ILogger LOGGER = LoggerProvider.getLogger(DroneMonitoringServiceInstance.class);
	private final ArtifactIdentifier ROOT_NODE = new ArtifactIdentifier(ArtifactIdentifier.ROOT);
	private final ArtifactIdentifierTree handlerTree = new ArtifactIdentifierTree(ROOT_NODE);

	public DroneMonitoringServiceInstance() {
		super("DRONEMONITORING");
	}

	@Override
	protected Class<?> getServiceClass() {
		return DroneMonitoringService.class;
	}

	@Override
	protected int getOrder() {
		// TODO Auto-generated method stub
		return 14;
	}

	@Override
	protected String getPropertyPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void doStartService() throws Exception {

	}

	@Override
	protected void doStopService() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerMonitoringMessageHandler(IRemoteMonitoringMessageHandler handler) {
		registerMonitoringMessageHandler(handler, new ArtifactIdentifier(ArtifactIdentifier.ROOT));

	}

	@Override
	public void registerMonitoringMessageHandler(IRemoteMonitoringMessageHandler handler,
			ArtifactIdentifier<IRemoteMonitoringMessageHandler> identifier) {
		LOGGER.info("Attaching handler: " + identifier.toString());
		identifier.attachItem(handler);
		handlerTree.add(identifier);

	}
	@Override
	public Set<IRemoteMonitoringMessageHandler> getSubscribedHandler(ArtifactIdentifier identifier) {
		Set<ArtifactIdentifier> related = handlerTree.getParents(identifier);
		Set<IRemoteMonitoringMessageHandler> selectedHandler = new HashSet<>();

		for (ArtifactIdentifier ident : related) {
			selectedHandler.addAll(ident.getAttachedItems());
		}

		return selectedHandler;
	}

	@Override
	public void unsubscribeHandler(IRemoteMonitoringMessageHandler handler) {
		Set<ArtifactIdentifier> related = handlerTree.getAllRelatedIdentifier(ROOT_NODE);
		ROOT_NODE.removeAttachedItem(handler);
		for (ArtifactIdentifier id : related) {
			id.removeAttachedItem(handler);
		}

	}

}
