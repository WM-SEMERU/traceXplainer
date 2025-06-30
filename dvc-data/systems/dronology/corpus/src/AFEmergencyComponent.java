package edu.nd.dronology.ui.vaadin.activeflights;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.VerticalLayout;

/**
 * This is the set of emergency buttons in the AFInfoPanel
 * 
 * @author Patrick Falvey 
 *
 */
public class AFEmergencyComponent extends CustomComponent{
	private static final long serialVersionUID = -650745296345774988L;
	private HorizontalLayout buttons = new HorizontalLayout();
	private NativeButton hover = new NativeButton("All UAVs<br>Hover in Place");
  private NativeButton home = new NativeButton("All UAVs<br>Return to Home");
  
	public AFEmergencyComponent(){
		VerticalLayout layout = new VerticalLayout();
		layout.addStyleName("af_operations_group");
    
		Label caption = new Label("Emergency Operations");
    
    hover.setCaptionAsHtml(true);
    hover.addStyleName("btn-warning");
    home.setCaptionAsHtml(true);
    home.addStyleName("btn-warning");
    
    buttons.addComponents(hover, home);
    layout.addComponents(caption, buttons);
    
    setCompositionRoot(layout);
	}
	
	public NativeButton getHome(){
		return home;
	}
	
	public NativeButton getHover(){
		return hover;
	}
	
}
