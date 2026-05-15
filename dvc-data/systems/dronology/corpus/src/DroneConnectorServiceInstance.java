package edu.nd.dronology.gstation.connector.service.connector;

import java.text.DateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.nd.dronology.core.DronologyConstants;
import edu.nd.dronology.core.exceptions.DroneException;
import edu.nd.dronology.core.fleet.RuntimeDroneTypes;
import edu.nd.dronology.core.vehicle.IUAVProxy;
import edu.nd.dronology.gstation.connector.GroundStationException;
import edu.nd.dronology.gstation.connector.GroundstationConnector;
import edu.nd.dronology.gstation.connector.connect.IncommingGroundstationConnectionServer;
import edu.nd.dronology.gstation.connector.messages.ConnectionRequestMessage;
import edu.nd.dronology.services.core.base.AbstractServiceInstance;
import edu.nd.dronology.services.instances.dronesetup.DroneSetupService;
import edu.nd.dronology.util.NamedThreadFactory;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

public class DroneConnectorServiceInstance extends AbstractServiceInstance implements IDroneConnectorServiceInstance {
	ExecutorService connectionExecutor = Executors.newFixedThreadPool(DronologyConstants.MAX_GROUNDSTATIONS,
			new NamedThreadFactory("Connection-Socket-Threads"));

	private static final ILogger LOGGER = LoggerProvider.getLogger(DroneConnectorServiceInstance.class);

	static final transient Gson GSON = new GsonBuilder().enableComplexMapKeySerialization().serializeNulls()
			.setDateFormat(DateFormat.LONG).setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
			.setVersion(1.0).serializeSpecialFloatingPointValues().create();

	private IncommingGroundstationConnectionServer server;
	private Map<String, GroundstationConnector> activeConnections = new HashMap<>();

	public DroneConnectorServiceInstance() {
		super("DRONECONNECTOR");
	}

	@Override
	protected Class<?> getServiceClass() {
		return DroneConnectorService.class;
	}

	@Override
	protected int getOrder() {
		// TODO Auto-generated method stub
		return 3;
	}

	@Override
	protected String getPropertyPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void doStartService() throws Exception {
		server = new IncommingGroundstationConnectionServer();
		servicesExecutor.submit(server);
	}

	@Override
	protected void doStopService() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregisterConnection(String connectionId) throws GroundStationException {
		if (activeConnections.containsKey(connectionId)) {
			LOGGER.info("Removing connection!" + connectionId);
			GroundstationConnector conn = activeConnections.remove(connectionId);
			conn.tearDown(); 
			unregisterDrones(connectionId);
			try {
				RuntimeDroneTypes.getInstance().unregisterCommandHandler(connectionId);
			} catch (DroneException e) {
				throw new GroundStationException("Connection with id " + connectionId + " not found");
			}
		} else {
			throw new GroundStationException("Connection with id " + connectionId + " not found");
		}
	}

	private synchronized void unregisterDrones(String groundstationid) {

		Collection<IUAVProxy> activeDrones = DroneSetupService.getInstance().getActiveUAVs();
		for (IUAVProxy st : activeDrones) {
			if (st.getGroundstationId().equals(groundstationid)) {
				try {
					DroneSetupService.getInstance().deactivateDrone(st);
				} catch (Throwable e) {
					LOGGER.error(e);
				}
			}
		}
	}

	@Override
	public void handleConnection(GroundstationConnector connectionHandler) {
		if (activeConnections.size() >= DronologyConstants.MAX_GROUNDSTATIONS) {
			LOGGER.warn("Connection Limit reached - no new parallel connections can be added!");
			return;
		}

		Future<?> future = connectionExecutor.submit(connectionHandler);
	}

	@Override
	public void registerConnection(GroundstationConnector connector, ConnectionRequestMessage msg)
			throws GroundStationException {
		LOGGER.info("Connection requested by groundstation '" + msg.getGCSId() + "'");
		String groundstationId = msg.getGCSId();
		if (activeConnections.containsKey(groundstationId)) {
			throw new GroundStationException("Groundstation already registered! " + groundstationId);
		}
		activeConnections.put(groundstationId, connector);
	}

}
