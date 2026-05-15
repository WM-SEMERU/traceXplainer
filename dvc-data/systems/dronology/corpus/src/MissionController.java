package edu.nd.dronology.services.extensions.missionplanning.plan;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.util.concurrent.RateLimiter;

import edu.nd.dronology.services.core.info.UAVMappingInfo;
import edu.nd.dronology.services.core.items.IMissionPlan;
import edu.nd.dronology.services.extensions.missionplanning.MissionExecutionException;
import edu.nd.dronology.services.extensions.missionplanning.sync.SynchronizationManager;
import edu.nd.dronology.services.extensions.missionplanning.v1.FullMissionPlan;
import edu.nd.dronology.services.instances.dronesetup.DroneSetupService;
import edu.nd.dronology.util.NamedThreadFactory;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

/**
 * 
 * Central control class responsible for building, initializing, and executing a
 * {@link FullMissionPlan}.<br>
 * The {@link MissionController} periodically checks for new tasks that can be
 * activated and activates them on demand.
 * 
 * @author Michael Vierhauser
 */
public class MissionController implements Runnable {
	private static final ILogger LOGGER = LoggerProvider.getLogger(MissionController.class);
	RateLimiter barrier = RateLimiter.create(1);
	AtomicBoolean missionrunning = new AtomicBoolean(true);
	private transient IExecuteableMissionPlan missionPlan;
	private static final ExecutorService SERVICE_EXECUTOR = Executors
			.newSingleThreadExecutor(new NamedThreadFactory("MissionControl"));
	private transient static MissionController INSTANCE;

	@Deprecated
	public void executeMission(String mission) throws MissionExecutionException {
		if (missionPlan != null) {
			throw new MissionExecutionException("Another mission is currently running!");
		}

		FullMissionPlan mp = new FullMissionPlan(mission);
		mp.build();
		missionPlan = mp;
		SERVICE_EXECUTOR.submit(this);
	}

	public void executeMission(IMissionPlan mission, UAVMappingInfo mapping) throws MissionExecutionException {
		if (missionPlan != null) {
			throw new MissionExecutionException("Another mission is currently running!");
		}
		
		FullMissionPlan2 mp = new FullMissionPlan2(mission);
		mp.build(mapping);
		missionPlan = mp;
		SERVICE_EXECUTOR.submit(this);

	}

	@Override
	public void run() {
		missionrunning.getAndSet(true);
		while (missionrunning.get() && missionPlan.isMissionActive()) {
			barrier.acquire();
			try {
				missionPlan.checkAndActivateTask();
			} catch (MissionExecutionException e) {
				LOGGER.error(e);
				try {
					cancelMission();
				} catch (MissionExecutionException e1) {
					LOGGER.error(e1);
				}
			}
		}
		LOGGER.info("Mission complete");
		SynchronizationManager.getInstance().resetAltitudes();
		missionPlan = null;
	}

	/**
	 * @return The singleton MissionController instance
	 */
	public static MissionController getInstance() {
		if (INSTANCE == null) {
			synchronized (DroneSetupService.class) {
				if (INSTANCE == null) {
					INSTANCE = new MissionController();
				}
			}
		}
		return INSTANCE;
	}

	public void cancelMission() throws MissionExecutionException {
		if (missionPlan == null) {
			throw new MissionExecutionException("No mission currently active");
		}
		SynchronizationManager.getInstance().resetAltitudes();
		missionrunning.getAndSet(false);
		missionPlan.cancelMission();
		missionPlan = null;
	}

}
