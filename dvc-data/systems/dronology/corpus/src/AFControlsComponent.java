package edu.nd.dronology.ui.vaadin.activeflights;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;

/**
 * This is the control panel framework for the Activated Flights UI
 * 
 * @author Jinghui Cheng
 */
public class AFControlsComponent extends CustomComponent {
	private static final long serialVersionUID = 1L;
	private AFInfoPanel info = new AFInfoPanel();
	public AFControlsComponent() {
		this.setWidth("100%");
		addStyleName("controls_component");
		
		VerticalLayout content = new VerticalLayout();

		content.addComponent(info);
		setCompositionRoot(content);
	}
	
	public AFInfoPanel getPanel(){
		return info;
	}
	
}
