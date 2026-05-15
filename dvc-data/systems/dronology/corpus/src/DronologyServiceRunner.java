package edu.nd.dronology.services.launch;

import edu.nd.dronology.core.fleet.RuntimeDroneTypes;
import edu.nd.dronology.gstation.connector.service.connector.DroneConnectorService;
import edu.nd.dronology.monitoring.service.DroneMonitoringServiceRemoteFacade;
import edu.nd.dronology.monitoring.service.IDroneMonitoringRemoteService;
import edu.nd.dronology.services.core.remote.IAreaMappingRemoteService;
import edu.nd.dronology.services.core.remote.IMissionPlanningRemoteService;
import edu.nd.dronology.services.extensions.areamapping.facade.AreaMappingServiceRemoteFacade;
import edu.nd.dronology.services.extensions.areamapping.instances.AreaMappingService;
import edu.nd.dronology.services.extensions.missionplanning.service.MissionPlanningServiceRemoteFacade;
import edu.nd.dronology.services.extensions.missionplanning.service.internal.MissionPlanningService;
import edu.nd.dronology.services.instances.dronesetup.DroneSetupService;
import edu.nd.dronology.services.instances.dronesimulator.DroneSimulatorService;
import edu.nd.dronology.services.instances.flightmanager.FlightManagerService;
import edu.nd.dronology.services.instances.flightroute.FlightRouteplanningService;
import edu.nd.dronology.services.instances.registration.types.UAVTypeRegistrationService;
import edu.nd.dronology.services.instances.registration.uavs.UAVRegistrationService;
import edu.nd.dronology.services.remote.RemoteManager;
import edu.nd.dronology.services.remote.RemoteService;
import edu.nd.dronology.services.supervisor.SupervisorService;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

public class DronologyServiceRunner {

	private static final ILogger LOGGER = LoggerProvider.getLogger(DronologyServiceRunner.class);
	private static final boolean USE_SAFETY_CASES = true;

	public static void main(String[] args) {

		try {

			RemoteService.getInstance().startService();
			SupervisorService.getInstance().startService();
			FlightRouteplanningService.getInstance().startService();
			FlightManagerService.getInstance().startService();
			DroneSetupService.getInstance().startService();
			UAVRegistrationService.getInstance().startService();
			DroneSimulatorService.getInstance().startService();
			
			
			UAVRegistrationService.getInstance().startService();
			UAVTypeRegistrationService.getInstance().startService();
			

			DroneConnectorService.getInstance().startService();
			RuntimeDroneTypes runtimeMode = RuntimeDroneTypes.getInstance();

			runtimeMode.setPhysicalEnvironment();

			// Extension services....
			MissionPlanningService.getInstance().startService();
			AreaMappingService.getInstance().startService();

			RemoteManager.getInstance().contributeService(IMissionPlanningRemoteService.class,
					MissionPlanningServiceRemoteFacade.getInstance());

			RemoteManager.getInstance().contributeService(IDroneMonitoringRemoteService.class,
					DroneMonitoringServiceRemoteFacade.getInstance());
			
			RemoteManager.getInstance().contributeService(IAreaMappingRemoteService.class,
					AreaMappingServiceRemoteFacade.getInstance());

			// DronologyMonitoringManager.getInstance().registerHandler(new
			// MonitoringDataHandler3());

			// new SimpleMonitor().main(null);
			// SimpleChecker.getInstance().init();

		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

}
