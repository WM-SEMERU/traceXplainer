package edu.nd.dronology.gstation.connector.dispatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import edu.nd.dronology.core.DronologyConstants;
import edu.nd.dronology.core.IUAVPropertyUpdateNotifier;
import edu.nd.dronology.core.vehicle.commands.IDroneCommand;
import edu.nd.dronology.core.vehicle.internal.PhysicalDrone;
import edu.nd.dronology.gstation.connector.IUAVSafetyValidator;
import edu.nd.dronology.gstation.connector.messages.AbstractUAVMessage;
import edu.nd.dronology.gstation.connector.messages.UAVHandshakeMessage;
import edu.nd.dronology.gstation.connector.messages.UAVMonitoringMessage;
import edu.nd.dronology.gstation.connector.messages.UAVStateMessage;
import edu.nd.dronology.gstation.connector.service.connector.DroneConnectorService;
import edu.nd.dronology.services.core.info.DroneInitializationInfo;
import edu.nd.dronology.services.core.info.DroneInitializationInfo.DroneMode;
import edu.nd.dronology.services.core.util.DronologyServiceException;
import edu.nd.dronology.services.instances.dronesetup.DroneSetupService;
import edu.nd.dronology.util.NamedThreadFactory;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

/**
 * The {@link DispatchQueueManager} handles both <i>incoming</i> and <i>outgoing</i> queues. </br>
 * Incoming queues contain {@link UAVState} received from the UAV to be dispatched to the {@link PhysicalDrone}.<br>
 * The outgoing queue contains {@link IDroneCommand}s being sent to the UAV.
 * 
 * @author Michael Vierhauser
 *
 */
@SuppressWarnings("rawtypes")
public class DispatchQueueManager {

	private static final ILogger LOGGER = LoggerProvider.getLogger(DispatchQueueManager.class);

	private static final int NUM_THREADS = 20;
	private static final ExecutorService SERVICE_EXECUTOR = Executors.newFixedThreadPool(NUM_THREADS,
			new NamedThreadFactory("Dispatch-Threads"));

	private static final boolean USE_MONITORING = true;

	Map<String, BlockingQueue<AbstractUAVMessage>> queueMap = new ConcurrentHashMap<>();
	List<Future> dispatchThreads = new ArrayList<>();

	private BlockingQueue<IDroneCommand> outgoingCommandQueue = new LinkedBlockingDeque<>(100);
	private BlockingQueue<AbstractUAVMessage> monitoringQueue = new LinkedBlockingDeque<>(100);

	private final String groundstationid;

	public String getGroundstationid() {
		return groundstationid;
	}

	private IUAVSafetyValidator validator;

	public DispatchQueueManager(String groundstationid) {
		this.groundstationid = groundstationid;
		if (USE_MONITORING) {
			createMonitoringDispatchThread(monitoringQueue);
		}
	} 

	public void postDroneStatusUpdate(String id, AbstractUAVMessage<?> status) {
		try {
			synchronized (queueMap) {
				boolean success = false;
				if (queueMap.containsKey(id)) {
					success = queueMap.get(id).offer(status);
				} else {
					LOGGER.hwInfo("No uav with id '" + id + "' registered - discarding message");
					return;
				}
				if (!success) {
					LOGGER.hwFatal("Buffer overflow! '" + id + "'");
				}
				if (status instanceof UAVStateMessage) {
					forwardToValidator((UAVStateMessage) status);
				}
			}

		} catch (Throwable t) {
			LOGGER.error(t);
		}
	}

	private void forwardToValidator(UAVStateMessage status) {
		try {
			if (!DronologyConstants.USE_MONITORING) {
				return;
			}
			boolean success = false;
			success = monitoringQueue.offer(status);
			if (!success) {
				LOGGER.warn("MonitoringQueue is Full!");
			}
		} catch (Throwable e) {
			LOGGER.error(e);
		} 
	}

