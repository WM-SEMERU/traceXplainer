package edu.nd.dronology.ui.vaadin.activeflights;

import java.awt.MouseInfo;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.LMarker;
import org.vaadin.addon.leaflet.LeafletMouseOverEvent;
import org.vaadin.addon.leaflet.LeafletMouseOverListener;
import org.vaadin.addon.leaflet.shared.Point;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import edu.nd.dronology.core.util.Waypoint;
import edu.nd.dronology.core.vehicle.IUAVProxy;
import edu.nd.dronology.services.core.info.FlightPlanInfo;
import edu.nd.dronology.services.core.remote.IDroneSetupRemoteService;
import edu.nd.dronology.services.core.remote.IFlightManagerRemoteService;
import edu.nd.dronology.services.core.util.DronologyServiceException;
import edu.nd.dronology.ui.vaadin.connector.BaseServiceProvider;
import edu.nd.dronology.ui.vaadin.map.LeafletmapFactory;
import edu.nd.dronology.ui.vaadin.start.MyUI;
import edu.nd.dronology.ui.vaadin.utils.Configuration;

/**
 * This is the map component for the Active Flights UI
 * 
 * @author Jinghui Cheng
 * 
 */
public class AFMapComponent extends CustomComponent { 
	private static final long serialVersionUID = 1L;

	private LMap leafletMap;
	// private ArrayList<LMarker> markers = new ArrayList<>();
	private Collection<IUAVProxy> drones;
	private Collection<FlightPlanInfo> currentFlights;
	private IDroneSetupRemoteService service;
	private IFlightManagerRemoteService flightManagerService;
	private BaseServiceProvider provider = MyUI.getProvider();

	private boolean follow = false;
	private boolean followZoom = false;
	private VerticalLayout content = new VerticalLayout();
	private AbsoluteLayout followLayout = new AbsoluteLayout();
	private AFFollowBar followBar;
	private AbsoluteLayout layout = new AbsoluteLayout();
	private PopupView popup;
	private PopupView dronePopup;

	private AFInfoPanel panel;
 
	private MapDrawingUtil drawingutil;

	public AFMapComponent(AFInfoPanel panel) {
		this.panel = panel;

		this.setWidth("100%");
		addStyleName("map_component");
		addStyleName("af_map_component");

		leafletMap = LeafletmapFactory.generateMap();
		drawingutil = new MapDrawingUtil(leafletMap, this);

		try {
			service = (IDroneSetupRemoteService) provider.getRemoteManager().getService(IDroneSetupRemoteService.class);
			flightManagerService = (IFlightManagerRemoteService) provider.getRemoteManager()
					.getService(IFlightManagerRemoteService.class);
			// currentFlights = flightRouteService.getFlightDetails().getCurrentFlights();
		} catch (RemoteException | DronologyServiceException e) {
			e.printStackTrace();
			MyUI.setConnected(false);
		}
		List<String> temp = new ArrayList<>();
		drawingutil.addDroneMarkers("", temp);
		drawingutil.addActiveFlightRoutes("", temp);
		this.setAverageCenter();
		double screenHeight = UI.getCurrent().getPage().getBrowserWindowHeight();
		int layoutHeight = (int) Math.rint(screenHeight * 0.9);
		layout.setHeight(Integer.toString(layoutHeight) + "px");
		layout.addComponent(leafletMap);
		popup = createWayPointPopupView();
		dronePopup = createDronePopupView();
		layout.addComponents(popup, dronePopup);
		content.addComponent(layout);
		setCompositionRoot(content);
	}

	public void setCenter(double centerLat, double centerLon) {
		leafletMap.setCenter(centerLat, centerLon);
	}

	public void setZoomLevel(double zoomLevel) {
		leafletMap.setZoomLevel(zoomLevel);
	}

	public double getCenterLat() {
		return leafletMap.getCenter().getLat();
	}

	public double getCenterLon() {
		return leafletMap.getCenter().getLon();
	}

	public double getZoomLevel() {
		return leafletMap.getZoomLevel();
	}
 
	public LMap getMapInstance() {
		return leafletMap;
	}

