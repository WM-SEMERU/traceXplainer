package edu.nd.dronology.ui.vaadin.activeflights;

import org.vaadin.teemu.switchui.Switch;

import com.vaadin.server.Resource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import edu.nd.dronology.core.vehicle.DroneFlightStateManager.FlightMode;
import edu.nd.dronology.ui.vaadin.connector.DronologyActionExecutor;
import edu.nd.dronology.ui.vaadin.flightroutes.windows.TakeoffAltitudeWindow;
import edu.nd.dronology.ui.vaadin.utils.ImageProvider;
import edu.nd.dronology.ui.vaadin.utils.StyleConstants; 

/**
 * This is the box in the side panel that contains a UAV's information
 * 
 * @author Patrick Falvey
 *
 */

public class AFInfoBox extends CustomComponent {
	private static final long serialVersionUID = -8541147696474050819L;

	private boolean visible = false;
	private boolean isChecked;
	private boolean hoverInPlace;
	private boolean checkClicked = false;

	private CheckBox check = new CheckBox();
	private Label statusInfo1 = new Label();
	private Label statusInfo2 = new Label();
	private Label statusInfo3 = new Label();
	private Label health = new Label();
	private Label locationInfo1 = new Label();
	// private Label locationInfo2 = new Label();
	// private Label locationInfo3 = new Label();
	private Label locationInfo4 = new Label();
	private Switch hoverSwitch = new Switch();
	private Button returnToHome = new Button("");
	private Button takeoff = new Button("");

	private Resource takeoffIcon = ImageProvider.getTaekoffResource();
	private Resource rtlIcon = ImageProvider.getRTLResource();
	private Resource assignRouteIcon = ImageProvider.getAssignRouteResource();
	private Resource resendIcon = ImageProvider.getResendCommandResource();
	private Resource okIcon = ImageProvider.getStatusOKResource();
	private Resource user_controlled = ImageProvider.getStatusUsercontrolledResource();

	private Button assignNewRoute = new Button("");
	private Button resendCommand = new Button("");

	private VerticalLayout mainContent = new VerticalLayout();
	private HorizontalLayout topContent = new HorizontalLayout();
	private VerticalLayout middleContent = new VerticalLayout();
	private HorizontalLayout bottomContent = new HorizontalLayout();
	private UAVStatusWrapper uavStatus;

	public AFInfoBox(boolean isChecked, UAVStatusWrapper uavStatus, boolean hoverInPlace) {
		this.uavStatus = uavStatus;
		this.isChecked = isChecked;
		this.hoverInPlace = hoverInPlace;
	}

	public void createContents() {
		this.addStyleName(StyleConstants.INFO_BOX);
		this.addStyleName(StyleConstants.AF_INFO_BOX);

		topContent.addStyleName(StyleConstants.AF_BOX_INFO_TOP_CONTENT);
		middleContent.addStyleName(StyleConstants.AF_BOX_INFO_DETAIL_CONTENT);
		bottomContent.addStyleName(StyleConstants.AF_BOX_INFO_BOTTOM_CONTENT);

		createTopComponent();

		middleContent.addComponents(locationInfo1, locationInfo4);

		createBottomComponent();

		mainContent.addComponents(topContent, middleContent, bottomContent);
		mainContent.setSizeUndefined();
		mainContent.setSpacing(false);

		middleContent.setVisible(visible);
		bottomContent.setVisible(visible);

		setCompositionRoot(mainContent);

		returnToHome.setHeight("30px");
		assignNewRoute.setHeight("30px");
		takeoff.setHeight("30px");
		resendCommand.setHeight("30px");

		takeoff.setWidth("55px"); 
		returnToHome.setWidth("55px");
		assignNewRoute.setWidth("55px");
		resendCommand.setWidth("55px");

		takeoff.setDescription("Take-off to target altitude");
		resendCommand.setDescription("Resend previous command");
		returnToHome.setDescription("Cancel all routes and return to home imediatelly");
		assignNewRoute.setDescription("Assign a new flight route");

		takeoff.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
		
		takeoff.setIcon(takeoffIcon);
		returnToHome.setIcon(rtlIcon);
		assignNewRoute.setIcon(assignRouteIcon);
		resendCommand.setIcon(resendIcon);
		addListener();
		update(uavStatus);
	}

	private void createBottomComponent() {
		GridLayout bottomButtons = new GridLayout(2, 2);
		bottomButtons.setWidth("150px");
		bottomButtons.setHeight("70px");
		VerticalLayout bottomSwitch = new VerticalLayout();

		hoverSwitch.setValue(this.hoverInPlace);

		Label caption = new Label("Hover in Place");
		bottomSwitch.addComponents(caption, hoverSwitch);

		bottomButtons.addComponents(takeoff, returnToHome, assignNewRoute, resendCommand);
		bottomContent.addComponents(bottomSwitch, bottomButtons);

	}

	private void createTopComponent() {
		VerticalLayout statusContent = new VerticalLayout();
		check.setValue(this.isChecked);
		Image droneImage = ImageProvider.getDefaultUAVImage();

		statusInfo1.addStyleName("info_box_name");
		statusInfo1.addStyleName(ValoTheme.LABEL_BOLD);

		statusContent.addComponents(statusInfo1, statusInfo2, statusInfo3);
		statusContent.setSpacing(false);
		// health.setCaptionAsHtml(true);
		health.setIcon(okIcon);

		topContent.addComponents(check, droneImage, statusContent, health);
		topContent.setSpacing(false);
		topContent.setComponentAlignment(check, Alignment.TOP_LEFT);
		topContent.setComponentAlignment(droneImage, Alignment.TOP_LEFT);
		topContent.setComponentAlignment(statusContent, Alignment.TOP_LEFT);
		topContent.setComponentAlignment(health, Alignment.TOP_RIGHT);

	}

