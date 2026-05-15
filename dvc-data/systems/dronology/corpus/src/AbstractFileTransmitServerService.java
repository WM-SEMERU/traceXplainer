package edu.nd.dronology.services.core.base;

import java.rmi.RemoteException;
import java.util.Collection;

import edu.nd.dronology.services.core.api.IFileTransmitServiceInstance;
import edu.nd.dronology.services.core.listener.IItemChangeListener;
import edu.nd.dronology.services.core.util.DronologyServiceException;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

/**
 * 
 * Abstract base class for all services that can handle files.
 * 
 * @author Michael Vierhauser 
 *
 * @param <S>
 *          The service instance.
 * @param <T>
 *          The type of file that is supported.
 */
public abstract class AbstractFileTransmitServerService<S extends IFileTransmitServiceInstance<T>, T>
		extends AbstractServerService<S> {

	private static final ILogger LOGGER = LoggerProvider.getLogger(AbstractFileTransmitServerService.class);

	public AbstractFileTransmitServerService() {
		super();
	}

	public byte[] requestFromServer(String id) throws DronologyServiceException {
		return serviceInstance.requestFromServer(id);
	}

	public void transmitToServer(String id, byte[] content) throws DronologyServiceException {
		serviceInstance.transmitToServer(id, content);
	}

	public boolean addItemChangeListener(IItemChangeListener listener) {
		return serviceInstance.addItemChangeListener(listener);
	}

	public boolean removeItemChangeListener(IItemChangeListener listener) {
		return serviceInstance.removeItemChangeListener(listener);
	}

	public Collection<T> getItems() throws RemoteException {
		return serviceInstance.getItems();
	}
	
	public T getItems(String id) throws RemoteException, DronologyServiceException {
		return serviceInstance.getItem(id);
	}

	public T createItem() throws RemoteException, DronologyServiceException {
		return serviceInstance.createItem();
	}

	public void deleteItem(String itemid) throws DronologyServiceException {
		serviceInstance.deleteItem(itemid);

	}

}
