package edu.nd.dronology.ui.vaadin.activeflights;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;

import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;

import edu.nd.dronology.services.core.info.MissionInfo;
import edu.nd.dronology.services.core.remote.IMissionPlanningRemoteService;
import edu.nd.dronology.ui.vaadin.start.MyUI;

public class MissionHandler {

	//private MissionUploader receiver;

	public MissionHandler() {
		//receiver = new MissionUploader();
	}

	public void executeMission() {
		// System.out.println("UPLOAD!!");
		try {
			// Upload upload = new Upload("Upload Image Here", receiver);
			// upload.setButtonCaption("Select");
			//System.out.println("STRING:" + missionContent);
			IMissionPlanningRemoteService missionService = (IMissionPlanningRemoteService) MyUI.getProvider()
					.getRemoteManager().getService(IMissionPlanningRemoteService.class);

			missionService.executeMissionPlan(missionContent);
		} catch (Exception e) {
			MyUI.setConnected(false);
			e.printStackTrace();
		}

	}
/*
	public MissionUploader getReceiver() {
		return receiver;
	}
*/	
	public void setMissionContent(MissionInfo info) {
		missionContent = info;
	}
	

	// Show uploaded file in this placeholder
	MissionInfo missionContent;

	// Implement both receiver that saves upload in a file and
	// listener for successful upload
	
	//class MissionUploader implements Receiver, SucceededListener {
		/**
		 * 
		 */
	/*
		private static final long serialVersionUID = -533969977302422170L;
		public File file;

		@Override
		public OutputStream receiveUpload(String filename, String mimeType) {
			System.out.println("UPLOAD!!!!");
			FileOutputStream fos = null; // Output stream to write to
			try {
				System.out.println(filename);

				// file = new File("c:\\tmp\\uploads\\" + filename);
				file = File.createTempFile("abc", "tmp");
				file.createNewFile();
				fos = new FileOutputStream(file);
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			// ...
			return fos;
		}

		
		@Override
		public void uploadSucceeded(SucceededEvent event) {
			try {
				missionContent = readFile(file.getAbsolutePath());
				executeMission();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public String readFile(String absolutePath) {
			StringBuilder contents = new StringBuilder();
			BufferedReader buf = getBufferedFileReader(absolutePath);
			String line;
			try {
				while (buf != null && (line = buf.readLine()) != null) {
					contents.append(line + System.lineSeparator());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return contents.toString();
		}

		public BufferedReader getBufferedFileReader(String absolutePath) {
			FileReader fr = null;
			BufferedReader buf = null;
			try {
				String path = absolutePath;
				fr = new FileReader(path);
				buf = new BufferedReader(fr);
			} catch (FileNotFoundException e) {
				// OGGER.Lwarn("Could not read file: " + absolutePath);
				e.printStackTrace();

			}
			return buf;
		}
		
	};
	*/
}

