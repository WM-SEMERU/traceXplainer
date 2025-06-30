package edu.nd.dronology.services.extensions.missionplanning.service.internal;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import edu.nd.dronology.core.status.DronologyListenerManager;
import edu.nd.dronology.services.core.api.IFileChangeNotifyable;
import edu.nd.dronology.services.core.base.AbstractFileTransmitServiceInstance;
import edu.nd.dronology.services.core.info.MissionInfo;
import edu.nd.dronology.services.core.info.UAVMappingInfo;
import edu.nd.dronology.services.core.items.IMissionPlan;
import edu.nd.dronology.services.core.persistence.MissionPlanningPersistenceProvider;
import edu.nd.dronology.services.core.persistence.PersistenceException;
import edu.nd.dronology.services.core.util.DronologyConstants;
import edu.nd.dronology.services.core.util.DronologyServiceException;
import edu.nd.dronology.services.core.util.ServiceIds;
import edu.nd.dronology.services.extensions.missionplanning.MissionExecutionException;
import edu.nd.dronology.services.extensions.missionplanning.plan.MissionController;
import edu.nd.dronology.services.extensions.missionplanning.sync.SynchronizationManager;
import edu.nd.dronology.services.instances.DronologyElementFactory;
import edu.nd.dronology.services.supervisor.SupervisorService;
import edu.nd.dronology.util.FileUtil;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

public class MissionPlanningServiceInstance extends AbstractFileTransmitServiceInstance<MissionInfo>
		implements IFileChangeNotifyable, IMissionPlanningServiceInstance {

	private static final ILogger LOGGER = LoggerProvider.getLogger(MissionPlanningServiceInstance.class);

	public static final String EXTENSION = DronologyConstants.EXTENSION_MISSION;

	private DronologySeriviceListener listener;

	public MissionPlanningServiceInstance() {
		super(ServiceIds.SERVICE_MISSIONPLANNING, "Mission Planning", EXTENSION);
	}

	@Override
	protected Class<?> getServiceClass() {
		return MissionPlanningService.class;
	}

	@Override
	protected int getOrder() {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	protected String getPropertyPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void doStartService() throws Exception {
		reloadItems();
		registerListener();
	}

	private void registerListener() {
		listener = new DronologySeriviceListener();
		DronologyListenerManager.getInstance().addListener(listener);
		
	}

	@Override
	protected void doStopService() throws Exception {
		unregisterListener();

	}

	private void unregisterListener() {
		DronologyListenerManager.getInstance().removeListener(listener);
		
	}

	@Override
	public void executeMissionPlan(String mission) throws DronologyServiceException {
		try {
			MissionController.getInstance().executeMission(mission);
		} catch (MissionExecutionException e) {
			LOGGER.error(e);
			new DronologyServiceException(e.getMessage());
		}

	}

	@Override
	public void cancelMission() throws DronologyServiceException {
		try {
			MissionController.getInstance().cancelMission();
		} catch (MissionExecutionException e) {
			LOGGER.error(e);
			new DronologyServiceException(e.getMessage());
		}

	}

	@Override
	public void removeUAV(String uavid) throws DronologyServiceException {
		SynchronizationManager.getInstance().removeUAV(uavid);

	}

	@Override
	public MissionInfo createItem() throws DronologyServiceException {
		MissionPlanningPersistenceProvider persistor = MissionPlanningPersistenceProvider.getInstance();
		IMissionPlan missionPlan = MissionElementFactory.createNewMissionPlan();
		missionPlan.setName("New-MissionPlan");
		String savePath = FileUtil.concat(storagePath, missionPlan.getId(), EXTENSION);

		try {
			persistor.saveItem(missionPlan, savePath);
		} catch (PersistenceException e) {
			throw new DronologyServiceException("Error when creating mission plan: " + e.getMessage());
		}
		return new MissionInfo(missionPlan.getName(), missionPlan.getId());
	}

	@Override
	protected String getPath() {
		String path = SupervisorService.getInstance().getMissionPlanningLocation();
		return path;
	}

	@Override
	protected MissionInfo fromFile(String id, File file) throws Throwable {
		IMissionPlan atm = MissionPlanningPersistenceProvider.getInstance().loadItem(file.toURI().toURL());
		MissionInfo info = new MissionInfo(atm.getName(), id);

		BasicFileAttributes attr = Files.readAttributes(Paths.get(file.toURI()), BasicFileAttributes.class);
		info.setDateCreated(attr.creationTime().toMillis());
		info.setDateModified(attr.lastModifiedTime().toMillis());
		info.setDescription(atm.getDescription());
		return info;
	}

	@Override
	public void executeMissionPlan(MissionInfo info) throws DronologyServiceException {
		executeMissionPlan(info, new UAVMappingInfo());
	}

	@Override
	public void executeMissionPlan(MissionInfo info, UAVMappingInfo mapping) throws DronologyServiceException {
		try {
			File file = fileManager.getFile(info.getId());
			IMissionPlan mission = MissionPlanningPersistenceProvider.getInstance().loadItem(file.toURI().toURL());

			MissionController.getInstance().executeMission(mission,mapping);
		} catch (MissionExecutionException | MalformedURLException | PersistenceException e) {
			LOGGER.error(e);
			new DronologyServiceException(e.getMessage());
		}

	}

}
