package edu.nd.dronology.ui.vaadin.activeflights;

import java.util.List;

import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;

/**
 * This is the main layout for the Active Flights UI
 * 
 * @author Jinghui Cheng
 */
public class AFMainLayout extends CustomComponent {
	private static final long serialVersionUID = 1L;
	private AFControlsComponent controls = new AFControlsComponent();
	private AFMapComponent map = new AFMapComponent(controls.getPanel());
 
	public AFMainLayout() {
		addStyleName("main_layout");

		CssLayout content = new CssLayout();
		content.setSizeFull();

		controls.getPanel().getMapView().getViewButton().addClickListener(e -> {
			map.setFollow(false);
			map.setAverageCenter();
		});



		controls.getPanel().getMapView().getFollowButton().addClickListener(e -> {
			map.setFollow(true);
			map.setFollowZoom(true);
			List<String> names = controls.getPanel().getChecked();
			map.followDrones(names);
		});

		content.addComponents(controls, map);
		setCompositionRoot(content);
	}

	/**
	 * determines if the map should continue in follow mode
	 */
	public void continueFollowing() {
		if (map.getFollow()) {
			List<String> names = controls.getPanel().getChecked();
			map.followDrones(names);
		}
	}

	public AFControlsComponent getControls() {
		return controls;
	}

	public AFMapComponent getAFMap() {
		return map;
	}
}
