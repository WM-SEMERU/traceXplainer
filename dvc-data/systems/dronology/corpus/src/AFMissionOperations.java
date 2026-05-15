package edu.nd.dronology.ui.vaadin.activeflights;

import java.rmi.RemoteException;
import java.util.Map;

import com.vaadin.event.ContextClickEvent;
import com.vaadin.event.ContextClickEvent.ContextClickListener;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import edu.nd.dronology.services.core.info.MissionInfo;
import edu.nd.dronology.services.core.remote.IMissionPlanningRemoteService;
import edu.nd.dronology.services.core.util.DronologyServiceException;
import edu.nd.dronology.ui.vaadin.missionplanning.MPEditMissionHelpWindow;
import edu.nd.dronology.ui.vaadin.missionplanning.MPEditMissionWindow;
import edu.nd.dronology.ui.vaadin.missionplanning.MPInfoPanel;
import edu.nd.dronology.ui.vaadin.missionplanning.MPMapComponent;
import edu.nd.dronology.ui.vaadin.start.MyUI;

/**
 * This is the set of map operation buttons in the AFInfoPanel
 * 
 * @author Patrick Falvey
 *
 */

public class AFMissionOperations extends CustomComponent {

	private static final long serialVersionUID = -2249802562670339842L;
	private HorizontalLayout buttons = new HorizontalLayout();
	private Upload upload;
	private Button cancelMission = new Button("Cancel Mission");
	private Button executeMission=new Button("Execute Mission");
	//private AFChooseMissionWindow chooseMissionWindow;

	public AFMissionOperations(AFInfoPanel infoPanel) {
		VerticalLayout layout = new VerticalLayout();
		layout.addStyleName("af_mission_group");

		Label caption = new Label("Mission Operations");

	//	MissionHandler handler = new MissionHandler();
	//	upload = new Upload(null, handler.getReceiver());
	//	upload.setVisible(true);
	//	upload.addSucceededListener(handler.getReceiver());
	//	upload.setImmediateMode(true);
	//	upload.setButtonCaption("Execute Mission");
		cancelMission.addClickListener(e -> {
			cancelMisison();

		});
		executeMission.addClickListener( e ->{
			UI.getCurrent().addWindow(new AFChooseMissionWindow(infoPanel));
		});
		
	
		
	//	ContextClickListener myListener = new MyListener();
	//	upload.addContextClickListener(myListener);

		buttons.addComponents(executeMission, cancelMission);   //upload
		layout.addComponents(caption, buttons);

		setCompositionRoot(layout);
	}

	private void cancelMisison() {
		try {
			IMissionPlanningRemoteService service = (IMissionPlanningRemoteService) MyUI.getProvider()
					.getRemoteManager().getService(IMissionPlanningRemoteService.class);
			service.cancelMission();
		} catch (RemoteException | DronologyServiceException e) {
			MyUI.setConnected(false);
			e.printStackTrace();
		}

	}

	public void addOnClickListener(LayoutClickListener listener) {
		buttons.addLayoutClickListener(listener);
	}

	
//	private class MyListener implements ContextClickListener {

		/**
		 * 
		 */
	//	private static final long serialVersionUID = -1892418708777796487L;

	//	@Override
	//	public void contextClick(ContextClickEvent event) {
			// upload
	//		System.out.println("CLICK!");
	//		upload.submitUpload();
	//	}
	//}

}
