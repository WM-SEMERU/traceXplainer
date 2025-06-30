package edu.nd.dronology.monitoring.simplechecker.checker;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.DateFormat;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.nd.dronology.core.DronologyConstants;
import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.core.monitoring.DronologyMonitoringManager;
import edu.nd.dronology.core.monitoring.IMonitorableMessage;
import edu.nd.dronology.core.monitoring.MessageMarshaller;
import edu.nd.dronology.core.monitoring.messages.UAVMonitorableMessage.MessageType;
import edu.nd.dronology.gstation.connector.messages.UAVStateMessage;
import edu.nd.dronology.monitoring.IRemoteMonitoringMessageHandler;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

public class SimpleCheckerMessageHandler extends UnicastRemoteObject implements IRemoteMonitoringMessageHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3680886616398014407L;

	private static final transient ILogger LOGGER = LoggerProvider.getLogger(SimpleCheckerMessageHandler.class);

	static final transient Gson GSON = new GsonBuilder().enableComplexMapKeySerialization().serializeNulls()
			.setDateFormat(DateFormat.LONG).setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
			.setVersion(1.0).serializeSpecialFloatingPointValues().create();

	public SimpleCheckerMessageHandler() throws RemoteException {

		super();
	}

	GeofenceRectangle geofence = new GeofenceRectangle(new LlaCoordinate(41.682, -86.253, 0),
			new LlaCoordinate(41.676, -86.244, 0));

	// 'LlaCoordinate(41.679517, -86.252505, 10.000000)
	@Override
	public void notifyMonitoringMessage(IMonitorableMessage message) throws RemoteException {
		if (message.getType().equals(MessageType.PHYSICAL_UAV_ACTIVATED)) {
			String data = message.getData();
			LlaCoordinate cord = GSON.fromJson(data, LlaCoordinate.class);
			boolean latOK = checkLat(cord);
			boolean longOK = checkLong(cord);
			boolean altOK = checkAlt(cord);

			boolean fence = geofence.isInside(cord);

			if (latOK && longOK && altOK) {
				LOGGER.info("Home Location of '" + message.getUavid() + "' OK");

			}
			if (fence) {
				LOGGER.info("Home Location of '" + message.getUavid() + "inside of geofence!");
				DronologyMonitoringManager.getInstance().publish(MessageMarshaller
						.createMessage(MessageType.FENCE_CHECK, message.getUavid(), message.getData()));

			} else {
				LOGGER.error("Home Location of '" + message.getUavid() + "outside  of geofence!");
				DronologyMonitoringManager.getInstance().publish(MessageMarshaller
						.createMessage(MessageType.FENCE_BREACH, message.getUavid(), message.getData()));
			}
		} else if (message instanceof UAVStateMessage) {
			UAVStateMessage msg = (UAVStateMessage) message;

			boolean fence = true || geofence.isInside(msg.getLocation());

			DistanceChecker.getInstance().notify(message);

			if (fence) {
				// LOGGER.info("Location of '" + message.getUavid() + "inside of geofence!");

			} else {
				LOGGER.missionError("GEOFENCE BREACH! -- '" + message.getUavid() + "outside  of geofence!");
				DronologyMonitoringManager.getInstance().publish(
						MessageMarshaller.createMessage(MessageType.FENCE_BREACH, message.getUavid(), msg.getData()));
				SimpleChecker.getInstance().emergencyStop(msg.getUavid());
			}

		} else {
			// System.out.println(message.toString());
		}
	}

	private boolean checkAlt(LlaCoordinate cord) {
		if (cord.getAltitude() < 0 || cord.getAltitude() > DronologyConstants.TAKE_OFF_ALTITUDE) {
			LOGGER.error("WRONG LATITUDE");
			return false;
		}
		return true;
	}

	private boolean checkLong(LlaCoordinate cord) {
		if (cord.getLongitude() > -86 || cord.getLongitude() < -87) {
			LOGGER.error("WRONG LATITUDE");
			return false;
		}
		return true;
	}

	private boolean checkLat(LlaCoordinate cord) {
		if (cord.getLatitude() > 42 || cord.getLatitude() < 41) {
			LOGGER.error("WRONG LATITUDE");
			return false;
		}
		return true;
	}

}
