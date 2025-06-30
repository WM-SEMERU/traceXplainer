package edu.nd.dronology.core.monitoring;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.nd.dronology.util.NamedThreadFactory;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

public class DronologyMonitoringManager {

	private static DronologyMonitoringManager INSTANCE = new DronologyMonitoringManager();

	private static final ILogger LOGGER = LoggerProvider.getLogger(DronologyMonitoringManager.class);

	private static final BlockingQueue<IMonitorableMessage> queue = new ArrayBlockingQueue<>(500);

	private static final ExecutorService SERVICE_EXECUTOR = Executors
			.newSingleThreadExecutor(new NamedThreadFactory("Monitoring-Manager"));

	private boolean handlerRegistered;

	/**
	 * 
	 * @return The singleton instance.
	 */
	public static DronologyMonitoringManager getInstance() {

		if (INSTANCE == null) {
			synchronized (DronologyMonitoringManager.class) {
				if (INSTANCE == null) {
					INSTANCE = new DronologyMonitoringManager();
				}
			}
		}
		return INSTANCE;
	}

	public void publish(IMonitorableMessage message) {
		try {
			if (!handlerRegistered) {
				return;
			}

			boolean taken = queue.offer(message);
			if (!taken) {
				LOGGER.error("Monitoring queue full!");
			}
		} catch (Throwable t) {
			LOGGER.error(t); 
		}

	}

	public void registerHandler(IMonitoringDataHandler handler) {
		handler.setQueue(queue);
		handlerRegistered = true;
		SERVICE_EXECUTOR.submit(handler);
	}

}
