//package edu.nd.dronology.core.monitoring.messages;
//
//import edu.nd.dronology.core.monitoring.ArtifactIdentifier;
//import edu.nd.dronology.core.monitoring.IMonitorableMessage;
//import edu.nd.dronology.core.monitoring.messages.UAVMonitorableMessage.MessageType;
//import edu.nd.dronology.core.util.PreciseTimestamp;
//
//public class UAVPlanChangeMessage implements IMonitorableMessage {
//
//	private String uavid;
//	private long startTime;
//	private String type;
//	private String flightid;
//	private long endTime;
//	private final PreciseTimestamp timestamp;
//
//	public UAVPlanChangeMessage(String uavid, String type, String flightid, long startTime, long endTime) {
//		this.timestamp = PreciseTimestamp.create();
//		this.uavid = uavid;
//		this.type = type;
//		this.flightid = flightid;
//		this.startTime = startTime;
//		this.endTime = endTime;
//	}
//
//	@Override
//	public ArtifactIdentifier<?> getIdentifier() {
//		return new ArtifactIdentifier(ArtifactIdentifier.ROOT, "INTERNAL", MessageType.PLAN_CHANGE.toString(), uavid);
//	}
//
//	public String getUavid() {
//		return uavid;
//	}
//
//	@Override
//	public String getData() {
//		// TODO Auto-generated method stub
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
//		return MessageType.PLAN_CHANGE;
//	}
//
//}
