package edu.nd.dronology.gstation.connector.messages;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.nd.dronology.core.monitoring.ArtifactIdentifier;
import edu.nd.dronology.core.monitoring.IMonitorableMessage;
import edu.nd.dronology.core.monitoring.messages.UAVMonitorableMessage.MessageType;
import edu.nd.dronology.core.util.PreciseTimestamp;

public class AbstractUAVMessage<T> implements Serializable, IMonitorableMessage {

	private static final long serialVersionUID = 8470856533906132618L;

	private transient PreciseTimestamp receiveTimestamp;
	private long sendtimestamp;

	protected String type;
	protected final Map<String, T> data = new HashMap<>();
	protected String uavid;
	private String groundstationid;

	public AbstractUAVMessage(String type, String groundstationid, String uavid) {
		this.type = type;
		this.uavid = uavid;
		this.groundstationid = groundstationid;
		this.receiveTimestamp = PreciseTimestamp.create();
	}

	public void timestamp() {
		this.receiveTimestamp = PreciseTimestamp.create();

	}

	@Override
	public PreciseTimestamp getTimestamp() {
		return receiveTimestamp;
	}

	public long getSendtimestamp() {
		return sendtimestamp;
	}

	@Override
	public String toString() {
		return UAVMessageFactory.GSON.toJson(this);
	}

	@Override
	public String getUavid() {
		return uavid;
	}

	public void setUavid(String uavid) {
		this.uavid = uavid;
	}

	public void addPropery(String key, T value) {
		data.put(key, value);

	}

	public T getProperty(String key) {
		return data.get(key);

	}

	public Set<Entry<String, T>> getProperties() {
		return data.entrySet();
	}

	public void setType(String type) {
		this.type = type;

	}

	public String getGCSId() {
		return groundstationid;
	}

	@Override
	public ArtifactIdentifier<?> getIdentifier() {
		return new ArtifactIdentifier(ArtifactIdentifier.ROOT, groundstationid, uavid, getClass().getSimpleName());

	}

	@Override
	public String getData() {
		return this.toString();
	}

	@Override
	public MessageType getType() {
		return MessageType.GCS_MONITORING;
	}

}
