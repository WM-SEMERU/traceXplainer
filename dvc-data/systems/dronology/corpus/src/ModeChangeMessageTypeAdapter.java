package edu.nd.dronology.gstation.connector.messages;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import edu.nd.dronology.core.vehicle.DroneFlightStateManager.FlightMode;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

public class ModeChangeMessageTypeAdapter implements JsonDeserializer<UAVModeChangeMessage> {

	private static final ILogger LOGGER = LoggerProvider.getLogger(ModeChangeMessageTypeAdapter.class);

	@Override
	public UAVModeChangeMessage deserialize(JsonElement element, Type mapType, JsonDeserializationContext arg2)
			throws JsonParseException {
		JsonObject mapObject = element.getAsJsonObject();
		String uavid = mapObject.get(UAVModeChangeMessage.UAV_ID).getAsString();
		String gsid = mapObject.get(UAVModeChangeMessage.GS_ID).getAsString();
		JsonObject data = mapObject.getAsJsonObject("data");

		String mode = data.get(UAVModeChangeMessage.MODE).getAsString();

		String dMode = FlightMode.USER_CONTROLLED.toString();

		if ("LOITER".equals(mode)) {
			dMode = FlightMode.USER_CONTROLLED.toString();
		} else if ("STABILIZE".equals(mode)) {
			dMode = FlightMode.IN_AIR.toString();
		} else if ("GUIDED".equals(mode)) {
			dMode = FlightMode.IN_AIR.toString();
		} else if ("LAND".equals(mode)) {
			dMode = FlightMode.IN_AIR.toString();
		} else {
			LOGGER.hwFatal("Mode '" + mode + "' not recognized!");
		}

		UAVModeChangeMessage message = new UAVModeChangeMessage(gsid, uavid, dMode);

		return message;
	}

}
