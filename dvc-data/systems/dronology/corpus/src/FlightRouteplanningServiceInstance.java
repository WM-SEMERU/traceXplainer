package edu.nd.dronology.services.instances.flightroute;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.nd.dronology.core.util.Waypoint;
import edu.nd.dronology.services.core.api.IFileChangeNotifyable;
import edu.nd.dronology.services.core.api.ServiceInfo;
import edu.nd.dronology.services.core.base.AbstractFileTransmitServiceInstance;
import edu.nd.dronology.services.core.info.FlightRouteCategoryInfo;
import edu.nd.dronology.services.core.info.FlightRouteInfo;
import edu.nd.dronology.services.core.items.FlightRoute;
import edu.nd.dronology.services.core.items.IFlightRoute;
import edu.nd.dronology.services.core.persistence.FlightRoutePersistenceProvider;
import edu.nd.dronology.services.core.persistence.PersistenceException;
import edu.nd.dronology.services.core.util.DronologyConstants;
import edu.nd.dronology.services.core.util.DronologyServiceException;
import edu.nd.dronology.services.core.util.ServiceIds;
import edu.nd.dronology.services.instances.DronologyElementFactory;
import edu.nd.dronology.services.supervisor.SupervisorService;
import edu.nd.dronology.util.FileUtil;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

/**
 * 
 * The {@link FlightRouteplanningServiceInstance} provides capabilities for retrieving {@link FlightRoute} from the file system.<br>
 * When a new {@link FlightRoute} is detected in the file system it gets automatically loaded.<br>
 * Routes are provided via {@link FlightRouteInfo} proxies containing, basic info on the flight route.
 * 
 *   
 * 
 * @author Michael Vierhauser
 * 
 *
 */
public class FlightRouteplanningServiceInstance extends AbstractFileTransmitServiceInstance<FlightRouteInfo>
		implements IFileChangeNotifyable, IFlightRouteplanningServiceInstance {
 
	private static final ILogger LOGGER = LoggerProvider.getLogger(FlightRouteplanningServiceInstance.class);

	private static final int ORDER = 2;

	public static final String EXTENSION = DronologyConstants.EXTENSION_FLIGHTROUTE;

	private Map<String, FlightRouteInfo> flightPaths = new Hashtable<>();

	private Collection<FlightRouteCategoryInfo> categories = new ArrayList<>();

	public FlightRouteplanningServiceInstance() {
		super(ServiceIds.SERVICE_FLIGHROUTE, "Routeplanning Management", EXTENSION);

		categories.add(new FlightRouteCategoryInfo("South-Bend Area", "sba"));
		categories.add(new FlightRouteCategoryInfo("River", "river"));
		categories.add(new FlightRouteCategoryInfo("Default", "Default"));
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
	public FlightRouteInfo createItem() throws DronologyServiceException {
		FlightRoutePersistenceProvider persistor = FlightRoutePersistenceProvider.getInstance();
		IFlightRoute flightRoute = DronologyElementFactory.createNewFlightPath();
		flightRoute.setName("New-FlightRoute");
		String savePath = FileUtil.concat(storagePath, flightRoute.getId(), EXTENSION);

		try {
			persistor.saveItem(flightRoute, savePath);
		} catch (PersistenceException e) {
			throw new DronologyServiceException("Error when creating flight route: " + e.getMessage());
		}
		return new FlightRouteInfo(flightRoute.getName(), flightRoute.getId());
	}

	@Override
	protected String getPath() {
		String path = SupervisorService.getInstance().getFlightPathLocation();
		return path;
	}

	@Override
	protected FlightRouteInfo fromFile(String id, File file) throws Throwable {
		IFlightRoute atm = FlightRoutePersistenceProvider.getInstance().loadItem(file.toURI().toURL());
		FlightRouteInfo info = new FlightRouteInfo(atm.getName(), id);
		info.setCategory(atm.getCategory());
		for (Waypoint waypoint : atm.getWaypoints()) {
			info.addWaypoint(waypoint);
		}

		BasicFileAttributes attr = Files.readAttributes(Paths.get(file.toURI()), BasicFileAttributes.class);
		if (atm.getWaypoints().size() > 1) {
			double distance = calculateDistance(atm.getWaypoints());
			info.setRouteLength(distance);
		} else {
			info.setRouteLength(0);
		}
		info.setDateCreated(attr.creationTime().toMillis());
		info.setDateModified(attr.lastModifiedTime().toMillis());
		info.setDescription(atm.getDescription());
		return info;
	} 

	private double calculateDistance(List<Waypoint> waypoints) {
		LinkedList<Waypoint> ll = new LinkedList<>(waypoints);
		Waypoint current = ll.remove(0);
		double distance = 0;
		while (!ll.isEmpty()) {
			Waypoint next = ll.remove(0);
			distance += Math.abs(current.getCoordinate().distance(next.getCoordinate()));
			current = next;
		}
		return distance;
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
				HashSet<Entry<String, FlightRouteInfo>> allEntries = new HashSet(flightPaths.entrySet());
				for (Entry<String, FlightRouteInfo> e : allEntries) {
					if (e.getValue().getId().equals(changed)) {
						flightPaths.remove(e.getKey());
					}
				}
			}
		}
	}

	@Override
	public Collection<FlightRouteCategoryInfo> getFlightPathCategories() {
		return Collections.unmodifiableCollection(categories);
	}

	@Override
	public FlightRouteInfo getItem(String name) throws DronologyServiceException {
		for (FlightRouteInfo item : itemmap.values()) {
			if (item.getId().equals(name)) {
				return item;
			}
		}
		throw new DronologyServiceException("Flighroute '" + name + "' not found");
	}

	@Override
	public FlightRouteInfo getRouteByName(String routeName) throws DronologyServiceException {
		for (FlightRouteInfo item : itemmap.values()) {
			if (item.getName().equals(routeName)) {
				return item;
			}
		}
		throw new DronologyServiceException("Flighroute '" + routeName + "' not found");
	}
}
