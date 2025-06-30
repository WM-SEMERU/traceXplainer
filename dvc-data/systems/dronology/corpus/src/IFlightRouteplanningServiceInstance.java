package edu.nd.dronology.services.instances.flightroute;

import java.util.Collection;

import edu.nd.dronology.services.core.api.IFileTransmitServiceInstance;
import edu.nd.dronology.services.core.info.FlightRouteCategoryInfo;
import edu.nd.dronology.services.core.info.FlightRouteInfo;
import edu.nd.dronology.services.core.util.DronologyServiceException;

public interface IFlightRouteplanningServiceInstance extends IFileTransmitServiceInstance<FlightRouteInfo> {

	Collection<FlightRouteCategoryInfo> getFlightPathCategories();

	FlightRouteInfo getRouteByName(String routeName) throws DronologyServiceException;

}
