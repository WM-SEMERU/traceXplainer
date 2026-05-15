package edu.nd.dronology.core.monitoring.messages;

import java.io.Serializable;
import java.text.DateFormat;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.nd.dronology.core.monitoring.ArtifactIdentifier;
import edu.nd.dronology.core.monitoring.IMonitorableMessage;
import edu.nd.dronology.core.util.FormatUtil;
import edu.nd.dronology.core.util.PreciseTimestamp;

/**
 * 
 * Base class for all monitorable messages.
 * 
 * @author Michael Vierhauser
 *
 */
public class UAVMonitorableMessage implements IMonitorableMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9170213896841874463L;
	static final transient Gson GSON = new GsonBuilder().enableComplexMapKeySerialization().serializeNulls()
			.setDateFormat(DateFormat.LONG).setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES).setVersion(1.0)
			.serializeSpecialFloatingPointValues().create();

	public enum MessageType {
		COMMAND, STATE_CHANGE, PLAN_ACTIVATED, GCS_MONITORING, WAYPOINT_REACHED, PLAN_COMPLETE, VIRTUAL_UAV_ACTIVATED, PHYSICAL_UAV_ACTIVATED, PHYSICAL_UAV_DEACTIVATED, FENCE_BREACH, FENCE_CHECK, MISSION_WAYPOINT, COLLISION
	}

	private final MessageType type;
	private final PreciseTimestamp timestamp;
	private final ArtifactIdentifier id;
	private String uavid;
	private String data;
	private String datatype;

	public UAVMonitorableMessage(MessageType type, String uavid) {
		this(type, uavid, null);
	}

	public UAVMonitorableMessage(MessageType type, String uavid, Serializable data) {
		timestamp = PreciseTimestamp.create();
		this.type = type;
		this.uavid = uavid;
		this.id = new ArtifactIdentifier(ArtifactIdentifier.ROOT, "INTERNAL", type.toString(), uavid);
		if (data != null) {
			this.data = GSON.toJson(data);
			this.datatype = data.getClass().getSimpleName();
		}
	}

	@Override
	public MessageType getType() {
		return type;
	}

	@Override
	public PreciseTimestamp getTimestamp() {
		return timestamp;
	}

	@Override
	public String getUavid() {
		return uavid;
	}

	@Override
	public String getData() {
		return data;
	}

	@Override
	public ArtifactIdentifier<?> getIdentifier() {
		return id;

	}

	public String getDataType() {
		return datatype;
	}

	@Override
	public String toString() {
		return FormatUtil.formatTimestamp(timestamp) + " [Message: " + type + " | uav:" + uavid + " | " + data + " ]";

	}

}
