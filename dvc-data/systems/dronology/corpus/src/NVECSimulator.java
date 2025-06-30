package edu.nd.dronology.core.simulator.nvecsimulator;

import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.core.coordinate.NVector;
import edu.nd.dronology.core.simulator.IFlightSimulator;
import edu.nd.dronology.core.simulator.simplesimulator.DroneVoltageSimulator;
import edu.nd.dronology.core.simulator.simplesimulator.FlightSimulator;
import edu.nd.dronology.core.vehicle.internal.VirtualDrone;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

public class NVECSimulator implements IFlightSimulator {
	private static final ILogger LOGGER = LoggerProvider.getLogger(FlightSimulator.class);
	private VirtualDrone drone;
	private NVector currentPosition;
	private NVector targetPosition;
	private DroneVoltageSimulator voltageSimulator;

	public NVECSimulator(VirtualDrone drone) {
		this.drone = drone;
		voltageSimulator = new DroneVoltageSimulator();
	}

	@Override
	public boolean isDestinationReached(double distanceMovedPerTimeStep) {
		return NvecInterpolator.move(currentPosition, targetPosition, distanceMovedPerTimeStep).equals(targetPosition);
	}

	@Override
	public boolean move(double i) {
		currentPosition = NvecInterpolator.move(currentPosition, targetPosition, i);
		drone.setCoordinates(currentPosition.toLlaCoordinate());
		LOGGER.trace("Remaining Dinstance: " + NVector.travelDistance(currentPosition, targetPosition));
		return !currentPosition.equals(targetPosition);
	}

	@Override
	public void setFlightPath(LlaCoordinate currentPosition, LlaCoordinate targetCoordinates) {
		this.currentPosition = currentPosition.toNVector();
		this.targetPosition = targetCoordinates.toNVector();

	}

	@Override
	public void startBatteryDrain() {
		voltageSimulator.startBatteryDrain();
	}

	@Override
	public void stopBatteryDrain() {
		voltageSimulator.startBatteryDrain();

	}

	@Override
	public double getVoltage() {
		return voltageSimulator.getVoltage();
	}

	@Override
	public void checkPoint() {
		voltageSimulator.checkPoint();

	}

}
