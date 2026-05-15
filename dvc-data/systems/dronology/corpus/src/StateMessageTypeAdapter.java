package edu.nd.dronology.gstation.connector.messages;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.gstation.connector.messages.UAVStateMessage.BatteryStatus;
import edu.nd.dronology.gstation.connector.messages.UAVStateMessage.DroneMode;
import edu.nd.dronology.gstation.connector.messages.UAVStateMessage.DroneStatus;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

public class StateMessageTypeAdapter implements JsonDeserializer<Map> {

	private static final ILogger LOGGER = LoggerProvider.getLogger(StateMessageTypeAdapter.class);

	@Override
	public Map deserialize(JsonElement element, Type mapType, JsonDeserializationContext arg2)
			throws JsonParseException {
		Map<String, Object> dataMap = new HashMap<>();
		JsonObject mapObject = element.getAsJsonObject();

		LlaCoordinate location = deserializeLLACoordinate(mapObject, UAVStateMessage.LOCATION);
		dataMap.put(UAVStateMessage.LOCATION, location);

		Vector3D attitude = deserializeVector3D(mapObject, UAVStateMessage.ATTITUDE);
		dataMap.put(UAVStateMessage.ATTITUDE, attitude);

		Vector3D velocity = deserializeVector3D(mapObject, UAVStateMessage.VELOCITY);
		dataMap.put(UAVStateMessage.VELOCITY, velocity);

		String statusString = mapObject.get(UAVStateMessage.STATUS).getAsString();
		String modeString = mapObject.get(UAVStateMessage.MODE).getAsString();

		dataMap.put(UAVStateMessage.STATUS, DroneStatus.valueOf(statusString));
		dataMap.put(UAVStateMessage.MODE, DroneMode.valueOf(modeString));

		boolean armedValue = mapObject.get(UAVStateMessage.ARMED).getAsBoolean();
		dataMap.put(UAVStateMessage.ARMED, Boolean.valueOf(armedValue));

		boolean armableValue = mapObject.get(UAVStateMessage.ARMABLE).getAsBoolean();
		dataMap.put(UAVStateMessage.ARMABLE, Boolean.valueOf(armableValue));

		double groundspeed = mapObject.get(UAVStateMessage.GROUNDSPEED).getAsDouble();
		dataMap.put(UAVStateMessage.GROUNDSPEED, Double.valueOf(groundspeed));

		BatteryStatus batteryStatus = deserializeBatteryStatus(mapObject, UAVStateMessage.BATTERYSTATUS);
		dataMap.put(UAVStateMessage.BATTERYSTATUS, batteryStatus);

		// TODO Auto-generated method stub
		return dataMap;
	}

	private BatteryStatus deserializeBatteryStatus(JsonObject mapObject, String itemname) {
		JsonElement locationElem = mapObject.get(itemname);
		JsonObject locObject = locationElem.getAsJsonObject();

		double bcurrent = -1;
		if (locObject.has("current")) {
			try {
				JsonPrimitive level = locObject.getAsJsonPrimitive("current");
				bcurrent = level.getAsDouble();
			} catch (ClassCastException e) {
				LOGGER.error("Current not a value");
			} catch (Exception e) {
				LOGGER.error(e);
			}
		}

		double blevel = -1;
		if (locObject.has("level")) {
			try {
				JsonPrimitive level = locObject.getAsJsonPrimitive("level");
				blevel = level.getAsDouble();
			} catch (ClassCastException e) {
				LOGGER.error("Level not a value");
			} catch (Exception e) {
				LOGGER.error(e);
			}

		}

		double bvoltage = -1;
		if (locObject.has("voltage")) {
			try {
				JsonPrimitive volt = locObject.getAsJsonPrimitive("voltage");
				bvoltage = volt.getAsDouble();
			} catch (ClassCastException e) {
				LOGGER.error("Voltage not a value");
			} catch (Exception e) {
				LOGGER.error(e);
			}
		}
		return new BatteryStatus(bcurrent, bvoltage, blevel);
	}

	private LlaCoordinate deserializeLLACoordinate(JsonObject mapObject, String itemname) {
		JsonElement locationElem = mapObject.get(itemname);
		JsonObject locObject = locationElem.getAsJsonObject();

		JsonPrimitive latitude = locObject.getAsJsonPrimitive("x");
		JsonPrimitive longitude = locObject.getAsJsonPrimitive("y");
		JsonPrimitive altitude = locObject.getAsJsonPrimitive("z");

		return new LlaCoordinate(latitude.getAsDouble(), longitude.getAsDouble(), altitude.getAsDouble());

	}

	private Vector3D deserializeVector3D(JsonObject mapObject, String itemname) {
		JsonElement locationElem = mapObject.get(itemname);
		JsonObject locObject = locationElem.getAsJsonObject();

		JsonPrimitive x = locObject.getAsJsonPrimitive("x");
		JsonPrimitive y = locObject.getAsJsonPrimitive("y");
		JsonPrimitive z = locObject.getAsJsonPrimitive("z");

		return new Vector3D(x.getAsDouble(), y.getAsDouble(), z.getAsDouble());
	}

}