	private void registerNewDrone(String uavid, UAVHandshakeMessage message) {
		LOGGER.hwInfo("New drone registered with  '" + uavid + "' -> " + message.toString());
		DroneInitializationInfo info = new DroneInitializationInfo(PysicalDroneIdGenerator.generate(uavid, groundstationid),
				DroneMode.MODE_PHYSICAL, uavid, message.getHome());
		try {
			DroneSetupService.getInstance().initializeDrones(info);
		} catch (DronologyServiceException e) {
			LOGGER.error(e);
		}

	}

	public void createDispatchThread(String id, IUAVPropertyUpdateNotifier listener) {
		try {
			BlockingQueue<AbstractUAVMessage> queue;
			synchronized (queueMap) {
				if (queueMap.containsKey(id)) {
					queue = queueMap.get(id);
				} else {
					queue = new LinkedBlockingQueue<>(DronologyConstants.NR_MESSAGES_IN_QUEUE);
					queueMap.put(id, queue);
				}
			}
			StatusDispatchThread thread = new StatusDispatchThread(queue, listener);

			LOGGER.hwInfo("New Dispatch-Thread for UAV '" + id + "' created");
			Future<Object> ftr = SERVICE_EXECUTOR.submit(thread);
			dispatchThreads.add(ftr);
		} catch (Throwable t) {
			LOGGER.error(t);
		}
	}

	private void createMonitoringDispatchThread(BlockingQueue<AbstractUAVMessage> queue) {
		MonitoringDispatchThread thread = new MonitoringDispatchThread(queue);

		LOGGER.hwInfo("New Monitoring Dispatch-Thread created");
		Future ftr = SERVICE_EXECUTOR.submit(thread);
		dispatchThreads.add(ftr);
	}

	public void tearDown() { 

		LOGGER.hwInfo("Ground Control Station '" + groundstationid + "' terminated");
		try {
			DroneConnectorService.getInstance().unregisterConnection(groundstationid);
		} catch (Exception e) {
			LOGGER.error("No groundstation connection with id '" + groundstationid + "' registered");
		}
		for (Future<?> ft : dispatchThreads) {
			ft.cancel(true);
		}
		// SERVICE_EXECUTOR.shutdown();
	}

	public BlockingQueue<IDroneCommand> getOutgoingCommandQueue() {
		return outgoingCommandQueue;
	}

	public void send(IDroneCommand cmd) {
		boolean taken = outgoingCommandQueue.offer(cmd);
		LOGGER.trace("Command added to queue!");
		if (!taken) {
			LOGGER.hwFatal("Outgoing Command queue limit reached - command dropped!");
		}

	}

	public void postMonitoringMessage(UAVMonitoringMessage message) {
		if (!DronologyConstants.USE_MONITORING) {
			return;
		}
		String uavid = message.getUavid();
		synchronized (queueMap) {
			if (!queueMap.containsKey(uavid)) {
				LOGGER.hwInfo("No uav with id '" + uavid + "' registered - discarding message");
				return;
			}
		}

		// LOGGER.info("Message " + message.getClass().getSimpleName() + " received :: " + groundstationid);
		boolean success = false;
		success = monitoringQueue.offer(message);
		if (!success) {
			LOGGER.warn("MonitoringQueue is Full!");
		}
	}

	public void postDoneHandshakeMessage(String uavid, UAVHandshakeMessage message) {
 
		if (DronologyConstants.USE_SAFETY_CHECKS) {
			if (validator != null) {
				if (message.getSafetyCase() == null) {
					LOGGER.error("No safety information provided");
				} else {
					boolean success = validator.validate(uavid, message.getSafetyCase());
					if (success) {
						registerNewDrone(uavid, message);
					} else {
						LOGGER.error("Safety checks failed - uav '" + uavid + "' not registered!");
					}
				}
			} else {
				LOGGER.error("No validator provided");
			}
		} else {
			registerNewDrone(uavid, message);
		}

	}

	public void registerSafetyValidator(IUAVSafetyValidator validator) {
		this.validator = validator;

	}

}
