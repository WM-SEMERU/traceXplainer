package edu.nd.dronology.ui.vaadin.activeflights;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.LMarker;
import org.vaadin.addon.leaflet.LPolyline;
import org.vaadin.addon.leaflet.shared.Point;

import com.vaadin.server.Resource;
import com.vaadin.ui.Notification;

import edu.nd.dronology.core.util.Waypoint;
import edu.nd.dronology.core.vehicle.IUAVProxy;
import edu.nd.dronology.services.core.info.FlightPlanInfo;
import edu.nd.dronology.services.core.remote.IDroneSetupRemoteService;
import edu.nd.dronology.services.core.remote.IFlightManagerRemoteService;
import edu.nd.dronology.services.core.util.DronologyServiceException;
import edu.nd.dronology.ui.vaadin.connector.BaseServiceProvider;
import edu.nd.dronology.ui.vaadin.start.MyUI;
import edu.nd.dronology.ui.vaadin.utils.ImageProvider;
import edu.nd.dronology.ui.vaadin.utils.MapMarkerUtilities;
import edu.nd.dronology.ui.vaadin.utils.UIWayPoint;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

/**
 * Utility class for drawing marker and UAV icons on the map <br>
 * <b>NEEDS ADDITIONAL CLEANUP & REFACTORING!<b>
 * 
 * @author Michael Vierhauser
 *
 */
public class MapDrawingUtil {

	private static final ILogger LOGGER = LoggerProvider.getLogger(MapDrawingUtil.class);

	private LMap leafletMap;
	private MapMarkerUtilities utilities;

	private List<List<LMarker>> wayPointMarkers = new ArrayList<>();
	private List<List<LPolyline>> flightRoutes = new ArrayList<>();
	private ArrayList<LMarker> markers = new ArrayList<>();

	private Resource droneIconFocused = ImageProvider.getFocusUAVResource();
	private Resource droneIcon = ImageProvider.getDefaultUAVResource(); 
	private Resource droneIconSelected = ImageProvider.getSelectedUAVResource();

	private Resource dotIcon = ImageProvider.getDotIconResource();

	private AFMapComponent mapComponent;

	BaseServiceProvider provider;
	private IFlightManagerRemoteService flightManagerService;
	private IDroneSetupRemoteService droneSetupService;

	private double ACHN_X = 11;

	private double ANCH_Y = 23;

	public MapDrawingUtil(LMap leafletMap, AFMapComponent mapComponent) {
		this.leafletMap = leafletMap;
		this.mapComponent = mapComponent;
		utilities = new MapMarkerUtilities(leafletMap);
		initConnection();

	}

	private void initConnection() {
		try {
			provider = MyUI.getProvider();
			flightManagerService = (IFlightManagerRemoteService) provider.getRemoteManager()
					.getService(IFlightManagerRemoteService.class);
			droneSetupService = (IDroneSetupRemoteService) provider.getRemoteManager()
					.getService(IDroneSetupRemoteService.class);
		} catch (RemoteException | DronologyServiceException e) {
			MyUI.setConnected(false);// reconnect to dronology if connection is lost
			try {
				Notification.show("Reconnecting...");
				droneSetupService = (IDroneSetupRemoteService) provider.getRemoteManager()
						.getService(IDroneSetupRemoteService.class);
				flightManagerService = (IFlightManagerRemoteService) provider.getRemoteManager()
						.getService(IFlightManagerRemoteService.class);
			} catch (RemoteException | DronologyServiceException e1) {
				Notification.show("Reconnecting...");
			}
			Notification.show("Reconnecting...");

		}

	}