	/**
	 * This function sets the center and zoom of the map to include all drones and their flight routes. It finds the average latitude and longitude first. It then finds the point farthest from the
	 * center and bases the zoom level off of that point.
	 */
	public void setAverageCenter() {
		if (content.getComponentIndex(layout) == -1) { // if coming out of follow mode
			content.removeAllComponents();
			leafletMap.removeStyleName("af_leaflet_map_edit_mode");
			content.addComponent(layout);
		}
		Configuration configuration = Configuration.getInstance();
		try {
			service = (IDroneSetupRemoteService) provider.getRemoteManager().getService(IDroneSetupRemoteService.class);
			drones = service.getActiveUAVs();
			double avgLat = 0;
			double avgLon = 0;
			int numPoints = 0;
			for (IUAVProxy e : drones) { // finding the average latitude and longitude of the drones and flight routes
				avgLat += e.getLatitude();
				avgLon += e.getLongitude();
				numPoints++;
			}
			currentFlights = flightManagerService.getCurrentFlights();
			for (FlightPlanInfo e : currentFlights) {
				List<Waypoint> coordinates = e.getWaypoints();
				for (Waypoint coord : coordinates) {
					avgLat += coord.getCoordinate().getLatitude();
					avgLon += coord.getCoordinate().getLongitude();
					numPoints++;
				}
			}
			avgLat /= (numPoints * 1.0);
			avgLon /= (numPoints * 1.0);
			double farthestLat = 0;
			double farthestLon = 0; // finding the farthest point from the center
			for (IUAVProxy e : drones) {
				if (Math.abs(e.getLatitude() - avgLat) > farthestLat) {
					farthestLat = Math.abs(e.getLatitude() - avgLat);
				}
				if (Math.abs(e.getLongitude() - avgLon) > farthestLon) {
					farthestLon = Math.abs(e.getLongitude() - avgLon);
				}
			}
			for (FlightPlanInfo e : currentFlights) {
				List<Waypoint> coordinates = e.getWaypoints();
				for (Waypoint coord : coordinates) {
					if (Math.abs(coord.getCoordinate().getLatitude() - avgLat) > farthestLat) {
						farthestLat = Math.abs(coord.getCoordinate().getLatitude() - avgLat);
					}
					if (Math.abs(coord.getCoordinate().getLongitude() - avgLon) > farthestLon) {
						farthestLon = Math.abs(coord.getCoordinate().getLongitude() - avgLon);
					}
				}
			}
			Point point = new Point(avgLat, avgLon);
			double zoom;
			if (farthestLat == 0 && farthestLon == 0) {
				zoom = 14;
			} else { // sets the zoom based on the calculation of degrees on the map per zoom level
				zoom = Math.floor(Math.log10(180.0 / Math.max(farthestLat, farthestLon)) / Math.log10(2));
			}
			leafletMap.setCenter(point, zoom);
		} catch (RemoteException | DronologyServiceException e1) {
			MyUI.setConnected(false);
			e1.printStackTrace();
		} 
		if (drones.size() < 1) {
			Point point = new Point(configuration.getMapCenterLat(), configuration.getMapCenterLon());
			double zoom = configuration.getMapDefaultZoom();
			leafletMap.setCenter(point, zoom);
		}

	}

	/**
	 * @return follow is a boolean variable that is true when the map is following drones
	 */
	public boolean getFollow() {
		return this.follow;
	}

	public void setFollow(boolean follow) {
		this.follow = follow;
	}

	/**
	 * @return followZoom determines whether the map should zoom in on the drones in follow mode. Only happens once initially when the user clicks the button to follow the drones on the map.
	 */
	public boolean getFollowZoom() {
		return this.followZoom;
	}

	public void setFollowZoom(boolean followZoom) {
		this.followZoom = followZoom;
	}

