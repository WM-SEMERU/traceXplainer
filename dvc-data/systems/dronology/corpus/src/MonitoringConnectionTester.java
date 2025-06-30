package edu.nd.dronology.test;

import edu.nd.dronology.core.monitoring.ArtifactIdentifier;
import edu.nd.dronology.monitoring.reminds.RemoteMessageHandler;
import edu.nd.dronology.monitoring.service.IDroneMonitoringRemoteService;

public class MonitoringConnectionTester {

	public static void main(String[] args) {
		BaseServiceProvider provider = new BaseServiceProvider();

		try {
			System.setProperty("java.rmi.server.hostname", "localhost");
			
			IDroneMonitoringRemoteService sevice = (IDroneMonitoringRemoteService) provider.getRemoteManager()
					.getService(IDroneMonitoringRemoteService.class);

		//	ArtifactIdentifier id = new ArtifactIdentifier(ArtifactIdentifier.ROOT, "INTERNAL");
			ArtifactIdentifier id = new ArtifactIdentifier(ArtifactIdentifier.ROOT);
			// DroneMonitoringService.getInstance().registerMonitoringMessageHandler(new
			// RemoteMessageHandler(), id);
			RemoteMessageHandler handler = new RemoteMessageHandler();
			sevice.registerMonitoringMessageHandler(handler, id);
			
			//ProcessorManager.getInstance().initListener();
			
			
	
	//		new SeparationDistanceChecker();
			
			

		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
