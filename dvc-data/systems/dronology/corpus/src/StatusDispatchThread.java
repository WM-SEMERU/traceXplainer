package edu.nd.dronology.gstation.connector.dispatch;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import edu.nd.dronology.core.IUAVPropertyUpdateNotifier;
import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.core.status.DronologyListenerManager;
import edu.nd.dronology.core.vehicle.DroneFlightStateManager.FlightMode;
import edu.nd.dronology.gstation.connector.messages.AbstractUAVMessage;
import edu.nd.dronology.gstation.connector.messages.UAVModeChangeMessage;
import edu.nd.dronology.gstation.connector.messages.UAVStateMessage;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

public class StatusDispatchThread extends AbstractStatusDispatchThread<AbstractUAVMessage> implements Callable {
	private static final ILogger LOGGER = LoggerProvider.getLogger(StatusDispatchThread.class);

	private IUAVPropertyUpdateNotifier listener;

	public StatusDispatchThread(final BlockingQueue<AbstractUAVMessage> queue, IUAVPropertyUpdateNotifier listener) {
		super(queue);
		this.listener = listener;
	}

	protected void notifyListener() throws Exception {

	}

	@Override
	public Object call() {
		while (cont.get()) {

			try {
				AbstractUAVMessage state = queue.take();
				if (state instanceof UAVStateMessage) {
					UAVStateMessage sm = (UAVStateMessage) state;
					LlaCoordinate location = sm.getLocation();
					double level = sm.getBatterystatus().getBatteryLevel();
					double speed = sm.getGroundspeed();
					listener.update(location, level, speed, sm.getVelocity(), sm.getAttitude());
				} else if (state instanceof UAVModeChangeMessage) {
					UAVModeChangeMessage mcm = (UAVModeChangeMessage) state;
					
					listener.updateMode(mcm.getMode());

					// If the new mode is LOITER, we should remove this drone from its mission plan
					// (if it has one).
					// TODO: determine if this is actually the right place to do this.
					// TODO: potentially create an enum for mode names.
					if (mcm.getMode().equals(FlightMode.USER_CONTROLLED.toString())) {
						DronologyListenerManager.getInstance().notifyUAVRemoved(mcm.getUavid());
						// notify flight plan pool... remove active plans..

					}

				} else {
					LOGGER.error("Unhandled message type detected " + state.getClass());
				}
			} catch (InterruptedException e) {
				LOGGER.warn("Status Dispatch Thread terminated");
			} catch (Exception e) {
				LOGGER.error(e);
			}

		}
		LOGGER.info("Dispatcher shutdown!");
		return null;
	}

}
