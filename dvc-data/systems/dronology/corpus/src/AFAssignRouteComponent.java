package edu.nd.dronology.ui.vaadin.activeflights;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;

import org.vaadin.teemu.switchui.Switch;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import edu.nd.dronology.services.core.info.FlightInfo;
import edu.nd.dronology.services.core.info.FlightRouteInfo;
import edu.nd.dronology.services.core.remote.IFlightManagerRemoteService;
import edu.nd.dronology.services.core.remote.IFlightRouteplanningRemoteService;
import edu.nd.dronology.services.core.util.DronologyServiceException;
import edu.nd.dronology.ui.vaadin.activeflights.AFDragLayout.WrappedComponent;
import edu.nd.dronology.ui.vaadin.connector.BaseServiceProvider;
import edu.nd.dronology.ui.vaadin.flightroutes.FRInfoBox;
import edu.nd.dronology.ui.vaadin.flightroutes.FRMainLayout;
import edu.nd.dronology.ui.vaadin.start.MyUI;
import edu.nd.dronology.ui.vaadin.utils.ImageProvider;
import edu.nd.dronology.ui.vaadin.utils.StyleConstants;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

/**
 * This is the UI for assigning new routes to a UAV
 * 
 * @author Patrick Falvey
 *  
 */ 

public class AFAssignRouteComponent extends CustomComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3476532205257979147L;

	private static final ILogger LOGGER = LoggerProvider.getLogger(AFAssignRouteComponent.class);

	private VerticalLayout content = new VerticalLayout();
	private HorizontalLayout topContent = new HorizontalLayout();
	private HorizontalLayout sideContent = new HorizontalLayout();
	private HorizontalLayout bottomButtons = new HorizontalLayout();
	private VerticalLayout sideButtons = new VerticalLayout(); 
	private AFDragLayout panelContent;
	private FRMainLayout frLayout = new FRMainLayout();
	private Panel sidePanel = new Panel();
	private Button cancel = new Button("Cancel");
	private Button apply = new Button("Apply");
	private Button left = new Button("<");
	private Button right = new Button(">");
	private int numRoutes = 0;
	private Switch hoverSwitch = new Switch();
	private Button returnToHome = new Button("Return to Home");
	private int index = -1;
	private int boxID = 0;

	private BaseServiceProvider provider = MyUI.getProvider();
	private IFlightManagerRemoteService flightRouteService;
	private FlightInfo flightRouteInfo = null;
	private IFlightRouteplanningRemoteService flightInfoService;

	private Image droneImage;
	private UAVStatusWrapper uavStatus;

	public AFAssignRouteComponent(UAVStatusWrapper uavStatus) {
		this.uavStatus = uavStatus;
	}

	public void createContents() {
		this.addStyleName(StyleConstants.AF_ASSING_ROUTE);
		topContent.addStyleName(StyleConstants.AF_ASSING_ROUTE_TOP_CONTENT);
		sideContent.addStyleName(StyleConstants.AF_ASSING_ROUTE_MIDDLE_CONTENT);
		bottomButtons.addStyleName(StyleConstants.AF_ASSING_ROUTE_BOTTOM_CONTENT);

		panelContent = new AFDragLayout(uavStatus.getName());

		droneImage = ImageProvider.getDefaultUAVImage();
		VerticalLayout statusContent = new VerticalLayout();

		createStatusPannel(statusContent);

		try {
			flightRouteService = (IFlightManagerRemoteService) provider.getRemoteManager()
					.getService(IFlightManagerRemoteService.class);
			flightRouteInfo = flightRouteService.getFlightInfo(uavStatus.getName());
		} catch (RemoteException e) {
			MyUI.setConnected(false);
			LOGGER.error(e);
		} catch (Exception e) {
			LOGGER.error(e);
		}

		numRoutes = flightRouteInfo.getPendingFlights().size();

		createSidePannel(sidePanel);

		bottomButtons.addComponents(cancel, apply);
		apply.addStyleName("btn-okay");
		content.addComponents(topContent, sideContent, bottomButtons);

		setCompositionRoot(content);

	}

	private void createSidePannel(Panel sidePanel) {
		sidePanel.addStyleName("fr_info_panel");
		sidePanel.addStyleName("control_panel");
		sidePanel.setCaption(numRoutes + " Routes Assigned");
		apply.setEnabled(true); 

		sideButtons.addComponents(left, right);
		sideButtons.setComponentAlignment(left, Alignment.MIDDLE_CENTER);
		sideButtons.setComponentAlignment(right, Alignment.MIDDLE_CENTER);

		// when adding a route to be assigned
		left.addClickListener(e -> {
			if (frLayout.getControls().getInfoPanel().getHighlightedFRInfoBox() != null) {
				FlightRouteInfo selectedFlight = frLayout.getControls().getInfoPanel().getHighlightedFRInfoBox()
						.getFlightRouteInfo();
				if (selectedFlight.getWaypoints().size() < 1) {
					Notification.show("There is no waypoint defined in " + selectedFlight.getName()
							+ ". You cannot assign an empty route to a UAV.");
				} else {
					addRoute(selectedFlight);
				}
			} else
				Notification.show("Please select route to assign.");
		});

		// when removing a route from the assigned list
		right.addClickListener(e -> {
			if (index != -1) {
				removeRoute(this.index);
				this.index = -1;
			} else
				Notification.show("Please select assigned route to remove.");
		});

		// when clicking on a route, focus the box and show the route on the map on the right
		panelContent.getSortableLayout().getVerticalLayout().addLayoutClickListener(e -> {
			WrappedComponent child = (WrappedComponent) e.getChildComponent();
			Component childContent = child.getContent();
			if (panelContent.getComponentIndex(childContent) != -1) {
				((FRInfoBox) childContent).addStyleName("info_box_focus");
				frLayout.switchRoute(((FRInfoBox) childContent));
			}
			index = panelContent.getComponentIndex(childContent);

			int numComponents = panelContent.getComponentCount();

			// when one route is clicked, the others go back to default background color
			for (int i = 0; i < numComponents; i++) {
				if (i != index) {
					panelContent.getComponent(i).removeStyleName("info_box_focus");
				}
			}
		});
		
		// be able to add a route to the left with a double click
		frLayout.getControls().getInfoPanel().getRoutes().addLayoutClickListener(e -> {
			if (e.isDoubleClick()) {	
				if (frLayout.getControls().getInfoPanel().getHighlightedFRInfoBox() != null) {
					FlightRouteInfo selectedFlight = frLayout.getControls().getInfoPanel().getHighlightedFRInfoBox().getFlightRouteInfo();
					if (selectedFlight.getWaypoints().size() < 1) {
						Notification.show("There is no waypoint defined in " + selectedFlight.getName()
								+ ". You cannot assign an empty route to a UAV.");
					} else {
						addRoute(selectedFlight);
					}
				}
				else Notification.show("Please select route to assign.");
			}
		});
		
		// be able to add a route to the right with a double click
		panelContent.getSortableLayout().getVerticalLayout().addLayoutClickListener(e -> {
			if (e.isDoubleClick()) {
				if (index != -1) {
					removeRoute(this.index);
					this.index = -1;
				} else
					Notification.show("Please select assigned route to remove.");
			}
		});
	
		
		
		sidePanel.setContent(panelContent);
		sideContent.addComponents(sidePanel, sideButtons, frLayout);

	}

	private void createStatusPannel(VerticalLayout statusContent) {

		Label statusInfo1 = new Label();
		Label statusInfo2 = new Label();
		Label statusInfo3 = new Label();

		statusInfo1.setValue("Assigning Routes for " + uavStatus.getName());
		statusInfo1.addStyleName("info_box_name");
		statusInfo1.addStyleName(ValoTheme.LABEL_BOLD);
		statusInfo2.setValue("Status: " + uavStatus.getStatus());

		statusInfo3.setValue("Battery Life: " + uavStatus.getBatteryLife() + " %");
		statusContent.addComponents(statusInfo1, statusInfo2, statusInfo3);
		statusContent.setSpacing(false);
		Label health = new Label();
		health.setCaptionAsHtml(true);
		health.setCaption("<span style=\'color: " + uavStatus.getHealthColor() + " !important;\'> "
				+ VaadinIcons.CIRCLE.getHtml() + "</span>");
		if (uavStatus.getHealthColor().equals("green"))
			health.setDescription("Normally Functionable");
		else if (uavStatus.getHealthColor().equals("yellow"))
			health.setDescription("Needs Attention");
		else if (uavStatus.getHealthColor().equals("red"))
			health.setDescription("Needs Immediate Attention");

		topContent.addComponents(droneImage, statusContent, health);
		topContent.setSpacing(false);

		VerticalLayout coordinates = new VerticalLayout();
		VerticalLayout altAndSpeed = new VerticalLayout();
		HorizontalLayout positionInfo = new HorizontalLayout();

		Label locationInfo1 = new Label();
		Label locationInfo2 = new Label();
		Label locationInfo3 = new Label();
		Label locationInfo4 = new Label();

		locationInfo1.setValue("Latitude:\t" + uavStatus.getLatitude());
		locationInfo2.setValue("Longitude:\t" + uavStatus.getLongitude());
		locationInfo3.setValue("Altitude:\t" + uavStatus.getAltitude() + " meters");

		locationInfo4.setValue("Ground Speed:\t" + uavStatus.getSpeed() + " m/s");

		coordinates.addComponents(locationInfo1, locationInfo2);
		altAndSpeed.addComponents(locationInfo3, locationInfo4);
		positionInfo.addComponents(coordinates, altAndSpeed);

		topContent.addComponent(positionInfo);

		VerticalLayout buttons = new VerticalLayout();

		HorizontalLayout bottomSwitch = new HorizontalLayout();
		Label caption = new Label("Hover in Place");
		bottomSwitch.addComponents(caption, hoverSwitch);

		buttons.addComponents(bottomSwitch, returnToHome);

		topContent.addComponent(buttons);

	}

	/**
	 * 
	 * @return in-order list of flight routes to be assigned to the UAV based on the order in the AFDragLayout
	 */
	public Collection<FlightRouteInfo> getRoutesToAssign() {
		Collection<FlightRouteInfo> current = new ArrayList<>();
		Collection<FlightRouteInfo> items = null;
		try {
			flightInfoService = (IFlightRouteplanningRemoteService) provider.getRemoteManager()
					.getService(IFlightRouteplanningRemoteService.class);
			items = flightInfoService.getItems();
		} catch (RemoteException | DronologyServiceException e) {
			MyUI.setConnected(false);
			LOGGER.error(e);
		}

		for (int i = 0; i < panelContent.getComponentCount(); i++) {
			FRInfoBox box = (FRInfoBox) panelContent.getComponent(i);
			for (FlightRouteInfo info : items) {
				if (box.getName().equals(info.getName())) {
					current.add(info);
				}
			}
		}

		return current;
	}

	public void addRoute(FlightRouteInfo routeInfo) {
		FRInfoBox box = new FRInfoBox(routeInfo);
		box.setId(Integer.toString(this.boxID));
		panelContent.addNewComponent(box);
		numRoutes += 1;
		sidePanel.setCaption(numRoutes + " Routes Assigned");
	}

	/**
	 * removes a route from the AFDragLayout
	 * 
	 * @param index
	 */
	public void removeRoute(int index) {
		panelContent.removeComponent(panelContent.getComponent(index));
		numRoutes -= 1;
		sidePanel.setCaption(numRoutes + " Routes Assigned");
	}

	public Button getCancel() {
		return cancel;
	} 

	public Button getApply() {
		return apply;
	}

	public Button getReturnToHome() {
		return returnToHome;
	}

	public Switch getHover() {
		return hoverSwitch;
	}

}