	/**
	 * This function gets the flight routes from dronology core and draws them on the map.
	 * 
	 * @param focused
	 *          this is the drone that is focused in the AFInfoPanel. It's flight route will be orange
	 * @param checked
	 *          this is a list of drones that have their checkbox checked in the AFInfoPanel. Their routes will be black.
	 */
	public void addActiveFlightRoutes(String focused, List<String> checked) {
		try {
			Collection<FlightPlanInfo> currentFlights = flightManagerService.getCurrentFlights();
			for (FlightPlanInfo e : currentFlights) { // goes through each route
				List<Waypoint> coordinates = e.getWaypoints();
				List<UIWayPoint> wayPoints = new ArrayList<>();
				List<LMarker> wayPointMarker = new ArrayList<>();
				int i = 0;
				for (Waypoint coord : coordinates) { // goes through all the coordinates in each route
					Point point = new Point(coord.getCoordinate().getLatitude(), coord.getCoordinate().getLongitude());
					UIWayPoint wayPoint = new UIWayPoint(point, nextReached(coordinates, i + 1));
					wayPoints.add(wayPoint);
					if (wayPointMarkers.size() != currentFlights.size()) { // adds the waypoints to the map first
						LMarker marker = new LMarker(point);
						marker.setIcon(dotIcon);
						marker.setIconSize(new Point(10, 10));
						marker.addMouseOverListener(mapComponent.getWaypointListener());
						wayPointMarker.add(marker);
						leafletMap.addComponent(marker);
					}
					i++;
				}
				List<LPolyline> polyLines = new ArrayList<>(); // draws the lines and loads them into a list
				if (e.getDroneId().equals(focused)) {
					utilities.removeAllLines();
					polyLines = utilities.drawLinesForWayPoints(wayPoints, 2, true);
				} else {
					boolean drawn = false;
					for (String name : checked) {
						if (e.getDroneId().equals(name)) { 
							utilities.removeAllLines();
							polyLines = utilities.drawLinesForWayPoints(wayPoints, 1, true);
							drawn = true;
						}
					}
					if (!drawn) {
						// utilities.removeAllLines();
						polyLines = utilities.drawLinesForWayPoints(wayPoints, 0, true);
					}
				}
				flightRoutes.add(polyLines); // keep a list of all lines and markers
				if (wayPointMarkers.size() != currentFlights.size())
					wayPointMarkers.add(wayPointMarker);
			}
		} catch (RemoteException e) { // reconnect to dronology if connection is lost
			initConnection();
		}
	}

	/**
	 * updates the flight routes. Deletes old ones, adds new ones, and redraws the lines to different colors as each waypoint is reached
	 * 
	 * @param focused
	 *          this is the drone that is focused in the AFInfoPanel. It's flight route will be orange
	 * @param checked
	 *          this is a list of drones that have their checkbox checked in the AFInfoPanel. Their routes will be black.
	 */
	public void updateActiveFlightRoutes(String focused, List<String> checked) {
		try {
			Collection<FlightPlanInfo> currentFlights = flightManagerService.getCurrentFlights();
			if (currentFlights.size() != flightRoutes.size() || true) {
				utilities.removeAllLines();
				boolean exists = true; // determines if flight route is still active
				for (List<LMarker> e : wayPointMarkers) {
					boolean individualExist = false; // helper variable to determine if each flight route is still active
					for (FlightPlanInfo q : currentFlights) {
						if (e.get(0).getPoint().getLat() == q.getWaypoints().get(0).getCoordinate().getLatitude()
								&& e.get(0).getPoint().getLon() == q.getWaypoints().get(0).getCoordinate().getLongitude()) {
							individualExist = true;
						}
					}
					if (individualExist == false)
						exists = false;
				}
				if (!exists || wayPointMarkers.size() != currentFlights.size()) { // if flight doesn't exist, remove it's waypoint markers
					for (List<LMarker> lmarkers : wayPointMarkers) {
						for (LMarker m : lmarkers) {
							utilities.getMap().removeComponent(m);
						}
					}
					wayPointMarkers.clear();
					if (!mapComponent.getFollow() && flightRoutes.size() < currentFlights.size()) // only reset the center when a flight route is added
						mapComponent.setAverageCenter();
				}
			}
			flightRoutes.clear();
			/*
			 * if (wayPointMarkers.size() != flightRoutes.size()){ for (ArrayList<LMarker> e:wayPointMarkers){ utilities.removeAllMarkers(e); } wayPointMarkers.clear(); } flightRoutes.clear();
			 */
			this.addActiveFlightRoutes(focused, checked); // redraw the flight routes
			// }

		} catch (RemoteException e) { // reconnect to dronology
			initConnection();
		}
	}

