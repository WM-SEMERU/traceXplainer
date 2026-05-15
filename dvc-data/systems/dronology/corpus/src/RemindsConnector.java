package edu.nd.dronology.monitoring.reminds;

import java.text.DateFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import at.jku.mevss.eventdistributor.core.transmit.TransmittableEventObject;
import at.jku.mevss.eventdistributor.core.transmit.TransmittableObjectFactory;
import at.jku.mevss.eventpublisher.core.internal.Publisher;
import at.jku.mevss.util.utils.PreciseTimestamp;
import edu.nd.dronology.core.monitoring.messages.UAVMonitorableMessage;
import edu.nd.dronology.gstation.connector.messages.UAVMonitoringMessage;
import edu.nd.dronology.gstation.connector.messages.UAVStateMessage;

public class RemindsConnector {
	static Gson gson = new GsonBuilder().enableComplexMapKeySerialization().serializeNulls()
			.setDateFormat(DateFormat.LONG).setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).setVersion(1.0)
			.serializeSpecialFloatingPointValues().create();
	private static Map<String, MockupProbe> stateProbes = new ConcurrentHashMap();
	private static Map<String, MockupProbe> dronologyProbes = new ConcurrentHashMap<>();
	private static Map<String, MockupProbe> monitoringProbes = new ConcurrentHashMap<>();
	static {
		try {
			Publisher.setMode(Publisher.Mode.M_SOCKET);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static synchronized void notify(UAVStateMessage message) {
		MockupProbe probePoint = getStateProbe(message.getUavid());
		// p.publish();
		TransmittableEventObject event = transform(message);
		try {
			probePoint.sendData(event);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static synchronized void notify(UAVMonitoringMessage message) {
		MockupProbe probePoint = getMonitoringProbes(message.getUavid());
		// p.publish();
		TransmittableEventObject event = transform(message);
		try {
			probePoint.sendData(event);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	// public static void notify(UAVStateChangeMessage message) {
	//
	// MockupProbe probePoint = getDronologyProbes(message.getType().name(),
	// message.getUavid());
	// // p.publish();
	// TransmittableEventObject event = transform(message);
	// try {
	// probePoint.sendData(event);
	// } catch (Throwable t) {
	// t.printStackTrace();
	// }
	// }
	public static void notify(UAVMonitorableMessage message) {
		MockupProbe probePoint = getProbe(message);
		// p.publish();
		TransmittableEventObject event = transform(message);
		try {
			probePoint.sendData(event);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private static MockupProbe getProbe(UAVMonitorableMessage message) {
		String uavid = message.getUavid();
		String msgType = message.getData() != null ? message.getDataType() : message.getType().name();
		if (msgType.equals("GoToCommand") || msgType.equals("TakeoffCommand") || msgType.equals("SetGroundSpeedCommand")
				|| msgType.equals("SetModeCommand")) {
			msgType = "flightcontrol";
		} else if (message.getDataType().equals("STATE_CHANGE")
				|| message.getType().toString().equals("STATE_CHANGE")) {
			msgType = "uavcontrol";
		}

		else if (msgType.equals("planchange") || message.getType().toString().equals("WAYPOINT_REACHED")
				|| msgType.equals("FlightPlan")|| msgType.equals("SimpleTakeoffFlightPlan")) {
			msgType = "routeplanning";
		} else if (message.getType().toString().equals("PHYSICAL_UAV_ACTIVATED") || message.getDataType().equals("FlightMode")) {
			msgType = "uavcontrol";
		} else {
			System.out.println(">>>>>>>>>" + message.toString() + "::::" + message.getData());
		}
		String id = "dronology." + msgType + "." + uavid;
		if (dronologyProbes.containsKey(id)) {
			return dronologyProbes.get(id);
		}
		String scope = ("dronology." + msgType);
		// String probename = id + "-dronologymessage";
		MockupProbe pp = new MockupProbe(id, "DRONOLOGY_INTERNAL", scope);
		dronologyProbes.put(id, pp);
		return pp;
	}

	// public static void notify(UAVPlanChangeMessage message) {
	//
	// MockupProbe probePoint = getProbe(message.getType().name(),
	// message.getUavid());
	// // p.publish();
	// TransmittableEventObject event = transform(message);
	// try {
	// probePoint.sendData(event);
	// } catch (Throwable t) {
	// t.printStackTrace();
	// }
	// }
	private static synchronized MockupProbe getStateProbe(String uavid) {
		if (stateProbes.containsKey(uavid)) {
			return stateProbes.get(uavid);
		}
		String scope = ("dronology.gcs1.uav@" + uavid);
		String probename = uavid + "-statemessages";
		MockupProbe pp = new MockupProbe(probename, "CS", scope);
		stateProbes.put(uavid, pp);
		return pp;
	}

	// private static synchronized MockupProbe getDronologyProbes(String
	// messageType, String uavid) {
	// String id = "dronology." + messageType + "." + uavid;
	// if (dronologyProbes.containsKey(id)) {
	// return dronologyProbes.get(id);
	// }
	// String scope = ("dronology." + messageType);
	// // String probename = id + "-dronologymessage";
	// MockupProbe pp = new MockupProbe(id, "DRONOLOGY_INTERNAL", scope);
	// dronologyProbes.put(id, pp);
	// return pp;
	// }
	private static synchronized MockupProbe getMonitoringProbes(String uavid) {
		if (monitoringProbes.containsKey(uavid)) {
			return monitoringProbes.get(uavid);
		}
		String scope = ("dronology.gcs1.uav@" + uavid);
		String probename = uavid + "-monitoringmessage";
		MockupProbe pp = new MockupProbe(probename, "CS", scope);
		monitoringProbes.put(uavid, pp);
		return pp;
	}

	private static TransmittableEventObject transform(UAVStateMessage message) {
		TransmittableEventObject event = TransmittableObjectFactory.createEventObject(PreciseTimestamp.create(),
				"UAVStateMessage");
		event.addData(TransmittableObjectFactory.createEventData(gson.toJson(message), "state"));
		return event;
	}

	private static TransmittableEventObject transform(UAVMonitoringMessage message) {
		TransmittableEventObject event = TransmittableObjectFactory.createEventObject(PreciseTimestamp.create(),
				"UAVMonitoringMessage");
		event.addData(TransmittableObjectFactory.createEventData(gson.toJson(message), "monitoring"));
		return event;
	}

	// private static TransmittableEventObject transform(UAVStateChangeMessage
	// message) {
	// TransmittableEventObject event =
	// TransmittableObjectFactory.createEventObject(PreciseTimestamp.create(),
	// "UAVStateChangeMessage");
	// event.addData(TransmittableObjectFactory.createEventData(gson.toJson(message),
	// "statechange"));
	// return event;
	// }
	//
	// private static TransmittableEventObject transform(UAVPlanChangeMessage
	// message) {
	// TransmittableEventObject event =
	// TransmittableObjectFactory.createEventObject(PreciseTimestamp.create(),
	// "UAVPlanChangeMessage");
	// event.addData(TransmittableObjectFactory.createEventData(gson.toJson(message),
	// "planchange"));
	// return event;
	// }
	public static TransmittableEventObject transform(UAVMonitorableMessage message) {
		String msgType = message.getData() != null ? message.getDataType() : message.getType().name();
		if(message.getType().toString().equals("WAYPOINT_REACHED")) {
			msgType = "WaypointReached";
		}
		if(message.getType().toString().equals("PLAN_ACTIVATED")) {
			msgType = "FlightPlanActivated";
		}
		if(message.getType().toString().equals("PLAN_COMPLETE")) {
			msgType = "FlightPlanComplete";
		}
		if(message.getType().toString().equals("PHYSICAL_UAV_ACTIVATED")) {
			msgType = "UAVActivation";
		}
		if(message.getType().toString().equals("STATE_CHANGE")) {
			msgType = "StateChange";
		}
		
		
		if(msgType.equals("FlightPlan")) {
			System.out.println("xx");
		}
		TransmittableEventObject event = TransmittableObjectFactory.createEventObject(PreciseTimestamp.create(),
				msgType);
		if (message.getData() != null) {
			event.addData(TransmittableObjectFactory.createEventData(message.getData(), "data"));
		}
		return event;
	}
}