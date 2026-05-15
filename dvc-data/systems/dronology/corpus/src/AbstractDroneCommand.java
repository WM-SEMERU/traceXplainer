package edu.nd.dronology.core.vehicle.commands;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * Abstract base class for all commands sent to the GCS.<br>
 * Contains the id of the UAV and a unique command id.
 * 
 * @author Michael Vierhauser
 *
 */
@SuppressWarnings("unused")
public class AbstractDroneCommand implements IDroneCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4401634183653180024L;

	static final transient Gson GSON = new GsonBuilder().enableComplexMapKeySerialization().serializeNulls()
			.setDateFormat(DateFormat.LONG).setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES).setVersion(1.0)
			.serializeSpecialFloatingPointValues().create();

	protected final Map<String, Object> data = new HashMap<>();

	private final String uavid;

	private final String command;
	private long sendtimestamp;
	private final String commandid;

	protected AbstractDroneCommand(String uavid, String command) {
		this.uavid = uavid;
		this.command = command;
		this.commandid = UUID.randomUUID().toString();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [" + GSON.toJson(this) + "]";
	}

	@Override
	public String toJsonString() {
		return GSON.toJson(this);
	}

	@Override
	public void timestamp() {
		sendtimestamp = System.currentTimeMillis();
	}

	@Override
	public String getUAVId() {
		return uavid;
	}

}
