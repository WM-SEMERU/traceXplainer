package edu.nd.dronology.services.core.base;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.nd.dronology.services.core.api.IFileChangeNotifyable;
import edu.nd.dronology.services.core.api.IFileTransmitServiceInstance;
import edu.nd.dronology.services.core.info.RemoteInfoObject;
import edu.nd.dronology.services.core.listener.IItemChangeListener;
import edu.nd.dronology.services.core.util.DronologyServiceException;
import edu.nd.dronology.services.core.util.FileManager;
import edu.nd.dronology.util.FileUtil;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

/**
 * 
 * Abstract base class for all services that allow transferring files as a byte
 * array
 * 
 * @author Michael Vierhauser
 *
 * 
 */
public abstract class AbstractFileTransmitServiceInstance<ITEM_TYPE extends RemoteInfoObject>
		extends AbstractServiceInstance implements IFileTransmitServiceInstance<ITEM_TYPE>, IFileChangeNotifyable {

	private static final ILogger LOGGER = LoggerProvider.getLogger(AbstractFileTransmitServiceInstance.class);

	protected String extension;
	protected FileManager fileManager;
	protected List<IItemChangeListener> listeners = new ArrayList<>();
	protected Map<String, ITEM_TYPE> itemmap = new Hashtable<>();
	protected String storagePath;

	public AbstractFileTransmitServiceInstance(String ID, String description, String extension) {
		super(ID, description);
		this.extension = extension;
		storagePath = getPath();
		fileManager = new FileManager(this, storagePath, extension);
	}

	@Override
	public Collection<ITEM_TYPE> getItems() {
		List<ITEM_TYPE> charts;
		synchronized (itemmap) {
			charts = new ArrayList<>(itemmap.values());
		}
		return Collections.unmodifiableList(charts);
	}

	@Override
	public ITEM_TYPE getItem(String id) throws DronologyServiceException {
		synchronized (itemmap) {
			if (itemmap.containsKey(id)) {
				return itemmap.get(id);
			}
		}
		throw new DronologyServiceException("Item '" + id + "' not found");
	}

	@Override
	public byte[] requestFromServer(String id) throws DronologyServiceException {
		return fileManager.getFileAsByteArray(id);
	}

	@Override
	public void transmitToServer(String id, byte[] content) throws DronologyServiceException {
		fileManager.saveByteArrayToFile(id, content);
	}

	@Override
	public void notifyFileChange(Set<String> changed) {
		reloadItems();
		Set<String> info = new TreeSet<>();
		for (String s : changed) {
			String id = s.replace("." + extension, "");
			if (itemmap.containsKey(id)) {
				info.add("ADDED/UPDATED: " + itemmap.get(id).getName() + "[" + id + "]");
			} else {
				info.add("REMOVED: [" + id + "]");
			}
		}

		List<IItemChangeListener> notifyList;
		synchronized (listeners) {
			notifyList = new ArrayList<>(listeners);
		}
		for (IItemChangeListener listener : notifyList) {
			try {
				listener.itemChanged(info);
			} catch (RemoteException e) {
				LOGGER.error(e);
				listeners.remove(listener);
			}
		}

	}

	protected void reloadItems() {
		try {
			itemmap.clear();
			File[] files = fileManager.loadFiles();
			for (File file : files) {
				String id = file.getName().replace("." + extension, "");
				ITEM_TYPE item = null;
				try {
					item = fromFile(id, file);
				} catch (Throwable e) {
					LOGGER.error("Error loading file with id:" + id + "file: " + file.getAbsolutePath(), e);
				}
				if (item != null) {
					itemmap.put(id, item);
				}
			}
		} catch (Exception e) {
			LOGGER.error(e);
		}
	}

	@Override
	public void deleteItem(String itemid) throws DronologyServiceException {
		String savelocation = FileUtil.concat(storagePath, itemid, extension);
		File f = new File(savelocation);
		if (f.exists()) {
			boolean success = f.delete();
			LOGGER.info((success ? "Done" : "Failed") + " deleting Item: " + savelocation);
			reloadItems();
		} else {
			throw new DronologyServiceException("Item '" + itemid + "' does not exist on server");
		}

	}

	@Override
	public boolean addItemChangeListener(IItemChangeListener listener) {
		synchronized (listeners) {
			if (!listeners.contains(listener)) {
				LOGGER.info("Adding new Item Change Listener");
				return listeners.add(listener);
			}
			LOGGER.info("Listener instance already registered");
			return false;
		}
	}

	@Override
	public boolean removeItemChangeListener(IItemChangeListener listener) {
		boolean success;
		synchronized (listener) {
			success = listeners.remove(listener);
		}
		if (success) {
			LOGGER.info("Removed Item Change Listener");
		} else {
			LOGGER.info("Failed removing Item Change Listener");
		}
		return success;
	}

	protected abstract String getPath();

	protected abstract ITEM_TYPE fromFile(String id, File file) throws Throwable;

}
