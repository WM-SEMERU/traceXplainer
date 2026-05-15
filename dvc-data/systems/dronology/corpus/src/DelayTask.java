package edu.nd.dronology.services.extensions.missionplanning.tasks;

import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

/**
 * Delay tasks specifying a delay after a route has been executed before commencing with the next round. 
 * 
 * @author Michael Vierhauser
 *
 */
public class DelayTask extends AbstractMissionTask {
	private static final ILogger LOGGER = LoggerProvider.getLogger(DelayTask.class);

	private final double duration;
	private long startTime;

	/**
	 * 
	 * @param uavID
	 * @param taskName
	 * @param duration
	 *          in seconds
	 */
	protected DelayTask(String uavID, String taskName, double duration) {
		super(uavID, taskName);
		this.duration = duration;
	}

	public double getDuration() {
		return duration;
	}

	public void startDelayTask() {
		LOGGER.info("Delay started for UAV '" + getUAVId() + "' duration: " + duration);
		startTime = System.currentTimeMillis();
	}

	public boolean isFinished() {
		boolean finished = System.currentTimeMillis() - startTime > duration * 1000;

		if (finished) {
			LOGGER.info("Delay finished for UAV '" + getUAVId() + "'");
		}
		return finished;

	}

}