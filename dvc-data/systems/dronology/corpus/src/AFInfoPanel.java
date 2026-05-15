package edu.nd.dronology.ui.vaadin.activeflights;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import edu.nd.dronology.core.vehicle.IUAVProxy;
import edu.nd.dronology.services.core.remote.IDroneSetupRemoteService;
import edu.nd.dronology.services.core.remote.IFlightManagerRemoteService;
import edu.nd.dronology.services.core.util.DronologyServiceException;
import edu.nd.dronology.ui.vaadin.connector.BaseServiceProvider;
import edu.nd.dronology.ui.vaadin.start.MyUI;
import edu.nd.dronology.ui.vaadin.utils.StyleConstants;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

/**
 * This is the side panel that contains the AFInfoBoxes with the UAV information
 * 
 * @author Patrick Falvey
 *
 */

public class AFInfoPanel extends CustomComponent {
	private static final long serialVersionUID = -3663049148276256302L;
	private Panel panel = new Panel();
	private Button selectButton = new Button("Select all");
	private Button visibleButton = new Button("Expand all");
	private VerticalLayout content = new VerticalLayout();
	private int numUAVs = 0;
	private boolean selectAll = true;
	private boolean visible = false;
	private String focused = "";
	private AFMapViewOperations mapView = new AFMapViewOperations();
	private Collection<IUAVProxy> drones;
	private IDroneSetupRemoteService service;
	private BaseServiceProvider provider = MyUI.getProvider();
	private AFMissionOperations missionView = new AFMissionOperations(this);
	private static final ILogger LOGGER = LoggerProvider.getLogger(AFInfoPanel.class);

