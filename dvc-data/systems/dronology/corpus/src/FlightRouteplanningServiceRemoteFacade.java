package edu.nd.dronology.services.facades;

import java.rmi.RemoteException;
import java.util.Collection;

import edu.nd.dronology.services.core.info.FlightRouteCategoryInfo;
import edu.nd.dronology.services.core.info.FlightRouteInfo;
import edu.nd.dronology.services.core.listener.IItemChangeListener;
import edu.nd.dronology.services.core.remote.IFlightRouteplanningRemoteService;
import edu.nd.dronology.services.core.util.DronologyServiceException;
import edu.nd.dronology.services.instances.flightroute.FlightRouteplanningService;
import edu.nd.dronology.services.remote.AbstractRemoteFacade;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

/**
 * 
 * Remote facade for handling UAV routes<br>
 * Allows creating and modifying routes.
 * 
 * 
 * @author Michael Vierhauser
 * 
 */
public class FlightRouteplanningServiceRemoteFacade extends AbstractRemoteFacade
		implements IFlightRouteplanningRemoteService {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4580658378477037955L;
	private static final ILogger LOGGER = LoggerProvider.getLogger(FlightRouteplanningServiceRemoteFacade.class);
	private static volatile FlightRouteplanningServiceRemoteFacade INSTANCE;

	protected FlightRouteplanningServiceRemoteFacade() throws RemoteException {
		super(FlightRouteplanningService.getInstance());
	}

	public static IFlightRouteplanningRemoteService getInstance() throws RemoteException {
		if (INSTANCE == null) {
			try {
				synchronized (FlightRouteplanningServiceRemoteFacade.class) {
					if (INSTANCE == null) {
						INSTANCE = new FlightRouteplanningServiceRemoteFacade();
					}
				}
			} catch (RemoteException e) {
				LOGGER.error(e);
			}
		}
		return INSTANCE;
	}

	@Override
	public byte[] requestFromServer(String id) throws RemoteException, DronologyServiceException {
		return FlightRouteplanningService.getInstance().requestFromServer(id);
	}

	@Override
	public void transmitToServer(String id, byte[] content) throws RemoteException, DronologyServiceException {
		FlightRouteplanningService.getInstance().transmitToServer(id, content);

	}

	@Override
	public boolean addItemChangeListener(IItemChangeListener listener) throws RemoteException {
		return FlightRouteplanningService.getInstance().addItemChangeListener(listener);
	}

	@Override
	public boolean removeItemChangeListener(IItemChangeListener listener) throws RemoteException {
		return FlightRouteplanningService.getInstance().removeItemChangeListener(listener);
	}

	@Override
	public Collection<FlightRouteInfo> getItems() throws RemoteException {
		return FlightRouteplanningService.getInstance().getItems();
	}

	@Override
	public FlightRouteInfo createItem() throws RemoteException, DronologyServiceException {
		return FlightRouteplanningService.getInstance().createItem();
	}

	@Override
	public void deleteItem(String itemid) throws RemoteException, DronologyServiceException {
		FlightRouteplanningService.getInstance().deleteItem(itemid);

	}

	@Override
	public Collection<FlightRouteCategoryInfo> getFlightPathCategories() {
		return FlightRouteplanningService.getInstance().getFlightPathCategories();
	}

}