package edu.nd.dronology.services.extensions.missionplanning.service;

import java.rmi.RemoteException;
import java.util.Collection;

import edu.nd.dronology.services.core.info.MissionInfo;
import edu.nd.dronology.services.core.info.UAVMappingInfo;
import edu.nd.dronology.services.core.listener.IItemChangeListener;
import edu.nd.dronology.services.core.remote.IMissionPlanningRemoteService;
import edu.nd.dronology.services.core.util.DronologyServiceException;
import edu.nd.dronology.services.extensions.missionplanning.service.internal.MissionPlanningService;
import edu.nd.dronology.services.instances.flightmanager.FlightManagerService;
import edu.nd.dronology.services.remote.AbstractRemoteFacade;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

/**
 * 
 * Remote facade for handling MissionPlanning<br>
 * Initial implementation of mission planning capabilities. <br>
 * Allows retrieving sending a mission plan as JSON String to Dronology.
 * 
 * 
 * @author Michael Vierhauser
 *
 */
public class MissionPlanningServiceRemoteFacade extends AbstractRemoteFacade implements IMissionPlanningRemoteService {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4580658378477037955L;
	private static final ILogger LOGGER = LoggerProvider.getLogger(MissionPlanningServiceRemoteFacade.class);
	private static volatile MissionPlanningServiceRemoteFacade INSTANCE;

	protected MissionPlanningServiceRemoteFacade() throws RemoteException {
		super(FlightManagerService.getInstance());
	}

	public static IMissionPlanningRemoteService getInstance() throws RemoteException {
		if (INSTANCE == null) {
			try {
				synchronized (MissionPlanningServiceRemoteFacade.class) {
					if (INSTANCE == null) {
						INSTANCE = new MissionPlanningServiceRemoteFacade();
					}
				}

			} catch (RemoteException e) {
				LOGGER.error(e);
			}
		}
		return INSTANCE;
	}

	@Override
	public void executeMissionPlan(String mission) throws RemoteException, DronologyServiceException {
		MissionPlanningService.getInstance().executeMissionPlan(mission);

	}

	@Override
	public void executeMissionPlan(MissionInfo info) throws RemoteException, DronologyServiceException {
		MissionPlanningService.getInstance().executeMissionPlan(info);

	}

	@Override
	public void cancelMission() throws RemoteException, DronologyServiceException {
		MissionPlanningService.getInstance().cancelMission();

	}

	@Override
	public byte[] requestFromServer(String id) throws RemoteException, DronologyServiceException {
		return MissionPlanningService.getInstance().requestFromServer(id);
	}

	@Override
	public void transmitToServer(String id, byte[] content) throws RemoteException, DronologyServiceException {
		MissionPlanningService.getInstance().transmitToServer(id, content);
	}

	@Override
	public boolean addItemChangeListener(IItemChangeListener listener) throws RemoteException {
		return MissionPlanningService.getInstance().addItemChangeListener(listener);
	}

	@Override
	public boolean removeItemChangeListener(IItemChangeListener listener) throws RemoteException {
		return MissionPlanningService.getInstance().removeItemChangeListener(listener);

	}

	@Override
	public Collection<MissionInfo> getItems() throws RemoteException {
		return MissionPlanningService.getInstance().getItems();

	}

	@Override
	public MissionInfo createItem() throws RemoteException, DronologyServiceException {
		return MissionPlanningService.getInstance().createItem();

	}

	@Override
	public void deleteItem(String itemid) throws RemoteException, DronologyServiceException {
		MissionPlanningService.getInstance().deleteItem(itemid);

	}

	@Override
	public void executeMissionPlan(MissionInfo mission, UAVMappingInfo mapping)
			throws RemoteException, DronologyServiceException {
		MissionPlanningService.getInstance().executeMissionPlan(mission, mapping);

	}

}