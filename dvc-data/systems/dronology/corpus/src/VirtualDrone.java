package edu.nd.dronology.core.vehicle.internal;

import com.google.common.util.concurrent.RateLimiter;

import edu.nd.dronology.core.collisionavoidance.DroneSnapshotInternal;
import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.core.exceptions.DroneException;
import edu.nd.dronology.core.exceptions.FlightZoneException;
import edu.nd.dronology.core.simulator.IFlightSimulator;
import edu.nd.dronology.core.simulator.SimulatorFactory;
import edu.nd.dronology.core.vehicle.AbstractDrone;
import edu.nd.dronology.core.vehicle.IDrone;
import edu.nd.dronology.core.vehicle.commands.AbstractDroneCommand;
import edu.nd.dronology.util.NullUtil;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Representation of a virtual UAV which is controlled by the internal simulator.
 * 
 * @author Jane Cleland-Huang
 * 
 */ 
public class VirtualDrone extends AbstractDrone implements IDrone {

	private static final ILogger LOGGER = LoggerProvider.getLogger(VirtualDrone.class);
	IFlightSimulator simulator;

	/**
	 * Constructs drone without specifying its current position. This will be used by the physical drone (later) where positioning status will be acquired from the drone.
	 * 
	 * @param drnName
	 */
	public VirtualDrone(String drnName) {
		super(drnName);
		simulator = SimulatorFactory.getSimulator(this);
	} 

	@Override 
	public void takeOff(double targetAltitude) throws FlightZoneException {
		simulator.startBatteryDrain();
		droneStatus.updateBatteryLevel(simulator.getVoltage()); // Need more
		super.setCoordinates(droneStatus.getLatitude(), droneStatus.getLongitude(), targetAltitude);
		try {
			Thread.sleep(new Double(targetAltitude).intValue() * 100); // Simulates
		} catch (InterruptedException e) {
			LOGGER.error(e);
		}
	}

	@Override
	public void flyTo(LlaCoordinate targetCoordinates, Double speed) {
		NullUtil.checkNull(targetCoordinates);
		// LOGGER.info("Flying to: "+ targetCoordinates.toString());
		simulator.setFlightPath(currentPosition, targetCoordinates);
	}

	@Override
	public void land() throws FlightZoneException {
		try { 
			Thread.sleep(1500);
			simulator.checkPoint();
			simulator.stopBatteryDrain();

		} catch (Throwable e) {
			LOGGER.error(e);
		}
	}

	@Override
	public double getBatteryStatus() {
		droneStatus.updateBatteryLevel(simulator.getVoltage());
		return simulator.getVoltage();
	}

	RateLimiter limiter = RateLimiter.create(5);

	@Override
	public boolean move(double i) { // ALSO NEEDS THINKING ABOUT FOR non-VIRTUAL
		getBatteryStatus();
		// limiter.acquire();
		boolean moveStatus = simulator.move(2);
		droneStatus.updateCoordinates(getLatitude(), getLongitude(), getAltitude());

		// DroneCollectionStatus.getInstance().testStatus();
		return moveStatus;
	}

	@Override
	public void setVoltageCheckPoint() {
		simulator.checkPoint();

	}

	@Override
	public boolean isDestinationReached(int distanceMovedPerTimeStep) {
		return simulator.isDestinationReached(distanceMovedPerTimeStep);
	}

	@Override
	public void setGroundSpeed(double speed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVelocity(double x, double y, double z) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendCommand(AbstractDroneCommand command) throws DroneException {
		// TODO Auto-generated method stub

	}

	@Override
	public void resendCommand() {
		// TODO Auto-generated method stub
	}

	@Override
	public DroneSnapshotInternal getLatestDroneSnapshot() {
		//todo get velocity and attitude values
		Vector3D velocity = new Vector3D(0, 0, 0);
		Vector3D attitude = new Vector3D(0, 0, 0);
		return new DroneSnapshotInternal(this.droneName, droneStatus.getCoordinates(), velocity, attitude);
	}
}
