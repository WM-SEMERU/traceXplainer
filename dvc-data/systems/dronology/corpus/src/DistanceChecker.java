package edu.nd.dronology.monitoring.simplechecker.checker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.core.monitoring.DronologyMonitoringManager;
import edu.nd.dronology.core.monitoring.IMonitorableMessage;
import edu.nd.dronology.core.monitoring.MessageMarshaller;
import edu.nd.dronology.core.monitoring.messages.UAVMonitorableMessage.MessageType;
import edu.nd.dronology.core.util.ManagedHashTableList;
import edu.nd.dronology.gstation.connector.messages.UAVStateMessage;
import edu.nd.dronology.util.Pair;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

public class DistanceChecker {
	private static final transient ILogger LOGGER = LoggerProvider.getLogger(DistanceChecker.class);

	private static final double MIN_DIST = 5;

	public DistanceChecker() {
		Timer timer = new Timer();
		timer.schedule(new CheckTask(), 0, 1500);
	}

	private static transient DistanceChecker INSTANCE;

	private BlockingQueue<UAVStateMessage> messageQueue = new ArrayBlockingQueue<>(100);

	private transient ManagedHashTableList<String, Pair> dataMap = new ManagedHashTableList<>();

	public static DistanceChecker getInstance() {
		if (INSTANCE == null) {
			synchronized (SimpleChecker.class) {
				if (INSTANCE == null) {
					INSTANCE = new DistanceChecker();
				}
			}
		}
		return INSTANCE;
	}

	public void notify(IMonitorableMessage message) {
		messageQueue.offer((UAVStateMessage) message);

	}

	private class CheckTask extends TimerTask {
		@Override
		public void run() {
			List<UAVStateMessage> toCheck = new ArrayList<>();
			messageQueue.drainTo(toCheck);
			// System.out.println("RUN DISTANCE CHECK!" + dataMap.size());
			calculateDistance(toCheck);
			calculateDistance();
		}

	}

	public void processMessage(UAVStateMessage msg) {
		try {

			double xVal = msg.getLocation().getLatitude();
			double yVal = msg.getLocation().getLongitude();
			double zVal = msg.getLocation().getAltitude();

			LlaCoordinate cord = new LlaCoordinate(xVal, yVal, zVal);
			// System.out.println(event.getSource()+":::"+cord);

			dataMap.add(msg.getUavid(), Pair.create(msg.getSendtimestamp(), cord));

			// setLastValidState(Boolean.parseBoolean(value.toString()));
		} catch (Throwable e) {
			LOGGER.error(e);
		}
	}

	public void calculateDistance() {
		try {
			ManagedHashTableList<String, Pair> checkMap = new ManagedHashTableList<>();
			synchronized (dataMap) {
				for (Entry<String, List<Pair>> e : dataMap.entrySet()) {
					checkMap.put(e.getKey(), e.getValue());
				}
				dataMap.clear();
			}

			for (Entry<String, List<Pair>> e : checkMap.entrySet()) {
				String id = e.getKey();
				List<Pair> cords = e.getValue();

				for (Pair corrd : cords) {
					for (Entry<String, List<Pair>> f : checkMap.entrySet()) {
						String fid = f.getKey();
						if (id.equals(fid)) {
							continue;
						}
						List<Pair> fcords = f.getValue();
						for (Pair fcoord : fcords) {

							// System.out.println(id + "-" + fid + "DISTANCE:" + corrd.distance(fcoord));

							LlaCoordinate c1 = (LlaCoordinate) Pair.cast(corrd).getSecond();
							LlaCoordinate c2 = (LlaCoordinate) Pair.cast(fcoord).getSecond();

							long ts1 = (long) Pair.cast(corrd).getFirst();
							long ts2 = (long) Pair.cast(fcoord).getFirst();
							long dif = Math.abs(ts1 - ts2);
							// System.out.println(Math.abs(ts1.getTimestamp() - ts2.getTimestamp()));
							if (dif > 500) {

								continue;
							}

							double distance = (c1.distance(c2));

							if (distance < MIN_DIST) {
								LOGGER.error("COLLISION!!!");
								DronologyMonitoringManager.getInstance().publish(MessageMarshaller.createMessage(
										MessageType.COLLISION, id, new CollisionInfo(id, fid, distance)));
							}
						}

					}
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}

	}

	public void calculateDistance(List<UAVStateMessage> toCheck) {
		toCheck.forEach(m -> {
			processMessage(m);
		});

	}

}