	/**
	 * This function sets the center of the map as an average of the drones that it is following. This will constantly change as each drone flies.
	 * 
	 * @param names
	 *          The list of drone names that the map should be following
	 */
	public void followDrones(List<String> names) {
		if (names.size() < 1) {
			this.follow = false;
			if (content.getComponentIndex(layout) == -1) { // if not in follow mode
				content.removeAllComponents();
				leafletMap.removeStyleName("af_leaflet_map_edit_mode");
				content.addComponent(layout);
			}
			return;
		}
		if (this.follow == false) {
			if (content.getComponentIndex(layout) == -1) { // if not in follow mode
				content.removeAllComponents();
				leafletMap.removeStyleName("af_leaflet_map_edit_mode");
				content.addComponent(layout);
			}
			return;
		}
		try {
			service = (IDroneSetupRemoteService) provider.getRemoteManager().getService(IDroneSetupRemoteService.class);
			drones = service.getActiveUAVs();
			double avgLat = 0; // finds the average latitude and longitude
			double avgLon = 0;
			int numPoints = 0;
			for (IUAVProxy e : drones) {
				for (String name : names) {
					if (e.getID().equals(name)) {
						avgLat += e.getLatitude();
						avgLon += e.getLongitude();
						numPoints++;
					}
				}
			}
			avgLat /= (numPoints * 1.0);
			avgLon /= (numPoints * 1.0);
			double farthestLat = 0; // finds the farthest point from the center
			double farthestLon = 0;
			for (IUAVProxy e : drones) {
				for (String name : names) {
					if (e.getID().equals(name)) {
						if (Math.abs(e.getLatitude() - avgLat) > farthestLat) {
							farthestLat = Math.abs(e.getLatitude() - avgLat);
						}
						if (Math.abs(e.getLongitude() - avgLon) > farthestLon) {
							farthestLon = Math.abs(e.getLongitude() - avgLon);
						}
					}
				}
			}
			Point point = new Point(avgLat, avgLon); 
			if (this.followZoom) { // if the first time after the button click, set the zoom level to fit all drones
				double zoom;
				if (farthestLat == 0 && farthestLon == 0) {
					zoom = 17;
				} else {
					zoom = Math.floor(Math.log10(180.0 / Math.max(farthestLat, farthestLon)) / Math.log10(2));
				}
				leafletMap.setCenter(point, zoom);
				this.followZoom = false;
			} else {
				leafletMap.setCenter(point);
			}
			if (content.getComponentIndex(layout) != -1) { // change the map layout to display the follow bar.
				leafletMap.addStyleName("af_leaflet_map_edit_mode");
				followBar = new AFFollowBar(this, names);
				followLayout.addStyleName("af_mapabsolute_layout");
				followBar.addStyleName("bring_front");
				double screenHeight = UI.getCurrent().getPage().getBrowserWindowHeight();
				int layoutHeight = (int) Math.rint(screenHeight * 0.9);
				followLayout.setHeight(Integer.toString(layoutHeight) + "px");
				followLayout.addComponent(layout);
				followLayout.addComponent(followBar);
				content.removeAllComponents();
				content.addComponent(followLayout); 
			} else {
				followBar.updateUAVList(names);
			}
		} catch (RemoteException | DronologyServiceException e1) {
			MyUI.setConnected(false);
			e1.printStackTrace();
		}
	}

	/**
	 * this is a listener that displays an AFInfoBox in a popup over the drone that was just hovered over
	 * 
	 * @author Patrick Falvey
	 *
	 */
	private class DroneMouseListener implements LeafletMouseOverListener {

