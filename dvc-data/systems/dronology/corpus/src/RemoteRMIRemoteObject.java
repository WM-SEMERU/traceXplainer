package edu.nd.dronology.services.remote.rmi;

import java.rmi.RemoteException;
import java.util.List;

import edu.nd.dronology.services.core.api.ServiceInfo;
import edu.nd.dronology.services.core.remote.IRemoteManager;
import edu.nd.dronology.services.core.remote.IRemoteServiceListener;
import edu.nd.dronology.services.core.remote.IRemoteableService;
import edu.nd.dronology.services.core.remote.RemoteInfo;
import edu.nd.dronology.services.core.util.DronologyServiceException;
import edu.nd.dronology.services.remote.RemoteManager;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

public class RemoteRMIRemoteObject extends AbstractRMIRemoteObject implements IRemoteManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3719197265334713068L;
	private static final ILogger LOGGER = LoggerProvider.getLogger(RemoteRMIRemoteObject.class);
	private static int DEFAULT_PORT = 9779;
	private static String REG_BINDING = "Registration";
	private static IRemoteManager instance;

	protected RemoteRMIRemoteObject(int port) throws RemoteException {
		super(port, "Remote");
	}

	protected RemoteRMIRemoteObject() throws RemoteException {
		this(DEFAULT_PORT);
	}

	@Override
	public void initialize() throws RemoteException {
		super.initialize();
		// registry.rebind(REG_BINDING,
		// RegistryServiceRemoteFacade.getInstance());
		// LOGGER.info(">> Binding '" + REG_BINDING + "' established on port " +
		// port);
	}

	@Override
	public Object getService(Class service) throws RemoteException, DronologyServiceException {
		return RemoteManager.getInstance().getService(service);

	}

	@Override
	public void addServiceListener(IRemoteServiceListener processListener) throws RemoteException {
		RemoteManager.getInstance().addServiceListener(processListener);

	}

	@Override
	public void removeServiceListener(IRemoteServiceListener processListener) throws RemoteException {
		RemoteManager.getInstance().removeServiceListener(processListener);
	}

	@Override
	public List<ServiceInfo> getServices() throws RemoteException, DronologyServiceException {
		return RemoteManager.getInstance().getServices();
	}

	@Override
	public List<ServiceInfo> getCoreServices() throws RemoteException, DronologyServiceException {
		return RemoteManager.getInstance().getCoreServices();
	}

	@Override
	public List<ServiceInfo> getAllServices() throws RemoteException, DronologyServiceException {
		return RemoteManager.getInstance().getAllServices();
	}

	@Override
	public List<ServiceInfo> getFileServices() throws RemoteException, DronologyServiceException {
		return RemoteManager.getInstance().getFileServices();
	}

	@Override
	public void register(RemoteInfo rInfo) throws RemoteException {
		RemoteManager.getInstance().register(rInfo);

	}

	@Override
	public void unregister(RemoteInfo rInfo) throws RemoteException {
		RemoteManager.getInstance().unregister(rInfo);
	}

	@Override
	public void tearDown() throws RemoteException, DronologyServiceException {
		try {
			super.tearDown();
			registry.unbind(REG_BINDING);
			LOGGER.info(">> Binding '" + REG_BINDING + "' removed on port " + port);
		} catch (Exception e) {
			throw new DronologyServiceException(e.getMessage());
		}
	}

	public static IRemoteManager getInstance(Integer port) {
		if (instance == null) {
			synchronized (RemoteRMIRemoteObject.class) {
				try {
					if (port == null) {
						instance = new RemoteRMIRemoteObject();
					} else {
						instance = new RemoteRMIRemoteObject(port);
					}
				} catch (RemoteException e) {
					LOGGER.error(e);
				}
			}
		}
		return instance;
	}

	@Override
	public void contributeService(Class service, IRemoteableService serviceInstance)
			throws RemoteException, DronologyServiceException {
		RemoteManager.getInstance().contributeService(service,serviceInstance);
		
	}

}