	public AFInfoPanel() { 

		panel.setCaption(Integer.toString(numUAVs) + " Active UAVs");
		panel.setContent(content);
		panel.addStyleName(StyleConstants.AF_INFO_PANEL);
		panel.addStyleName(StyleConstants.CONTROL_PANEL);

		HorizontalLayout buttons = new HorizontalLayout();
		VerticalLayout sideBar = new VerticalLayout();

		AFEmergencyComponent emergency = new AFEmergencyComponent();

		emergency.getHome().addClickListener(e -> { // sends all UAVs (or all checked UAVs) to their homes
			List<String> checked = this.getChecked();
			String message = "";
			boolean sendHome = true;
			if (checked.size() > 0) {
				if (checked.size() == 1) {
					for (int i = 1; i < numUAVs + 1; i++) {
						AFInfoBox box = (AFInfoBox) content.getComponent(i);
						if (box.getName().equals(checked.get(0))) {
							if (box.getStatus().equals("ON_GROUND")) {
								Notification.show(checked.get(0) + " is already home.");
								sendHome = false;
							} else {
								message = "Are you sure you want to send " + checked.get(0) + " to its home?";
							}
						}
					}

				} else {
					String drones = "";
					for (int i = 0; i < checked.size() - 1; i++) {
						drones += checked.get(i) + ", ";
					}
					message = "Are you sure you want to send " + drones + "and " + checked.get(checked.size() - 1)
							+ " to their homes?";
				}
			} else {
				message = "Are you sure to send all UAVs to their homes?";
			}
			Window confirm = new Window("Confirm");
			confirm.addStyleName("confirm_window");
			VerticalLayout subContent = new VerticalLayout();
			HorizontalLayout subButtons = new HorizontalLayout();
			subButtons.addStyleName("confirm_button_area");
			Label label = new Label(message);
			Button yes = new Button("Yes");
			yes.addStyleName("btn-danger");
			Button no = new Button("No");

			yes.addClickListener(subEvent -> {
				UI.getCurrent().removeWindow(confirm);
				IFlightManagerRemoteService service;
				try {
					service = (IFlightManagerRemoteService) provider.getRemoteManager()
							.getService(IFlightManagerRemoteService.class);
					if (checked.size() > 0) {
						for (int i = 0; i < checked.size(); i++) {
							for (int j = 1; j < numUAVs + 1; j++) {
								AFInfoBox box = (AFInfoBox) content.getComponent(j);
								if (box.getName().equals(checked.get(i))) {
									if (!box.getStatus().equals("ON_GROUND")) {
										service.returnToHome(checked.get(i));
									}
								}
							}
						}
					} else {
						for (int i = 1; i < numUAVs + 1; i++) {
							AFInfoBox box = (AFInfoBox) content.getComponent(i);
							if (!box.getStatus().equals("ON_GROUND"))
								service.returnToHome(box.getName());
						}
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}

			});

			no.addClickListener(subEvent -> {
				UI.getCurrent().removeWindow(confirm);
			});

			subButtons.addComponents(yes, no);
			subContent.addComponents(label, subButtons);
			confirm.setContent(subContent);
			confirm.setModal(true);
			confirm.setResizable(false);
			confirm.center();
			if (sendHome)
				UI.getCurrent().addWindow(confirm);

		});

		content.addLayoutClickListener(e -> { // determines if a box should be in focus
			Component testChild = e.getChildComponent();
			if (testChild.getClass() == AFInfoBox.class) {
				AFInfoBox child = (AFInfoBox) e.getChildComponent();
				if (!child.getCheckClick()) { // if the box was clicked but not the checkbox
					child.addStyleName("info_box_focus");
					child.setIsChecked(true);
					focused = child.getName();
					for (int i = 1; i < numUAVs + 1; i++) {
						AFInfoBox box = (AFInfoBox) content.getComponent(i);
						if (!box.getName().equals(child.getName())) {
							box.removeStyleName("info_box_focus");
							box.setIsChecked(false);
							box.setCheckClick(false);
						}
					}
				} else {
					child.removeStyleName("info_box_focus");
					if (focused.equals(child.getName()))
						focused = "";
				}
			}
		});

		sideBar.addComponents(panel, mapView, missionView, emergency);
		setCompositionRoot(sideBar);

		selectButton.addStyleName(ValoTheme.BUTTON_LINK);
		selectButton.addStyleName(StyleConstants.SMALL_BUTTON_LINK);
		visibleButton.addStyleName(ValoTheme.BUTTON_LINK);
		visibleButton.addStyleName(StyleConstants.SMALL_BUTTON_LINK);

		buttons.addComponents(selectButton, visibleButton);
		buttons.addStyleName("af_uav_list_controls");

		selectButton.addClickListener(e -> {
			if (selectAll) {
				selectAll(true);
				selectButton.setCaption("Deselect all");
				selectAll = false;
			} else {
				selectAll(false);
				selectButton.setCaption("Select all");
				selectAll = true;
			}
		});

		visibleButton.addClickListener(e -> {
			if (visible) {
				visible = false;
				setVisibility(true);
				visibleButton.setCaption("Expand all");
			} else {
				visible = true;
				setVisibility(false);
				visibleButton.setCaption("Collapse all");
			}
		});

		content.addComponent(buttons);
		numUAVs = content.getComponentCount() - 1;

		try {
			service = (IDroneSetupRemoteService) provider.getRemoteManager().getService(IDroneSetupRemoteService.class);
			Collection<IUAVProxy> activeDrones = service.getActiveUAVs();
			drones = new ArrayList<>(activeDrones);
			// Collections.sort(drones);
			for (IUAVProxy e : drones) {
				addBox(false, new UAVStatusWrapper(e), false);
			}
		} catch (DronologyServiceException | RemoteException e1) {
			MyUI.setConnected(false);
			LOGGER.error(e1);
		}

	}

	public AFMapViewOperations getMapView() {
		return mapView;
	}

	public String getFocusedName() {
		return focused;
	}

	/**
	 * Adds a box to the panel
	 * 
	 * @param isChecked
	 * @param name
	 * @param status
	 * @param batteryLife
	 * @param healthColor
	 * @param lat
	 * @param lon
	 * @param alt
	 * @param speed
	 * @param hoverInPlace
	 */
	public void addBox(boolean isChecked, UAVStatusWrapper uavStatus, boolean hoverInPlace) {
		AFInfoBox box = new AFInfoBox(isChecked, uavStatus, hoverInPlace);
		box.createContents();
		content.addComponent(box);
		numUAVs = content.getComponentCount() - 1;
		panel.setCaption(Integer.toString(numUAVs) + " Active UAVs");
	}

	public void addBox() {
		AFInfoBox box = new AFInfoBox();
		content.addComponent(box);
		numUAVs = content.getComponentCount() - 1;
		panel.setCaption(Integer.toString(numUAVs) + " Active UAVs");
	}

	/**
	 * Removes a box from the panel
	 * 
	 * @param name
	 *          the name/ID of the drone
	 * @return returns true if successful. returns false if failed
	 */
	public boolean removeBox(String name) {
		for (int i = 1; i < numUAVs + 1; i++) {
			AFInfoBox box = (AFInfoBox) content.getComponent(i);
			if (box.getName().equals(name)) {
				content.removeComponent(box);
				numUAVs = content.getComponentCount() - 1;
				panel.setCaption(Integer.toString(numUAVs) + " Active UAVs");
				return true;
			}
		}
		return false;
	}

	public void selectAll(boolean select) {
		for (int i = 1; i < numUAVs + 1; i++) {
			AFInfoBox box = (AFInfoBox) content.getComponent(i);
			box.setIsChecked(select);
			if (!select && focused.equals(box.getName())) {
				box.removeStyleName("info_box_focus");
				box.setCheckClick(false);
				focused = "";
			}
		}
	}

	/**
	 * 
	 * @return a list of all drones that have their checkbox checked
	 */
	public List<String> getChecked() {
		List<String> names = new ArrayList<>();
		for (int i = 1; i < numUAVs + 1; i++) {
			AFInfoBox box = (AFInfoBox) content.getComponent(i);
			if (box.getIsChecked())
				names.add(box.getName());
		}
		return names;
	}

	/**
	 * 
	 * @return true if all the drones are checked
	 */
	private boolean getAllChecked() {
		boolean checked = true;
		for (int i = 1; i < numUAVs + 1; i++) {
			AFInfoBox box = (AFInfoBox) content.getComponent(i);
			if (!box.getIsChecked())
				checked = false;
		}
		return checked;
	}

	/**
	 * 
	 * @return true if all drones are not checked
	 */
	private boolean getAllNotChecked() {
		boolean notChecked = true;
		for (int i = 1; i < numUAVs + 1; i++) {
			AFInfoBox box = (AFInfoBox) content.getComponent(i);
			if (box.getIsChecked())
				notChecked = false;
		}
		return notChecked;
	}

	/**
	 * Expands or collapses all the boxes
	 * 
	 * @param visible
	 */
	public void setVisibility(boolean visible) {
		for (int i = 1; i < numUAVs + 1; i++) {
			AFInfoBox box = (AFInfoBox) content.getComponent(i);
			box.setBoxVisible(visible);
		}
	}

	/**
	 * 
	 * @return true if all boxes are expanded
	 */
	private boolean getAllVisible() {
		boolean visible = true;
		for (int i = 1; i < numUAVs + 1; i++) {
			AFInfoBox box = (AFInfoBox) content.getComponent(i);
			if (!box.getBoxVisible())
				visible = false;
		}
		return visible;
	}

	/**
	 * 
	 * @return true if all boxes are collapsed
	 */
	private boolean getAllNotVisible() {
		boolean notVisible = true;
		for (int i = 1; i < numUAVs + 1; i++) {
			AFInfoBox box = (AFInfoBox) content.getComponent(i);
			if (box.getBoxVisible())
				notVisible = false;
		}
		return notVisible;
	}

	public void setAllToHover() {
		for (int i = 1; i < numUAVs + 1; i++) {
			AFInfoBox box = (AFInfoBox) content.getComponent(i);
			box.setHoverInPlace(true);
		}
	}

	public VerticalLayout getBoxes() {
		return content;
	}

	public int getNumUAVS() {
		return numUAVs;
	}

	/**
	 * gets updated information from dronology about the UAV's location information and status. adds any new drones to the panel and removes any drones that were deactivated
	 */
	@SuppressWarnings("deprecation") 
	public void refreshDrones() {
		// update select/deselect all button
		if (this.getAllChecked() && selectButton.getCaption().equals("Select all") && numUAVs != 0) {
			selectButton.setCaption("Deselect all");
			selectAll = false;
		} else if (this.getAllNotChecked() && selectButton.getCaption().equals("Deselect all") && numUAVs != 0) {
			selectButton.setCaption("Select all");
			selectAll = true;
		}
		// update expand/collapse all button
		if (this.getAllVisible() && visibleButton.getCaption().equals("Expand all") && numUAVs != 0) {
			visibleButton.setCaption("Collapse all");
			visible = true;
		} else if (this.getAllNotVisible() && visibleButton.getCaption().equals("Collapse all") && numUAVs != 0) {
			visibleButton.setCaption("Expand all");
			visible = false;
		}
		try {

			Collection<IUAVProxy> newDrones = service.getActiveUAVs();
			/**
			 * add new drones to the panel
			 */
			if (newDrones.size() > drones.size()) {
				for (IUAVProxy e1 : newDrones) {
					boolean nameMatch = false;
					for (IUAVProxy e2 : drones) {
						if (e1.getID().equals(e2.getID())) {
							nameMatch = true;
						}
					}
					if (!nameMatch) {
						this.addBox(false, new UAVStatusWrapper(e1), false);
					}
				}
			}
			/**
			 * delete old drones from the panel
			 */
			if (newDrones.size() < drones.size()) {
				for (IUAVProxy old : drones) {
					boolean exists = false;
					for (IUAVProxy current : newDrones) {
						if (old.getID().equals(current.getID()))
							exists = true;
					}
					if (!exists) {
						for (int i = 1; i < numUAVs + 1; i++) {
							AFInfoBox box = (AFInfoBox) content.getComponent(i);
							if (old.getID().equals(box.getName()))
								this.removeBox(box.getName());
						}
					}
				}
			}
		} catch (RemoteException e1) {
			try {
				Notification.show("Reconnecting...");
				service = (IDroneSetupRemoteService) provider.getRemoteManager().getService(IDroneSetupRemoteService.class);
			} catch (RemoteException | DronologyServiceException e) {
				MyUI.setConnected(false);
				Notification.show("Reconnecting...");
			}
			Notification.show("Reconnecting...");
			content.removeAllComponents();
			numUAVs = 0;
		}
		/**
		 * update current drones' status
		 */
		try {
			drones = service.getActiveUAVs();
			for (IUAVProxy e : drones) {
				for (int i = 1; i < numUAVs + 1; i++) {
					AFInfoBox box = (AFInfoBox) content.getComponent(i);
					if (e.getID().equals(box.getName())) {
						box.update(new UAVStatusWrapper(e));

					}
				}
			}
		} catch (RemoteException e1) {
			try {
				Notification.show("Reconnecting...");
				service = (IDroneSetupRemoteService) provider.getRemoteManager().getService(IDroneSetupRemoteService.class);
			} catch (RemoteException | DronologyServiceException e) {
				Notification.show("Reconnecting...");
			}
			Notification.show("Reconnecting...");
			content.removeAllComponents();
			numUAVs = 0;
		}
	}

	public void createContents() {
		// TODO Auto-generated method stub

	}

}
