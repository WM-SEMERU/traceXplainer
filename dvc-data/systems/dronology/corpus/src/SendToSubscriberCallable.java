package edu.nd.dronology.monitoring;

import java.util.concurrent.Callable;

import edu.nd.dronology.core.monitoring.IMonitorableMessage;
import edu.nd.dronology.monitoring.service.DroneMonitoringService;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

public class SendToSubscriberCallable implements Callable {
	private static final ILogger LOGGER = LoggerProvider.getLogger(SendToSubscriberCallable.class);

	private IRemoteMonitoringMessageHandler handler;
	private IMonitorableMessage message;

	public SendToSubscriberCallable(IRemoteMonitoringMessageHandler handler, IMonitorableMessage message) {
		this.handler = handler;
		this.message = message;
	}

	@Override
	public Object call() throws Exception {
		try {
			handler.notifyMonitoringMessage(message);
		} catch (java.rmi.RemoteException e) {
			LOGGER.error(e.getMessage() + " Unregistering Handler");
			DroneMonitoringService.getInstance().unsubscribeHandler(handler);

		} catch (Throwable t) {
			t.printStackTrace();
		}
		return null;
	}
}
