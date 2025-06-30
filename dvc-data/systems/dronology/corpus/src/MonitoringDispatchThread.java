package edu.nd.dronology.gstation.connector.dispatch;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import edu.nd.dronology.core.IUAVPropertyUpdateNotifier;
import edu.nd.dronology.core.monitoring.DronologyMonitoringManager;
import edu.nd.dronology.gstation.connector.messages.AbstractUAVMessage;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

public class MonitoringDispatchThread extends AbstractStatusDispatchThread<AbstractUAVMessage> implements Callable {
	private static final ILogger LOGGER = LoggerProvider.getLogger(MonitoringDispatchThread.class);

	private IUAVPropertyUpdateNotifier listener;

	public MonitoringDispatchThread(final BlockingQueue<AbstractUAVMessage> queue) {
		super(queue);
	}

	@Override
	public Object call() {
		while (cont.get() && !Thread.currentThread().isInterrupted()) {

			try {
				AbstractUAVMessage message = queue.take();
				DronologyMonitoringManager.getInstance().publish(message);
			} catch (InterruptedException e) {
				LOGGER.info("Monitoring Dispatcher shutdown! -- " + e.getMessage());

			} catch (Throwable e) {
				LOGGER.error(e);
			}

		}
		LOGGER.info("Monitoring Dispatcher shutdown!");
		return null;
	}

}
