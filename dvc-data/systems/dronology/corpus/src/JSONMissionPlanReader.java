package edu.nd.dronology.services.extensions.missionplanning.v1;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import com.google.gson.Gson;

import edu.nd.dronology.services.extensions.missionplanning.MissionExecutionException;

/**
 * Simple JSON Reader that converts a json String into a {@link FullMissionPlan} object.
 * 
 * @author Jane Cleland-Huang
 *
 */
@SuppressWarnings("rawtypes")
public class JSONMissionPlanReader {

	FullMissionPlan coordinatedMission;
	// String fileName;

	public JSONMissionPlanReader(FullMissionPlan coordinatedMission) {
		this.coordinatedMission = coordinatedMission;
	}

	public void parseMission(String content)
			throws FileNotFoundException, IOException, JSONException, MissionExecutionException {
		// parsing input file
		// Object obj = new JSONParser().parse(content);

		Map valueMap = new Gson().fromJson(content, Map.class);

		List plans = (List) valueMap.get("plans");

		for (Object object : plans) {
			Map jo2 = (Map) object;
			String uavid = (String) jo2.get("id");
			coordinatedMission.addUAV(uavid);

			List tasks = (List) jo2.get("tasks");
			for (Object task : tasks) {
				parseTasks(task, uavid);
			}
		}

	}

	private void parseTasks(Object item, String uavid) throws MissionExecutionException {
		Map jsnObject = (Map) item;
		String task = (String) jsnObject.get("task");
		String taskID;

		taskID = (String) jsnObject.get("name");
		String duration = (String) jsnObject.get("duration");

		if (duration != null) {
			coordinatedMission.addTask(uavid, task, taskID, duration);
		} else {
			coordinatedMission.addTask(uavid, task, taskID);
		}
	}

}