	private void addListener() {
		returnToHome.addClickListener(e -> {
			DronologyActionExecutor.returnHome(null, uavStatus);
		});

		takeoff.addClickListener(e -> {
			this.takeOff();
		});

		assignNewRoute.addClickListener(assignEvent -> { // this opens the assign routes UI
			DronologyActionExecutor.assignRouteToUAV(uavStatus);
		});
		
		resendCommand.addClickListener(assignEvent -> { // this opens the assign routes UI
			DronologyActionExecutor.resendCommand(uavStatus);
		});

		hoverSwitch.addValueChangeListener(e -> {
			this.setHoverInPlace(this.hoverSwitch.getValue());
		});

		// this listener assists with whether a box is focused or only checked
		topContent.addLayoutClickListener(e -> {
			Component child = e.getChildComponent();
			if (child != null && child.getClass() == CheckBox.class) {
				checkClicked = true;
			} else {
				checkClicked = false;
			}
		});

		topContent.addLayoutClickListener(e -> {
			Component child = e.getChildComponent();
			if (child == null || !child.getClass().getCanonicalName().equals("com.vaadin.ui.CheckBox")) {
				setBoxVisible(visible);
			}
		});

	}

	private void takeOff() {
		if (!this.uavStatus.getStatus().equals("ON_GROUND")) {
			Notification.show(uavStatus.getName() + " is already in the air.");
			return;
		}
		TakeoffAltitudeWindow takeoffAltitudeWindow = new TakeoffAltitudeWindow(uavStatus.getName());

		UI.getCurrent().addWindow(takeoffAltitudeWindow);
	}

	/**
	 * default constructor
	 */
	public AFInfoBox() {
		// this(false, "NAME/ID of UAV", "Status", 0, "green", 0, 0, 0, 0, false);
	}

	public void setIsChecked(boolean isChecked) {
		this.isChecked = isChecked;
		check.setValue(this.isChecked);
	}

	public boolean getIsChecked() {
		this.isChecked = check.getValue();
		return this.isChecked;
	}

	public CheckBox getCheckBox() {
		return this.check;
	}

	public void setHoverInPlace(boolean hoverInPlace) {
		this.hoverInPlace = hoverInPlace;
		hoverSwitch.setValue(this.hoverInPlace);
		// if (this.hoverInPlace) {
		// this.status = "Hovering";
		// statusInfo2.setValue("Status: ");
		// } else {
		// this.status = "";
		// statusInfo2.setValue("Status: ");
		// }
	}

	public boolean getHoverInPlace() {
		return this.hoverInPlace;
	}

	public Switch getHoverSwitch() {
		return this.hoverSwitch;
	}

	/**
	 * Expands and collapses the box
	 * 
	 * @param visible
	 */
	public void setBoxVisible(boolean visible) {
		if (visible) {
			this.visible = false;
			middleContent.setVisible(this.visible);
			bottomContent.setVisible(this.visible);
		} else {
			this.visible = true;
			middleContent.setVisible(this.visible);
			bottomContent.setVisible(this.visible);
		}
	}

	public boolean getBoxVisible() {
		return this.visible;
	}

	public Button getHomeButton() {
		return this.returnToHome;
	}

	public Button getRouteButton() {
		return this.assignNewRoute;
	}

	public boolean getCheckClick() {
		return checkClicked;
	}

	public void setCheckClick(boolean checkClicked) {
		this.checkClicked = checkClicked;
	}

	public void update(UAVStatusWrapper uavStatus) {
		this.uavStatus = uavStatus;
		statusInfo1.setValue(uavStatus.getName());
		statusInfo2.setValue("Status: " + uavStatus.getStatus());
		statusInfo3.setValue("Battery Life: " + uavStatus.getBatteryLife() + " %");

		String locString = String.format("Lat: %s | Long: %s | Alt: %s", uavStatus.getLatitude(),
				uavStatus.getLongitude(), uavStatus.getAltitude());

		locationInfo1.setValue(locString);
		// locationInfo2.setValue("Longitude:\t" + uavStatus.getLongitude());
		// locationInfo3.setValue("Altitude:\t" + uavStatus.getAltitude() + " meters");
		locationInfo4.setValue("Ground Speed:\t" + uavStatus.getSpeed() + " m/s");

		// health.setCaption("<span style=\'color: " + uavStatus.getHealthColor() + "
		// !important;\'> "
		// + VaadinIcons.CIRCLE.getHtml() + "</span>");
		// if (uavStatus.getHealthColor().equals("green"))
		// health.setDescription("Normally Functionable");
		// else if (uavStatus.getHealthColor().equals("yellow"))
		// health.setDescription("Needs Attention");
		// else if (uavStatus.getHealthColor().equals("red"))
		// health.setDescription("Needs Immediate Attention");

		if (uavStatus.getStatus().equals(FlightMode.USER_CONTROLLED.toString())) {
			health.setIcon(user_controlled);
			this.setEnabled(false);
			setBoxVisible(true);
		} else {
			health.setIcon(okIcon);
			this.setEnabled(true);
		}

	}

	public String getName() {
		return uavStatus.getName();
	}

	public String getStatus() {
		return uavStatus.getStatus();
	}

	public String getHealthColor() {
		return uavStatus.getHealthColor();
	}
}