	/**
	 * This function updates the position of the drone icons on the map
	 * 
	 * @param focused
	 *          this is the drone that is focused in the AFInfoPanel. It's flight route will be orange
	 * @param checked
	 *          this is a list of drones that have their checkbox checked in the AFInfoPanel. Their routes will be black.
	 */
	public void updateDroneMarkers(String focused, List<String> checked) {
		try {
			Collection<IUAVProxy> drones = droneSetupService.getActiveUAVs();
			ArrayList<LMarker> remove = new ArrayList<>();
			if (markers.size() == drones.size()) {
				for (LMarker marker : markers) {
					boolean exists = false;
					for (IUAVProxy e : drones) {
						if (marker.getId().equals(e.getID())) { // if the marker correlates to the drone
							Point temp = new Point();
							temp.setLat(e.getLatitude()); // update location
							temp.setLon(e.getLongitude());
							marker.setPoint(temp);
							if (marker.getId().equals(focused))
								marker.setIcon(droneIconFocused);
							else {
								boolean chosen = false;
								for (String name : checked) {
									if (marker.getId().equals(name)) {
										marker.setIcon(droneIconSelected);
										chosen = true;
									}
								}
								if (!chosen)
									marker.setIcon(droneIcon);
							}
							exists = true;
						}
					}
					if (!exists) { // if the drone that is represented by the marker is no longer active or if the drone is new
						remove.add(marker);
						for (IUAVProxy e1 : drones) {
							boolean old = false;
							for (LMarker marker1 : markers) {
								if (e1.getID().equals(marker1.getId()))
									old = true;
							}
							if (!old) { // the drone does not have a marker represented by it
								LMarker newMarker = new LMarker(e1.getLatitude(), e1.getLongitude());
								newMarker.setId(e1.getID());
								if (marker.getId().equals(focused))
									marker.setIcon(droneIconFocused);
								else {
									boolean chosen = false;
									for (String name : checked) {
										if (marker.getId().equals(name)) {
											marker.setIcon(droneIconSelected);
											chosen = true;
										}
									}
									if (!chosen)
										marker.setIcon(droneIcon);
								}
								newMarker.setIconSize(new Point(33, 33));
								newMarker.setIconAnchor(new Point(ACHN_X, ANCH_Y));
								newMarker.addMouseOverListener(mapComponent.getDroneListener());
								markers.add(newMarker);
								leafletMap.addComponent(newMarker);
								if (!mapComponent.getFollow())
									mapComponent.setAverageCenter();
							}
						}
					}
				}
			} else if (markers.size() < drones.size()) {
				for (IUAVProxy e : drones) {
					boolean exists = false;
					for (LMarker marker : markers) {
						if (e.getID().equals(marker.getId()))
							exists = true;
					}
					if (!exists) {
						LMarker marker = new LMarker(e.getLatitude(), e.getLongitude()); // create new marker for the drone
						marker.setId(e.getID());
						if (marker.getId().equals(focused))
							marker.setIcon(droneIconFocused);
						else {
							boolean chosen = false;
							for (String name : checked) {
								if (marker.getId().equals(name)) {
									marker.setIcon(droneIconSelected);
									chosen = true;
								}
							}
							if (!chosen)
								marker.setIcon(droneIcon);
						}
						marker.setIconSize(new Point(33, 33));
						marker.addMouseOverListener(mapComponent.getDroneListener());
						marker.setIconAnchor(new Point(ACHN_X, ANCH_Y));
						markers.add(marker);
						leafletMap.addComponent(marker);
						if (!mapComponent.getFollow())
							mapComponent.setAverageCenter();

					}
				}
			} else if (markers.size() > drones.size()) {
				for (LMarker marker : markers) {
					boolean exists = false;
					for (IUAVProxy e : drones) {
						if (e.getID().equals(marker.getId()))
							exists = true;
					}
					if (!exists) // remove marker that represents a deactivated drone
						remove.add(marker);
				}
			}
			if (remove.size() > 0) {
				for (LMarker e : remove) {
					markers.remove(e);
					leafletMap.removeComponent(e);
					if (!mapComponent.getFollow())
						mapComponent.setAverageCenter();
				}
				remove.clear();
			}
		} catch (RemoteException e) { // reconnect to dronology
			initConnection();
		}
	}

	/**
	 * This function adds icons on the map that represent each drone's position.
	 * 
	 * @param focused
	 *          this is the drone that is focused in the AFInfoPanel. It's flight route will be orange
	 * @param checked
	 *          this is a list of drones that have their checkbox checked in the AFInfoPanel. Their routes will be black.
	 */
	public void addDroneMarkers(String focused, List<String> checked) {
		Collection<IUAVProxy> drones = Collections.emptyList();
		try {
			drones = droneSetupService.getActiveUAVs();
		} catch (RemoteException e1) {
			initConnection();
		}
		for (

		IUAVProxy e : drones) {
			LMarker marker = new LMarker(e.getLatitude(), e.getLongitude());
			marker.setId(e.getID());
			if (marker.getId().equals(focused))
				marker.setIcon(droneIconFocused);
			else {
				boolean chosen = false;
				for (String name : checked) {
					if (marker.getId().equals(name)) {
						marker.setIcon(droneIconSelected);
						chosen = true;
					}
				}
				if (!chosen)
					marker.setIcon(droneIcon);
			}
			marker.setIconSize(new Point(30, 30));
			marker.setIconAnchor(new Point(ACHN_X, ANCH_Y));
			marker.addMouseOverListener(mapComponent.getDroneListener());
			markers.add(marker);
			leafletMap.addComponent(marker);
			if (!mapComponent.getFollow())
				mapComponent.setAverageCenter();
		}
	}

	/**
	 * assists in the logic of updating flight routes
	 * 
	 * @param coordinates
	 * @param i
	 * @return
	 */
	private boolean nextReached(List<Waypoint> coordinates, int i) {
		if (coordinates.size() <= i) {
			return false;
		}
		Waypoint next = coordinates.get(i);
		return next.isReached();
	}

}