		@Override
		public void onMouseOver(LeafletMouseOverEvent event) {
			try {
				drones = service.getActiveUAVs();
			} catch (RemoteException e) {
				e.printStackTrace();
			}

			dronePopup.setVisible(false);
			dronePopup.setPopupVisible(false);
			LMarker leafletMarker = (LMarker) event.getSource();

			VerticalLayout popupContent = (VerticalLayout) dronePopup.getContent().getPopupComponent();
			popupContent.removeAllComponents();
			for (IUAVProxy e : drones) { // change the popup content to display the right AFInfoBox
				if (e.getID().equals(leafletMarker.getId())) {
					UAVStatusWrapper status = new UAVStatusWrapper(e);
					AFInfoBox box = new AFInfoBox(false, status, false);
					box.createContents();
					box.setBoxVisible(false);
					VerticalLayout boxes = panel.getBoxes();
					int numUAVs = panel.getNumUAVS();
					for (int i = 1; i < numUAVs + 1; i++) {
						AFInfoBox panelBox = (AFInfoBox) boxes.getComponent(i);
						if (panelBox.getName().equals(box.getName())) { // get the updated information from the AFInfoPanel
							box.setIsChecked(panelBox.getIsChecked());
							box.setHoverInPlace(panelBox.getHoverInPlace());
							box.update(status);

						}
					}
					box.getRouteButton().addClickListener(click -> {
						dronePopup.setPopupVisible(false);
					});
					box.getHomeButton().addClickListener(click -> {
						dronePopup.setPopupVisible(false);
					});
					box.getHoverSwitch().addValueChangeListener(click -> {
						for (int i = 1; i < numUAVs + 1; i++) {
							AFInfoBox panelBox = (AFInfoBox) boxes.getComponent(i);
							if (panelBox.getName().equals(box.getName())) {
								panelBox.setHoverInPlace(box.getHoverInPlace());
							}
						}
					});
					box.getCheckBox().addValueChangeListener(click -> { // if checkbox clicked in popup, it will change in AFInfoPanel
						if (box.getCheckBox().getValue()) {
							for (int i = 1; i < numUAVs + 1; i++) {
								AFInfoBox panelBox = (AFInfoBox) boxes.getComponent(i);
								if (panelBox.getName().equals(box.getName())) {
									panelBox.setIsChecked(true);
								}
							}
						} else {
							for (int i = 1; i < numUAVs + 1; i++) {
								AFInfoBox panelBox = (AFInfoBox) boxes.getComponent(i);
								if (panelBox.getName().equals(box.getName())) {
									panelBox.setIsChecked(false);
								}
							}
						}
					});
					popupContent.addComponent(box);
				}
			}
			/*
			 * find the location on the screen to display the popup. Takes the absolute position of the mouse and converts that to the relative position of the mouse on the map. Uses the map dimensions and
			 * the map position within the layout
			 */
			double mapWidth = UI.getCurrent().getPage().getBrowserWindowWidth() - 366.0;
			double mapHeight = UI.getCurrent().getPage().getBrowserWindowHeight() * 0.9;

			double xDegreeDifference = -(leafletMap.getCenter().getLon() - leafletMarker.getPoint().getLon());
			double yDegreeDifference = leafletMap.getCenter().getLat() - leafletMarker.getPoint().getLat();
			double degreePerZoom = (360.0 / (Math.pow(2, leafletMap.getZoomLevel())));
			double degreePerPixel = degreePerZoom / mapWidth;
			double xPixelDifference = (xDegreeDifference / degreePerPixel) / 3.0;
			double yPixelDifference = (yDegreeDifference / degreePerPixel) / 3.0;

			xPixelDifference = xPixelDifference * 0.55;

			double pixelsToLeftBorder = (mapWidth / 2.0) + xPixelDifference;
			double pixelsToTopBorder = (mapHeight / 2.0) + yPixelDifference;
			double mouseX = MouseInfo.getPointerInfo().getLocation().getX();
			double mouseY = MouseInfo.getPointerInfo().getLocation().getY();
			double mapTopLeftX = mouseX - pixelsToLeftBorder;
			double mapTopLeftY = mouseY - pixelsToTopBorder;

			double adjustedXLocation = mouseX - mapTopLeftX;
			double adjustedYLocation = mouseY - mapTopLeftY;

			layout.addComponent(dronePopup, "top:" + String.valueOf((int) adjustedYLocation) + "px;left:"
					+ String.valueOf((int) adjustedXLocation) + "px");

			dronePopup.setVisible(true);
			dronePopup.setPopupVisible(true);

		}
	}

	/**
	 * 
	 * @return returns the waypoint popup
	 */
	public PopupView createWayPointPopupView() {
		VerticalLayout popupContent = new VerticalLayout();
		popupContent.removeAllComponents();

		Label latitudeLabel = new Label();
		latitudeLabel.setId("latitude");

		Label longitudeLabel = new Label();
		longitudeLabel.setId("longitude");

		Label altitudeLabel = new Label();
		altitudeLabel.setId("altitude");

		Label transitSpeedLabel = new Label();
		transitSpeedLabel.setId("transitSpeed");

		popupContent.addComponent(latitudeLabel);
		popupContent.addComponent(longitudeLabel);
		popupContent.addComponent(altitudeLabel);
		popupContent.addComponent(transitSpeedLabel);

		PopupView popup = new PopupView(null, popupContent);

		popup.addStyleName("bring_front");
		popup.setVisible(false);
		popup.setPopupVisible(false);

		return popup;
	}

	/**
	 * 
	 * @return returns the drone popup
	 */
	public PopupView createDronePopupView() {
		VerticalLayout popupContent = new VerticalLayout();
		popupContent.removeAllComponents();

		popupContent.addComponent(new Label("Drone Information"));
		PopupView popup = new PopupView(null, popupContent);

		popup.addStyleName("bring_front");
		popup.setVisible(false);
		popup.setPopupVisible(false);
		return popup;
	}

	/**
	 * This listener displays a popup of information about a certain waypoint. Virtually the same listener used in the flight routes UI.
	 * 
	 * @author Patrick Falvey
	 *
	 */
	public class WaypointMouseListener implements LeafletMouseOverListener {

