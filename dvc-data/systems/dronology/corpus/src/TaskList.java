package edu.nd.dronology.services.extensions.missionplanning.patterns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.nd.dronology.services.extensions.missionplanning.tasks.IMissionTask;

/**
 * Contains a list of {@link IMissionTask} part mission.
 * 
 * @author Jane Cleland-Huang
 *
 */
public class TaskList {

	List<IMissionTask> tasks = new ArrayList<>();

	public List<IMissionTask> getTasks() {
		return Collections.unmodifiableList(tasks);
	}

	public void addTask(IMissionTask task) {
		tasks.add(task);

	}

}
