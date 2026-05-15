package edu.nd.dronology.ui.vaadin.activeflights;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.DropTarget;
import com.vaadin.event.dd.TargetDetails;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.Not;
import com.vaadin.event.dd.acceptcriteria.SourceIsTarget;
import com.vaadin.shared.ui.dd.HorizontalDropLocation;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.VerticalLayout;

import edu.nd.dronology.services.core.info.FlightInfo;
import edu.nd.dronology.services.core.info.FlightPlanInfo;
import edu.nd.dronology.services.core.info.FlightRouteInfo;
import edu.nd.dronology.services.core.remote.IFlightManagerRemoteService;
import edu.nd.dronology.services.core.remote.IFlightRouteplanningRemoteService;
import edu.nd.dronology.services.core.util.DronologyServiceException;
import edu.nd.dronology.ui.vaadin.connector.BaseServiceProvider;
import edu.nd.dronology.ui.vaadin.flightroutes.FRInfoBox;
import edu.nd.dronology.ui.vaadin.start.MyUI;

/**
 * This is the drag and drop vertical layout in which the FRInfoBoxes are stored 
 * in the assign routes UI.  
 * 
 * @author Patrick Falvey 
 *
 */

public class AFDragLayout extends VerticalLayout {

	/**
	* 
	*/
	private static final long serialVersionUID = -978484208144577037L;
	private SortableLayout layout = new SortableLayout();
	private IFlightManagerRemoteService flightRouteService;
	private IFlightRouteplanningRemoteService flightInfoService;
	private BaseServiceProvider provider = MyUI.getProvider();
	private String UAVid;
	private int boxID = 88888; //arbitrary value so the component ID does not overlap boxIDs from AFAssignRouteComponent

	public AFDragLayout(String UAVid) {	
		this.UAVid = UAVid;
		layout.setSizeUndefined();
		for (Component component : createComponents()) {
			layout.addNewComponent(component);
		}

		addComponent(layout);
	}