		@Override
		public void onMouseOver(LeafletMouseOverEvent event) {

			popup.setVisible(false);
			popup.setPopupVisible(false);
			LMarker leafletMarker = (LMarker) event.getSource();

			VerticalLayout popupContent = (VerticalLayout) popup.getContent().getPopupComponent();
			Iterator<Component> it = popupContent.iterator(); // iterates through the popup content and updates the waypoint information
			while (it.hasNext()) {
				Component c = it.next();
				try {
					currentFlights = flightManagerService.getCurrentFlights();
					for (FlightPlanInfo e : currentFlights) {
						List<Waypoint> coordinates = e.getWaypoints();
						for (Waypoint coord : coordinates) {
							if (coord.getCoordinate().getLatitude() == leafletMarker.getPoint().getLat()
									&& coord.getCoordinate().getLongitude() == leafletMarker.getPoint().getLon()) {
								if (c.getId() != null && c.getId().equals("latitude")) {
									Label l = (Label) c;
									l.setValue("Latitude: " + coord.getCoordinate().getLatitude());
								}
								if (c.getId() != null && c.getId().equals("longitude")) {
									Label l = (Label) c;
									l.setValue("Longitude: " + coord.getCoordinate().getLongitude());
								}
								if (c.getId() != null && c.getId().equals("altitude")) {
									Label l = (Label) c;
									l.setValue("Altitude: " + coord.getCoordinate().getAltitude());
								}
								if (c.getId() != null && c.getId().equals("transitSpeed")) {
									Label l = (Label) c;
									l.setValue("Transit Speed: " + coord.getApproachingspeed());
								}
							}
						}
					}
				} catch (RemoteException e) {
					try {
						Notification.show("Reconnecting...");
						service = (IDroneSetupRemoteService) provider.getRemoteManager().getService(IDroneSetupRemoteService.class);
						flightManagerService = (IFlightManagerRemoteService) provider.getRemoteManager()
								.getService(IFlightManagerRemoteService.class);
					} catch (RemoteException | DronologyServiceException e1) {
						MyUI.setConnected(false);
						Notification.show("Reconnecting...");
					}
					Notification.show("Reconnecting...");
				}
			}
			/*
			 * find the location on the screen to display the popup. Takes the absolute position of the mouse and converts that to the relative position of the mouse on the map. Uses the map dimensions and
			 * the map position within the layout
			 */
			double mapWidth = UI.getCurrent().getPage().getBrowserWindowWidth() - 366.0;
			double mapHeight = UI.getCurrent().getPage().getBrowserWindowHeight() * 0.9;

			double xDegreeDifference = -(leafletMap.getCenter().getLon() - leafletMarker.getPoint().getLon());
			double yDegreeDifference = leafletMap.getCenter().getLat() - leafletMarker.getPoint().getLat();
			double degreePerZoom = (360.0 / (Math.pow(2, leafletMap.getZoomLevel())));
			double degreePerPixel = degreePerZoom / mapWidth;
			double xPixelDifference = (xDegreeDifference / degreePerPixel) / 3.0;
			double yPixelDifference = (yDegreeDifference / degreePerPixel) / 3.0;

			xPixelDifference = xPixelDifference * 0.55;
			yPixelDifference = yPixelDifference * 0.6;

			double pixelsToLeftBorder = (mapWidth / 2.0) + xPixelDifference;
			double pixelsToTopBorder = (mapHeight / 2.0) + yPixelDifference;
			double mouseX = MouseInfo.getPointerInfo().getLocation().getX();
			double mouseY = MouseInfo.getPointerInfo().getLocation().getY();
			double mapTopLeftX = mouseX - pixelsToLeftBorder;
			double mapTopLeftY = mouseY - pixelsToTopBorder;

			double adjustedXLocation = mouseX - mapTopLeftX;
			double adjustedYLocation = mouseY - mapTopLeftY;

			layout.addComponent(popup, "top:" + String.valueOf((int) adjustedYLocation) + "px;left:"
					+ String.valueOf((int) adjustedXLocation) + "px");

			popup.setVisible(true);
			popup.setPopupVisible(true);
		}
	}

	public LeafletMouseOverListener getWaypointListener() {
		return new WaypointMouseListener();
	}

	public LeafletMouseOverListener getDroneListener() { 
		return new DroneMouseListener();
	} 

	public void refresh(String focused, List<String> checkedNames) {
		drawingutil.updateDroneMarkers(focused, checkedNames); 
		drawingutil.updateActiveFlightRoutes(focused, checkedNames);
	}

}