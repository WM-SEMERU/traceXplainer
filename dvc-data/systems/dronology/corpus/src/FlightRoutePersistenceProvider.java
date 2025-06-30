package edu.nd.dronology.services.core.persistence;

import edu.nd.dronology.services.core.items.IFlightRoute;
import edu.nd.dronology.services.core.persistence.internal.FlightRouteXStreamPersistor;

/**
 * Provider implementation for {@link IFlightRoute}.<br>
 * Details see {@link AbstractItemPersistenceProvider}
 * 
 * @author Michael Vierhauser
 *  
 */
public class FlightRoutePersistenceProvider extends AbstractItemPersistenceProvider<IFlightRoute> {

	public FlightRoutePersistenceProvider() {
		super();
	}

	@Override
	protected void initPersistor() {
		PERSISTOR = new FlightRouteXStreamPersistor();

	}

	@Override
	protected void initPersistor(String type) {
		initPersistor();
	}

	public static FlightRoutePersistenceProvider getInstance() {
		return new FlightRoutePersistenceProvider();
	}

}
