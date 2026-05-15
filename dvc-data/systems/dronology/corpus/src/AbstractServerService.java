package edu.nd.dronology.services.core.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.nd.dronology.services.core.api.IServiceInstance;
import edu.nd.dronology.services.core.api.ServiceInfo;
import edu.nd.dronology.services.core.listener.IServiceListener;
import edu.nd.dronology.services.core.util.DronologyServiceException;
import edu.nd.dronology.util.NullUtil;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

public abstract class AbstractServerService<S extends IServiceInstance> {

	private static final ILogger LOGGER = LoggerProvider.getLogger(AbstractServerService.class);

	protected S serviceInstance;
	protected AtomicBoolean isStarted = new AtomicBoolean(false);
	private static List<AbstractServerService> services = new ArrayList<>();
	private static String customWorkspace;



	public AbstractServerService() {
		services.add(this);
		serviceInstance = initServiceInstance();
	}

	public S getServer() {
		return serviceInstance;
	}

	public void startService() throws DronologyServiceException {
		if (isStarted.compareAndSet(false, true)) {
			// LOGGER.info("Service '" + this.getClass().getSimpleName() + "' started");
			serviceInstance.startService();
			// ServiceOrchestrator.registerService(this);
		}
	}

	public void stopService() throws DronologyServiceException {
		if (isStarted.compareAndSet(true, false)) {
			// LOGGER.info("Service '" + this.getClass().getSimpleName() + "' stopped");
			serviceInstance.stopService();
			// ServiceOrchestrator.unregisterService(this);
		}
	}

	public void restartService() throws DronologyServiceException {
		LOGGER.info("Restarting " + getClass().getSimpleName());
		if (!isStarted.get()) {
			startService();
		} else {
			stopService();
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				LOGGER.error(e);
			}
			startService();
		}
	}

	public boolean addServiceListener(IServiceListener processListener) {
		return serviceInstance.addServiceListener(processListener);

	}

	public boolean removeServiceListener(IServiceListener processListener) {
		return serviceInstance.remoteServiceListener(processListener);
	}

	public ServiceInfo getServerInfo() {
		return serviceInstance.getServiceInfo();
	}

	public static void addUniversialServiceListener(IServiceListener processListener) {
		synchronized (services) {
			for (AbstractServerService service : services) {
				service.serviceInstance.addServiceListener(processListener);
			}
		}
	}

	public static void setWorkspace(String customWorkspace) {
		NullUtil.checkNull(customWorkspace);
		AbstractServerService.customWorkspace = customWorkspace;
	}
	

	public static String getWorkspace() {
		return customWorkspace;
	}

	public static void removeUniversialServiceListener(IServiceListener processListener) {
		synchronized (services) {
			for (AbstractServerService service : services) {
				service.serviceInstance.addServiceListener(processListener);
			}
		}
	}

	protected abstract S initServiceInstance();

	public static List<ServiceInfo> getServiceInfos() throws DronologyServiceException {
		List<ServiceInfo> sInfos = new ArrayList<>();
		synchronized (services) {
			for (AbstractServerService service : services) {
				ServiceInfo sInfo = service.getServerInfo();
				if (sInfo == null) {
					throw new DronologyServiceException("Error retrieving service information from '"
							+ service.getClass().getSimpleName() + "'");
				}
				sInfos.add(sInfo);
			}
		}
		return Collections.unmodifiableList(sInfos);
	}

	public static List<ServiceInfo> getFileServiceInfos() throws DronologyServiceException {
		List<ServiceInfo> sInfos = new ArrayList<>();
		synchronized (services) {
			for (AbstractServerService service : services) {
				ServiceInfo sInfo = service.getServerInfo();
				if (sInfo == null) {
					throw new DronologyServiceException("Error retrieving service information from '"
							+ service.getClass().getSimpleName() + "'");
				}
				if (service instanceof AbstractFileTransmitServerService<?, ?>) {
					sInfos.add(sInfo);
				}
			}
		}
		return Collections.unmodifiableList(sInfos);
	}

	public static void stopAll() {
		for (AbstractServerService service : services) {
			try {
				service.stopService();
			} catch (DronologyServiceException e) {
				LOGGER.error(e);
			}
		}
	}

	public static void restartAll() {
		for (AbstractServerService service : services) {
			try {
				service.restartService();
			} catch (DronologyServiceException e) {
				LOGGER.error(e);
			}
		}
	}

	protected AbstractServerService getService(String serviceClass) throws DronologyServiceException {
		for (AbstractServerService service : services) {
			if (service.getClass().getCanonicalName().equals(serviceClass)) {
				return service;
			}
		}
		throw new DronologyServiceException("Service '" + serviceClass + "' not found!");
	}

	public static List<ServiceInfo> getCoreServices() throws DronologyServiceException {
		List<ServiceInfo> sInfos = new ArrayList<>();
		synchronized (services) {
			for (AbstractServerService service : services) {
				ServiceInfo sInfo = service.getServerInfo();
				if (sInfo == null) {
					throw new DronologyServiceException("Error retrieving service information from '"
							+ service.getClass().getSimpleName() + "'");
				}
				if (!(service instanceof AbstractFileTransmitServerService<?, ?>)) {
					sInfos.add(sInfo);
				}
			}
		}
		return Collections.unmodifiableList(sInfos);
	}



}
