//package edu.nd.dronology.core.monitoring.messages;
//
//import edu.nd.dronology.core.monitoring.ArtifactIdentifier;
//import edu.nd.dronology.core.monitoring.IMonitorableMessage;
//import edu.nd.dronology.core.monitoring.messages.UAVMonitorableMessage.MessageType;
//import edu.nd.dronology.core.util.PreciseTimestamp;
//
//public class UAVStateChangeMessage implements IMonitorableMessage {
//
//	private String uavid;
//	private String oldstate;
//	private String newstate;
//	private final PreciseTimestamp timestamp;
//
//	public UAVStateChangeMessage(String uavid, String oldstate, String newstate) {
//		this.uavid = uavid;
//		timestamp = PreciseTimestamp.create();
//		this.oldstate = oldstate;
//		this.newstate = newstate;
//	}
//
//	@Override
//	public ArtifactIdentifier<?> getIdentifier() {
//		return new ArtifactIdentifier(ArtifactIdentifier.ROOT, "INTERNAL",MessageType.STATE_CHANGE.toString(), uavid);
//
//	}
//
//	public String getUavid() {
//		return uavid;
//	}
//
//	@Override
//	public String getData() {
//		return null;
//	}
//
//	@Override
//	public PreciseTimestamp getTimestamp() {
//		return timestamp;
//	}
//
//	@Override
//	public MessageType getType() {
//		return MessageType.STATE_CHANGE;
//	}
//
//}
