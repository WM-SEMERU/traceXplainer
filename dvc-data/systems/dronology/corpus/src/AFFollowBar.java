package edu.nd.dronology.ui.vaadin.activeflights;

import java.util.List;

import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * This is the bar that indicates when the active flights UI is in follow mode
 * 
 * @author Patrick Falvey
 * 
 */
public class AFFollowBar extends CustomComponent{

	private static final long serialVersionUID = 2389713576038720628L;
	private HorizontalLayout totalLayout = new HorizontalLayout();
	private Button stopFollowingButton = new Button("Stop Following");
	 
	private Label textLabel = new Label("Following UAV(s): ");
	private Label smallText = new Label();
	
	private String UAVNames;
	 
	public AFFollowBar(AFMapComponent map, List<String> uavs) {
		UAVNames = "";
		for (int i = 0; i < uavs.size() - 1; i++){
			UAVNames = UAVNames + uavs.get(i) + ", ";
		}
		UAVNames = UAVNames + uavs.get(uavs.size() - 1);
		smallText.setValue(UAVNames);
		setStyleName("af_follow_bar");
		textLabel.setStyleName("large_text");
		smallText.setStyleName("small_text");
		
		stopFollowingButton.setHeight("25px");
		totalLayout.addComponents(textLabel, smallText, stopFollowingButton);
		setCompositionRoot(totalLayout);
		
		stopFollowingButton.addClickListener(e->{
			map.setAverageCenter();
			map.setFollow(false);
		});

	}
	
	public Button getStopFollowingButton(){
		return stopFollowingButton;
	}
	
	public void updateUAVList(List<String> uavs){
		UAVNames = "";
		for (int i = 0; i < uavs.size() - 1; i++){
			UAVNames = UAVNames + uavs.get(i) + ", ";
		}
		UAVNames = UAVNames + uavs.get(uavs.size() - 1);
		smallText.setValue(UAVNames);
	}
	
	
	
}