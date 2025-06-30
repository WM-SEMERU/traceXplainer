package edu.nd.dronology.core.vehicle.internal;

import java.util.Random;

import edu.nd.dronology.core.CoordinateChange;
import edu.nd.dronology.core.DronologyConstants;
import edu.nd.dronology.core.IUAVPropertyUpdateNotifier;
import edu.nd.dronology.core.collisionavoidance.DroneSnapshotInternal;
import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.core.exceptions.DroneException;
import edu.nd.dronology.core.exceptions.FlightZoneException;
import edu.nd.dronology.core.vehicle.AbstractDrone;
import edu.nd.dronology.core.vehicle.DroneFlightStateManager.FlightMode;
import edu.nd.dronology.core.vehicle.IDrone;
import edu.nd.dronology.core.vehicle.IDroneCommandHandler;
import edu.nd.dronology.core.vehicle.commands.AbstractDroneCommand;
import edu.nd.dronology.core.vehicle.commands.GoToCommand;
import edu.nd.dronology.core.vehicle.commands.LandCommand;
import edu.nd.dronology.core.vehicle.commands.SetGroundSpeedCommand;
import edu.nd.dronology.core.vehicle.commands.SetVelocityCommand;
import edu.nd.dronology.core.vehicle.commands.TakeoffCommand;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Representation of a physical UAV which either interacts with real hardware or the SITL simulated UAV.
 * 
 * @author Jane Cleland-Huang
 * 
 */
public class PhysicalDrone extends AbstractDrone implements IDrone, IUAVPropertyUpdateNotifier {

	private static final ILogger LOGGER = LoggerProvider.getLogger(PhysicalDrone.class);

	private IDroneCommandHandler baseStation;
	private String droneID;
	private LlaCoordinate currentTarget;
	private AbstractDroneCommand lastCommand = null;
	private volatile DroneSnapshotInternal latestDroneSnapshot;

	public PhysicalDrone(String drnName, IDroneCommandHandler baseStation) {
		super(drnName);
		this.baseStation = baseStation;
		currentTarget = new LlaCoordinate(0, 0, 0);
		try {
			droneID = drnName;
			baseStation.setStatusCallbackNotifier(droneID, this);
			droneStatus.setGroundstationId(baseStation.getHandlerId());
		} catch (Exception e) {
			LOGGER.error(e);
		}
	}

	@Override
	public double getLatitude() {
		return getCoordinates().getLatitude();
	}

	@Override
	public double getLongitude() {
		return getCoordinates().getLongitude();
	}

	@Override
	public double getAltitude() {
		return getCoordinates().getAltitude();
	}

	Random r = new Random();

	@Override
	public void flyTo(LlaCoordinate targetCoordinates, Double speed) {
		if (targetCoordinates != currentTarget) {
			// TODO: add some time limit for refreshing the information in case it didn't
			// properly get sent
			currentTarget = targetCoordinates;
			try { 
				LOGGER.missionInfo(droneID + " Flying to new waypoint " + targetCoordinates);
				sendCommand(new GoToCommand(droneID, targetCoordinates));

				if (speed != null && speed > 0) {
					sendCommand(new SetGroundSpeedCommand(droneID, speed));
				}
			} catch (DroneException e) {
				LOGGER.error(e);
			} 
		}

	}

	@Override
	public LlaCoordinate getCoordinates() {
		return droneStatus.getCoordinates();
	}

	@Override
	public void land() throws FlightZoneException {
		try {
			sendCommand(new LandCommand(droneID, LandCommand.MODE_LAND));
		} catch (DroneException e) {
			throw new FlightZoneException(e);
		}
	}

	@Override
	public void takeOff(double altitude) throws FlightZoneException {
		try {
			baseStation.sendCommand(new TakeoffCommand(droneID, altitude));
		} catch (DroneException e) {
			throw new FlightZoneException(e);
		}
	}

	@Override
	public double getBatteryStatus() {
		return droneStatus.getBatteryLevel();
	}

	@Override
	public boolean move(double i) {
		// update data from the server
		// TODO: this might not necessarily be the best place to update this
		// baseStation.getIncomingData();
		return !isDestinationReached(0);
		// TODO Auto-generated method stub

	}

	@Override
	public void setVoltageCheckPoint() {
		// TODO Auto-generated method stub

	}

	@Override
	@CoordinateChange
	public boolean isDestinationReached(int i) {
		double distancetotarget = Math.abs(currentPosition.distance(currentTarget));
		LOGGER.trace(droneID + " - Distance to target:" + distancetotarget);
		return distancetotarget < DronologyConstants.THRESHOLD_WAYPOINT_DISTANCE;
	}

	@Override
	public void update(LlaCoordinate location, double batteryLevel, double speed, Vector3D velocity, Vector3D attitude) {
		updateCoordinates(location);
		updateVelocity(speed);
		updateBatteryLevel(batteryLevel);
		updateCollisionAvoidance(location, velocity, attitude);

		super.setChanged();
		super.notifyObservers();
	}

	@Override
	public void updateCoordinates(LlaCoordinate location) {
		// LOGGER.info("Coordinates updated");
		super.setCoordinates(location);

	}

	@Override
	public void updateDroneState(String status) {
		LOGGER.info(status);

	}

	@Override
	public void setGroundSpeed(double speed) {
		try {
			baseStation.sendCommand(new SetGroundSpeedCommand(droneID, speed));
		} catch (DroneException e) {
			LOGGER.error(e);
		}
	}

	@Override
	public void setVelocity(double x, double y, double z) {
		try {
			baseStation.sendCommand(new SetVelocityCommand(droneID, x, y, z));
		} catch (DroneException e) {
			LOGGER.error(e);
		}
	}

	@Override
	public void updateBatteryLevel(double batteryLevel) {
		super.updateBatteryLevel(batteryLevel);
	}

	@Override
	public void updateVelocity(double velocity) {
		super.setVelocity(velocity);
	}

	@Override
	public void updateCollisionAvoidance(LlaCoordinate position, Vector3D velocity, Vector3D attitude) {
		DroneSnapshotInternal snapshot = new DroneSnapshotInternal(this.getDroneName(), position, velocity, attitude);
		this.latestDroneSnapshot = snapshot;
	}

	public DroneSnapshotInternal getLatestDroneSnapshot() {
		return latestDroneSnapshot;
	}

	@Override
	public void updateMode(String mode) {
		if (mode.equals(FlightMode.USER_CONTROLLED.toString())) {
			super.setUserControlled(); 
		}

	}

	@Override
	public void sendCommand(AbstractDroneCommand command) throws DroneException {
		synchronized (baseStation) {
			lastCommand = command;
			baseStation.sendCommand(command);
		}
	}

	@Override
	public void resendCommand() throws DroneException { 
		LOGGER.hwInfo("Resending previous command for '" + droneID + "'");
		synchronized (baseStation) {
			if (lastCommand == null) {
				LOGGER.hwFatal("No command recorded for uav '" + droneID + "'");
				throw new DroneException("no previous command found");
			}
			LOGGER.info("Resending command for '" + lastCommand + "'");
			sendCommand(lastCommand);
		}

	}
}
