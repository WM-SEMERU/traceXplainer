package edu.nd.dronology.gstation.connector.connect;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import edu.nd.dronology.gstation.connector.GroundstationConnector;
import edu.nd.dronology.gstation.connector.service.connector.DroneConnectorService;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

/**
 * The {@link IncommingGroundstationConnectionServer} listens on a predefined port for new connections from GCS.
 * 
 * 
 * @author Michael Vierhauser
 */
 
public class IncommingGroundstationConnectionServer implements Runnable {
	private int port = 1234;
	private ServerSocket serverSocket;
	private boolean cont = true;
	private static final ILogger LOGGER = LoggerProvider.getLogger(IncommingGroundstationConnectionServer.class);
	private String URL = "127.0.0.1";

	public IncommingGroundstationConnectionServer() {

	}

	@Override
	public void run() {

		serverSocket = null;
		try {
			serverSocket = new ServerSocket(port, 3000);
			// server.setReuseAddress(true);

			LOGGER.info("Incomming-Groundstation Connection Server listening on port: " + port);
			// server.setSoTimeout(1000);

			while (cont) {
				Socket socket = null;
				try {
					socket = serverSocket.accept();
					GroundstationConnector handler = new GroundstationConnector(this, socket);
					DroneConnectorService.getInstance().handleConnection(handler);

				} catch (SocketException e) {
					LOGGER.info("Socket was closed!");
				} catch (IOException e) {
					LOGGER.error(e);

				}
			}
		} catch (Exception e) {
			LOGGER.error(e);
		} finally {
			try {
				if (serverSocket != null) {
					serverSocket.close();
				}
			} catch (IOException e) {
				LOGGER.error(e);
			}
		}

	}


}
