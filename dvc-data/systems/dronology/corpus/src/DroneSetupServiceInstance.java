package edu.nd.dronology.services.instances.dronesetup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.nd.dronology.core.exceptions.DroneException;
import edu.nd.dronology.core.fleet.AbstractDroneFleetFactory;
import edu.nd.dronology.core.fleet.DroneFleetManager;
import edu.nd.dronology.core.fleet.PhysicalDroneFleetFactory;
import edu.nd.dronology.core.fleet.VirtualDroneFleetFactory;
import edu.nd.dronology.core.flight.PlanPoolManager;
import edu.nd.dronology.core.status.DroneCollectionStatus;
import edu.nd.dronology.core.vehicle.IUAVProxy;
import edu.nd.dronology.core.vehicle.ManagedDrone;
import edu.nd.dronology.core.vehicle.proxy.UAVProxyManager;
import edu.nd.dronology.services.core.base.AbstractServiceInstance;
import edu.nd.dronology.services.core.info.DroneInitializationInfo;
import edu.nd.dronology.services.core.info.DroneInitializationInfo.DroneMode;
import edu.nd.dronology.services.core.listener.IDroneStatusChangeListener;
import edu.nd.dronology.services.core.remote.IDroneSetupRemoteService;
import edu.nd.dronology.services.core.util.DronologyServiceException;
import edu.nd.dronology.util.NullUtil;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

/**
 * 
 * Service instance for handling UAVs<br>
 * See {@link IDroneSetupRemoteService} for details.
 *
 */
public class DroneSetupServiceInstance extends AbstractServiceInstance implements IDroneSetupServiceInstance {

	private static final ILogger LOGGER = LoggerProvider.getLogger(DroneSetupServiceInstance.class);

	private AbstractDroneFleetFactory physicalDroneFleetFactory;
	private AbstractDroneFleetFactory virtualDroneFleetFactory;

	private List<IDroneStatusChangeListener> listenerList = new ArrayList<>();

	public DroneSetupServiceInstance() {
		super("DRONESETUP");
	}

	@Override
	protected Class<?> getServiceClass() {
		return DroneSetupService.class;
	}

	@Override
	protected int getOrder() {
		return 3;
	}

	@Override
	protected String getPropertyPath() {
		return null;
	}

	@Override
	protected void doStartService() throws Exception {
		physicalDroneFleetFactory = PhysicalDroneFleetFactory.getInstance();
		virtualDroneFleetFactory = VirtualDroneFleetFactory.getInstance();

	}

	@Override
	protected void doStopService() throws Exception {
	}

	@Override
	public void initializeDrones(DroneInitializationInfo[] info) throws DronologyServiceException {
		NullUtil.checkArrayNull(info);
		for (DroneInitializationInfo di : info) {
			try {
				doInitDrone(di);
			} catch (DroneException e) {
				throw new DronologyServiceException(e.getMessage());
			}
		}
	}
 
	private void doInitDrone(DroneInitializationInfo di) throws DroneException {
		if (di.getMode() == DroneMode.MODE_PHYSICAL) {
			physicalDroneFleetFactory.initializeDrone(di.getId(), di.getType(), di.getInitialLocation());
		} else {
			virtualDroneFleetFactory.initializeDrone(di.getId(), di.getType(), di.getInitialLocation());
		}

		IUAVProxy drStat = UAVProxyManager.getInstance().getActiveUAV(di.getId());
		notifyDroneStatusChange(drStat);
	}

	@Override
	public void addDroneStatusChangeListener(IDroneStatusChangeListener listener) {
		synchronized (listenerList) {
			boolean success = listenerList.add(listener);
			if (!success) {
				// throw exception
			}
		}

	}

	@Override
	public void removeDroneStatusChangeListener(IDroneStatusChangeListener listener) {
		synchronized (listenerList) {
			boolean success = listenerList.remove(listener);
			if (!success) {
				// throw exception
			}
		}

	}

	private void notifyDroneStatusChange(IUAVProxy status) {
		List<IDroneStatusChangeListener> notifyList;
		synchronized (listenerList) {
			notifyList = new ArrayList<>(listenerList);
		}
		for (IDroneStatusChangeListener listener : notifyList) {
			try {
				listener.droneStatusChanged(status);
			} catch (Exception e) {
				LOGGER.error(e);
				listenerList.remove(listener);
			}
		}
	}

	@Override
	public Collection<IUAVProxy> getActiveUAVs() {
		return UAVProxyManager.getInstance().getActiveUAVs();
	}

	@Override
	public void deactivateDrone(IUAVProxy status) throws DronologyServiceException {
		try {
			LOGGER.info("Unregistering UAV '" + status.getID() + "'");
			DroneFleetManager.getInstance().unregisterDroe(status.getID());
			DroneCollectionStatus.getInstance().removeDrone(status);
			cancelPlans(status.getID());
			notifyDroneStatusChange(status);
		} catch (DroneException e) {
			throw new DronologyServiceException(e.getMessage());
		}

	}

	private void cancelPlans(String id) {
		if (PlanPoolManager.getInstance().getCurrentPlan(id) != null) {
			try {
				PlanPoolManager.getInstance().overridePlan(null, id);
			} catch (DroneException e) {
				LOGGER.error(e);
			}
		}
		if (PlanPoolManager.getInstance().getPendingPlans(id).size() > 0) {
			try {
				PlanPoolManager.getInstance().cancelPendingPlans(id);
			} catch (DroneException e) {
				LOGGER.error(e);
			}
		}

	}

	@Override
	public IUAVProxy getActiveUAV(String uavId) throws DronologyServiceException {
		IUAVProxy uavProxy = UAVProxyManager.getInstance().getActiveUAV(uavId);
		if (uavProxy == null) {
			throw new DronologyServiceException("UAV '" + uavId + "' not active");
		}
		return uavProxy;
	}

	@Override
	public void resendCommand(String uavId) throws DronologyServiceException {

		try {
			ManagedDrone drone = DroneFleetManager.getInstance().getRegisteredDrone(uavId);
			drone.resendCommand();

		} catch (DroneException e) {
			LOGGER.info(e.getMessage());
			throw new DronologyServiceException(e.getMessage());
		}

	}

}
