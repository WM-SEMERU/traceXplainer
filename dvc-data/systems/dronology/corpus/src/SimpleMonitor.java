package edu.nd.dronology.monitoring.simplechecker.monitor;

import edu.nd.dronology.core.monitoring.ArtifactIdentifier;

import edu.nd.dronology.monitoring.service.IDroneMonitoringRemoteService;
import edu.nd.dronology.monitoring.simplechecker.BaseServiceProvider;

public class SimpleMonitor {

	public static void main(String[] args) {

		MonitorDialog dialog = new MonitorDialog();
		new Thread(new Runnable() {

			@Override
			public void run() {
				dialog.main(args);
			}
		}).start();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		BaseServiceProvider provider = new BaseServiceProvider();

		try {
			System.setProperty("java.rmi.server.hostname", "localhost");

			IDroneMonitoringRemoteService sevice = (IDroneMonitoringRemoteService) provider.getRemoteManager()
					.getService(IDroneMonitoringRemoteService.class);

			ArtifactIdentifier id = new ArtifactIdentifier(ArtifactIdentifier.ROOT, "INTERNAL", "MISSION_WAYPOINT");
			ArtifactIdentifier id2 = new ArtifactIdentifier(ArtifactIdentifier.ROOT, "INTERNAL", "COLLISION");
			RemoteMessageHandler handler = new RemoteMessageHandler(dialog);
			sevice.registerMonitoringMessageHandler(handler, id);
			sevice.registerMonitoringMessageHandler(handler, id2);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
