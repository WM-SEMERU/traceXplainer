package edu.nd.dronology.services.instances.dronesimulator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.nd.dronology.core.util.Waypoint;
import edu.nd.dronology.services.core.api.IFileChangeNotifyable;
import edu.nd.dronology.services.core.api.ServiceInfo;
import edu.nd.dronology.services.core.base.AbstractFileTransmitServiceInstance;
import edu.nd.dronology.services.core.info.DroneInitializationInfo;
import edu.nd.dronology.services.core.info.DroneInitializationInfo.DroneMode;
import edu.nd.dronology.services.core.info.FlightRouteInfo;
import edu.nd.dronology.services.core.info.SimulatorScenarioCategoryInfo;
import edu.nd.dronology.services.core.info.SimulatorScenarioInfo;
import edu.nd.dronology.services.core.items.AssignedDrone;
import edu.nd.dronology.services.core.items.ISimulatorScenario;
import edu.nd.dronology.services.core.persistence.PersistenceException;
import edu.nd.dronology.services.core.persistence.SimulatorScenarioPersistenceProvider;
import edu.nd.dronology.services.core.util.DronologyConstants;
import edu.nd.dronology.services.core.util.DronologyServiceException;
import edu.nd.dronology.services.core.util.ServiceIds;
import edu.nd.dronology.services.instances.DronologyElementFactory;
import edu.nd.dronology.services.instances.dronesetup.DroneSetupService;
import edu.nd.dronology.services.instances.flightmanager.FlightManagerService;
import edu.nd.dronology.services.instances.flightroute.FlightRouteplanningService;
import edu.nd.dronology.services.supervisor.SupervisorService;
import edu.nd.dronology.util.FileUtil;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

public class DroneSimulatorServiceInstance extends AbstractFileTransmitServiceInstance<SimulatorScenarioInfo>
		implements IFileChangeNotifyable, IDroneSimulatorServiceInstance {

	private static final ILogger LOGGER = LoggerProvider.getLogger(DroneSimulatorServiceInstance.class);

	private static final int ORDER = 5;

	public static final String EXTENSION = DronologyConstants.EXTENSION_SIM_SCENARIO;

	private Collection<SimulatorScenarioCategoryInfo> categories = new ArrayList<>();

	private Map<String, ISimulatorScenario> scenarios = new HashMap<>();

	public DroneSimulatorServiceInstance() {
		super(ServiceIds.SERVICE_SIMULATOR, "DroneSimulator Management", EXTENSION);

		categories.add(new SimulatorScenarioCategoryInfo("Default", "Default"));

	}

	@Override
	protected Class<?> getServiceClass() {
		return FlightRouteplanningService.class;
	}

	@Override
	protected int getOrder() {
		return ORDER;
	}

	@Override
	protected String getPropertyPath() {
		return null;
	}

	@Override
	protected void doStartService() throws Exception {
		reloadItems();
	}

	@Override
	protected void doStopService() throws Exception {
		fileManager.tearDown();
	}

	@Override
	public ServiceInfo getServiceInfo() {
		ServiceInfo sInfo = super.getServiceInfo();
		sInfo.addAttribute(ServiceInfo.ATTRIBUTE_TYPE, ServiceInfo.ATTRIBUTE_FILE);
		return sInfo;
	}

	@Override
	public SimulatorScenarioInfo createItem() throws DronologyServiceException {
		SimulatorScenarioPersistenceProvider persistor = SimulatorScenarioPersistenceProvider.getInstance();
		ISimulatorScenario simulatorScenario = DronologyElementFactory.createNewSimulatorScenario();
		simulatorScenario.setName("New-SimulatorScenario");
		String savePath = FileUtil.concat(storagePath, simulatorScenario.getId(), EXTENSION);

		try {
			persistor.saveItem(simulatorScenario, savePath);
		} catch (PersistenceException e) {
			throw new DronologyServiceException("Error when creating flightpath: " + e.getMessage());
		}
		return new SimulatorScenarioInfo(simulatorScenario.getName(), simulatorScenario.getId());
	}

	@Override
	protected String getPath() {
		String path = SupervisorService.getInstance().getSimScenarioLocation();
		return path;
	}

	@Override
	protected SimulatorScenarioInfo fromFile(String id, File file) throws Throwable {
		ISimulatorScenario atm = SimulatorScenarioPersistenceProvider.getInstance().loadItem(file.toURI().toURL());
		scenarios.put(id, atm);
		SimulatorScenarioInfo info = new SimulatorScenarioInfo(atm.getName(), id);

		return info;
	}

	@Override
	protected boolean hasProperties() {
		return false;
	}

	@Override
	public void notifyFileChange(Set<String> changed) {
		super.notifyFileChange(changed);
		for (String s : changed) {
			String id = s.replace("." + extension, "");
			if (!itemmap.containsKey(id)) {

			}
		}
	}

	@Override
	public void activateScenario(SimulatorScenarioInfo scenario) throws DronologyServiceException {
		ISimulatorScenario item = scenarios.get(scenario.getId());
		if (item == null) {
			throw new DronologyServiceException("Scenario '" + scenario.getId() + "' not found");
		}
		for (AssignedDrone drone : item.getAssignedDrones()) {
			DroneSetupService.getInstance().initializeDrones(new DroneInitializationInfo(drone.getName(),
					DroneMode.MODE_VIRTUAL, drone.getName(), drone.getStartCoordinate()));
		}

		for (String path : item.getAssignedFlightPaths()) {

			FlightRouteInfo info = FlightRouteplanningService.getInstance().getItem(path);

			List<Waypoint> coordds = new ArrayList<>(info.getWaypoints());
			List<Waypoint> waypoints = new ArrayList<>();
			for (Waypoint c : coordds) {
				waypoints.add(c);
			}
			try {
				FlightManagerService.getInstance().planFlight(info.getName(), waypoints);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public Collection<SimulatorScenarioCategoryInfo> getCategories() {
		return Collections.unmodifiableCollection(categories);
	}
}
