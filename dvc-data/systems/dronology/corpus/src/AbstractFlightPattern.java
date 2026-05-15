package edu.nd.dronology.services.extensions.missionplanning.patterns;

import edu.nd.dronology.services.extensions.missionplanning.sync.SynchronizationManager;
import edu.nd.dronology.services.extensions.missionplanning.tasks.IMissionTask;

/**
 * Base class for all predefined flight patterns that can be exanded
 * 
 * @author Michael Vierhauser
 *
 */
public abstract class AbstractFlightPattern implements IFlightPattern {

	protected SynchronizationManager synchPointMgr;
	private TaskList taskList = new TaskList();

	@Override
	public void initialize(SynchronizationManager synchPointMgr) {
		this.synchPointMgr = synchPointMgr;
		doCreateSyncPoints();
	}

	protected abstract void doCreateSyncPoints();

	protected void addSyncPoint(String pointname) {
		synchPointMgr.createSynchronizationPoint(pointname);

	}

	protected void addTask(IMissionTask task) {
		taskList.addTask(task);

	}

	@Override
	public TaskList getTaskList() {
		return taskList;
	}

}
