package edu.nd.dronology.services.remote;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.nd.dronology.services.core.api.ServiceInfo;
import edu.nd.dronology.services.core.base.AbstractServerService;
import edu.nd.dronology.services.core.remote.IDroneSetupRemoteService;
import edu.nd.dronology.services.core.remote.IDroneSimulatorRemoteService;
import edu.nd.dronology.services.core.remote.IFlightManagerRemoteService;
import edu.nd.dronology.services.core.remote.IFlightRouteplanningRemoteService;
import edu.nd.dronology.services.core.remote.IRemoteManager;
import edu.nd.dronology.services.core.remote.IRemoteServiceListener;
import edu.nd.dronology.services.core.remote.IRemoteableService;
import edu.nd.dronology.services.core.remote.IUAVEquipmentTypeRegistrationRemoteService;
import edu.nd.dronology.services.core.remote.IUAVRegistrationRemoteService;
import edu.nd.dronology.services.core.remote.IUAVTypeRegistrationRemoteService;
import edu.nd.dronology.services.core.remote.RemoteInfo;
import edu.nd.dronology.services.core.util.DronologyServiceException;
import edu.nd.dronology.services.facades.DroneSetupServiceRemoteFacade;
import edu.nd.dronology.services.facades.DroneSimulatorServiceRemoteFacade;
import edu.nd.dronology.services.facades.FlightManagerServiceRemoteFacade;
import edu.nd.dronology.services.facades.FlightRouteplanningServiceRemoteFacade;
import edu.nd.dronology.services.facades.UAVEquipmentTypeRegistrationServiceRemoteFacade;
import edu.nd.dronology.services.facades.UAVRegistrationServiceRemoteFacade;
import edu.nd.dronology.services.facades.UAVTypeRegistrationServiceRemoteFacade;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

public class RemoteManager implements IRemoteManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2718289978864805774L;

	private static final ILogger LOGGER = LoggerProvider.getLogger(RemoteManager.class);

	private static RemoteManager instance;

	private Map<Class, IRemoteableService> externalServices = new HashMap<>();

	/**
	 * @return The singleton instance of the RemoteManager.
	 */
	public static IRemoteManager getInstance() {
		if (instance == null) {
			instance = new RemoteManager();
		}
		return instance;
	}

	@Override
	public Object getService(Class<?> service) throws RemoteException, DronologyServiceException {
		if (!IRemoteableService.class.isAssignableFrom(service)) {
			throw new DronologyServiceException(
					"Invalid service requested - Valid services extend " + IRemoteableService.class.getCanonicalName());
		}

		if (service.equals(IFlightRouteplanningRemoteService.class)) {
			return FlightRouteplanningServiceRemoteFacade.getInstance();
		}

		if (service.equals(IFlightManagerRemoteService.class)) {
			return FlightManagerServiceRemoteFacade.getInstance();
		}

		if (service.equals(IDroneSetupRemoteService.class)) {
			return DroneSetupServiceRemoteFacade.getInstance();
		}

		if (service.equals(IUAVRegistrationRemoteService.class)) {
			return UAVRegistrationServiceRemoteFacade.getInstance();
		}

		if (service.equals(IUAVTypeRegistrationRemoteService.class)) {
			return UAVTypeRegistrationServiceRemoteFacade.getInstance();
		}

		if (service.equals(IDroneSimulatorRemoteService.class)) {
			return DroneSimulatorServiceRemoteFacade.getInstance();
		}
		if (service.equals(IUAVEquipmentTypeRegistrationRemoteService.class)) {
			return UAVEquipmentTypeRegistrationServiceRemoteFacade.getInstance();
		}

		if (externalServices.containsKey(service)) {
			return externalServices.get(service);
		}

		throw new DronologyServiceException("Service" + service.getCanonicalName() + " not found!");

	}

	@Override
	public void addServiceListener(IRemoteServiceListener processListener) throws RemoteException {
		AbstractServerService.addUniversialServiceListener(new RemoteServerProcessListenerAdapter(processListener));

	}

	@Override
	public void removeServiceListener(IRemoteServiceListener processListener) throws RemoteException {
		AbstractServerService.removeUniversialServiceListener(new RemoteServerProcessListenerAdapter(processListener));

	}

	@Override
	public List<ServiceInfo> getServices() throws RemoteException, DronologyServiceException {

		List<ServiceInfo> allServices = new ArrayList<>();

		allServices.addAll(getCoreServices());
		// allServices.addAll(getExtensionServices());

		return Collections.unmodifiableList(allServices);

	}

	@Override
	public List<ServiceInfo> getCoreServices() throws RemoteException, DronologyServiceException {
		return AbstractServerService.getCoreServices();
	}

	@Override
	public List<ServiceInfo> getAllServices() throws RemoteException, DronologyServiceException {
		return AbstractServerService.getServiceInfos();
	}

	@Override
	public List<ServiceInfo> getFileServices() throws RemoteException, DronologyServiceException {
		return AbstractServerService.getFileServiceInfos();
	}

	@Override
	public void contributeService(Class service, IRemoteableService serviceInstance)
			throws RemoteException, DronologyServiceException {
		externalServices.put(service, serviceInstance);
	}

	@Override
	public void register(RemoteInfo rInfo) throws RemoteException {
		RemoteService.getInstance().register(rInfo);

	}

	@Override
	public void unregister(RemoteInfo rInfo) throws RemoteException {
		RemoteService.getInstance().unregister(rInfo);
	}

	@Override
	public void initialize() throws RemoteException, DronologyServiceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void tearDown() throws RemoteException, DronologyServiceException {
		// TODO Auto-generated method stub

	}

}
