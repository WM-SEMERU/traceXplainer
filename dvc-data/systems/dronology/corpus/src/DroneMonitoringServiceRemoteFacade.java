package edu.nd.dronology.monitoring.service;

import java.rmi.RemoteException;

import edu.nd.dronology.core.monitoring.ArtifactIdentifier;
import edu.nd.dronology.monitoring.IRemoteMonitoringMessageHandler;
import edu.nd.dronology.services.remote.AbstractRemoteFacade;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

/**
 * 
 * Remote facade for monitoring UAVs<br>
 * Allows registering {@link IRemoteMonitoringMessageHandler} to subscribe to certain message types or topics<br>
 * 
 * 
 * @author Michael Vierhauser
 *
 */

public class DroneMonitoringServiceRemoteFacade extends AbstractRemoteFacade implements IDroneMonitoringRemoteService {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4580658378477037955L;
	private static final ILogger LOGGER = LoggerProvider.getLogger(DroneMonitoringServiceRemoteFacade.class);
	private static volatile DroneMonitoringServiceRemoteFacade INSTANCE;

	protected DroneMonitoringServiceRemoteFacade() throws RemoteException {
		super(DroneMonitoringService.getInstance());
	}

	public static IDroneMonitoringRemoteService getInstance() throws RemoteException {
		if (INSTANCE == null) {
			synchronized (DroneMonitoringServiceRemoteFacade.class) {
				try {
					if (INSTANCE == null) {
						INSTANCE = new DroneMonitoringServiceRemoteFacade();
					}
				} catch (RemoteException e) {
					LOGGER.error(e);
				}
			}
		}
		return INSTANCE;

	}

	@Override
	public void registerMonitoringMessageHandler(IRemoteMonitoringMessageHandler handler) throws RemoteException {
		DroneMonitoringService.getInstance().registerMonitoringMessageHandler(handler);

	}

	@Override
	public void registerMonitoringMessageHandler(IRemoteMonitoringMessageHandler handler, ArtifactIdentifier identifier)
			throws RemoteException {
		DroneMonitoringService.getInstance().registerMonitoringMessageHandler(handler, identifier);

	}

	@Override
	public void setMonitoringFrequency(String uavid, Double frequency) throws RemoteException {
		DroneMonitoringService.getInstance().setMonitoringFrequency(uavid, frequency);

	}

}