	/**
	 * 
	 * @return list of pending plans for the UAV
	 */
	private List<Component> createComponents() {
		FlightInfo flightRouteInfo = null;
		Collection<FlightRouteInfo> items = null;
		try {
			flightRouteService = (IFlightManagerRemoteService) provider.getRemoteManager()
					.getService(IFlightManagerRemoteService.class);
			flightInfoService = (IFlightRouteplanningRemoteService) provider.getRemoteManager()
					.getService(IFlightRouteplanningRemoteService.class);
			flightRouteInfo = flightRouteService.getFlightInfo(UAVid);
			items = flightInfoService.getItems();
		} catch (RemoteException | DronologyServiceException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		List<Component> components = new ArrayList<>();
		
		for (FlightPlanInfo flight : flightRouteInfo.getPendingFlights()){ //convert plans into the FRInfoBoxes
			for (FlightRouteInfo info : items){
				String flightName = flight.getName().length() < info.getName().length() ? flight.getName() : 
					flight.getName().substring(flight.getName().length() - info.getName().length());
				if(flightName.equals(info.getName())){
					FRInfoBox box = new FRInfoBox(info);
					box.setId(Integer.toString(this.boxID));
					this.boxID++;
					components.add(box);
				}
			}
		}
		return components;
	}
	
	public SortableLayout getSortableLayout(){
		return this.layout;
	}
	
	public void addNewComponent(Component component){
		layout.addNewComponent(component);
	}
	
	@Override
	public void removeComponent(Component component){
		layout.removeComponent(component);
	}
	
	@Override
	public Component getComponent(int index){
		return layout.getComponent(index);
	}
	
	@Override
	public int getComponentCount(){
		return layout.getComponentCount();
	}
	
	@Override
	public int getComponentIndex(Component component){
		return layout.getComponentIndex(component);
	}
	public static class SortableLayout extends CustomComponent {
		/**
		* 
		*/
		private static final long serialVersionUID = 2763521051649448355L;
		private final AbstractOrderedLayout layout;
		private final DropHandler dropHandler;

		public SortableLayout() {
			layout = new VerticalLayout();
			dropHandler = new ReorderLayoutDropHandler(layout);

			DragAndDropWrapper pane = new DragAndDropWrapper(layout);
			setCompositionRoot(pane);
		}
		
		public VerticalLayout getVerticalLayout(){
			return (VerticalLayout) layout;
		}
		
		@Override
		public int getComponentCount(){
			return layout.getComponentCount();
		}
		
		public int getComponentIndex(Component component) {
			@SuppressWarnings("deprecation")
			Iterator<Component> componentIterator = layout.getComponentIterator();
			WrappedComponent next = null;
			int index = 0;
			while (componentIterator.hasNext()) { //important to compare with route name and component ID because some routes have the same name
				next = (WrappedComponent) componentIterator.next();
				if (((FRInfoBox) next.getContent()).getName().equals(((FRInfoBox) component).getName()) && component.getId().equals(next.getContent().getId())){
					return index;
				}
				else {
					index++;
				}
			}
			return -1;
		}

		public Component getComponent(int index) {
			WrappedComponent wrapper = (WrappedComponent) layout.getComponent(index);
			return wrapper.getContent();
		}
		
		public void addNewComponent(Component component) {
			WrappedComponent wrapper = new WrappedComponent(component, dropHandler);
			wrapper.setSizeUndefined();
			component.setWidth("100%");
			wrapper.setWidth("100%");
			layout.addComponent(wrapper);
		}
		
		public void removeComponent(Component component) {
			@SuppressWarnings("deprecation")
			Iterator<Component> componentIterator = layout.getComponentIterator();
			WrappedComponent next = null;
			boolean cont = true;
			while (cont && componentIterator.hasNext()) {
				next = (WrappedComponent) componentIterator.next();
				if (((FRInfoBox) next.getContent()).getName().equals(((FRInfoBox) component).getName()) && component.getId().equals(next.getContent().getId())){
					layout.removeComponent(next);
					cont = false;
				}
			}
		}
	}

	public static class WrappedComponent extends DragAndDropWrapper {

		/**
		* 
		*/
		private static final long serialVersionUID = -6051699334166210253L;
		private final DropHandler dropHandler;
		private Component content;

		public WrappedComponent(Component content, DropHandler dropHandler) {
			super(content);
			this.content = content;
			this.dropHandler = dropHandler;
			setDragStartMode(DragStartMode.WRAPPER);
		}

		@Override
		public DropHandler getDropHandler() {
			return dropHandler;
		}
		
		public Component getContent(){
			return this.content;
		}

	}

	private static class ReorderLayoutDropHandler implements DropHandler {

		/**
		* 
		*/
		private static final long serialVersionUID = 8500739235515201928L;
		private AbstractOrderedLayout layout;

		public ReorderLayoutDropHandler(AbstractOrderedLayout layout) {
			this.layout = layout;
		}

		@Override
		public AcceptCriterion getAcceptCriterion() {
			return new Not(SourceIsTarget.get());
		}

		@Override
		public void drop(DragAndDropEvent dropEvent) {  //logic to determine the position of the drag and dropped object
			Transferable transferable = dropEvent.getTransferable();
			Component sourceComponent = transferable.getSourceComponent();
			if (sourceComponent instanceof WrappedComponent) {
				TargetDetails dropTargetData = dropEvent.getTargetDetails();
				DropTarget target = dropTargetData.getTarget();

				boolean sourceWasAfterTarget = true;
				int index = 0;
				@SuppressWarnings("deprecation")
				Iterator<Component> componentIterator = layout.getComponentIterator();
				Component next = null;
				while (next != target && componentIterator.hasNext()) {
					next = componentIterator.next();
					if (next != sourceComponent) {
						index++;
					} else {
						sourceWasAfterTarget = false;
					}
				}
				if (next == null || next != target) {
					return;
				}

				if (dropTargetData.getData("horizontalLocation").equals(HorizontalDropLocation.CENTER.toString())) {
					if (sourceWasAfterTarget) {
						index--;
					}
				}

				else if (dropTargetData.getData("horizontalLocation").equals(HorizontalDropLocation.LEFT.toString())) {
					index--;
					if (index < 0) {
						index = 0;
					}
				}

				layout.removeComponent(sourceComponent);
				layout.addComponent(sourceComponent, index);
			}
		}
	}

}