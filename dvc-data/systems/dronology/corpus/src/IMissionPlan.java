package edu.nd.dronology.services.core.items;

import java.util.List;

public interface IMissionPlan extends IPersistableItem{

	String getDescription();

	void setDescription(String description);

	List<IUAVMissionDescription> getMissionDescriptions();

	void addMissionDescription(IUAVMissionDescription description);



}
