package edu.nd.dronology.core.simulator.simplesimulator;

import edu.nd.dronology.core.coordinate.LlaCoordinate;
import edu.nd.dronology.core.simulator.IFlightSimulator;
import edu.nd.dronology.core.vehicle.internal.VirtualDrone;

/**
 * Simple simulator implementation containing a flight simulator and a battery simulator.
 */
public class SimpleSimulator implements IFlightSimulator {

	FlightSimulator flightSimulator; 
	DroneVoltageSimulator voltageSimulator;

	public SimpleSimulator(VirtualDrone drone) {
		flightSimulator = new FlightSimulator(drone);
		voltageSimulator = new DroneVoltageSimulator();
	}

	@Override
	public void startBatteryDrain() {
		voltageSimulator.startBatteryDrain();

	}

	@Override
	public double getVoltage() {
		return voltageSimulator.getVoltage();
	}

	@Override
	public void setFlightPath(LlaCoordinate currentPosition, LlaCoordinate targetCoordinates) {
		flightSimulator.setFlightPath(currentPosition, targetCoordinates);

	}

	@Override
	public void checkPoint() {
		voltageSimulator.checkPoint();

	}

	@Override
	public boolean isDestinationReached(double distanceMovedPerTimeStep) {
		return flightSimulator.isDestinationReached(distanceMovedPerTimeStep);
	}

	@Override
	public void stopBatteryDrain() {
		voltageSimulator.startBatteryDrain();

	}

	@Override
	public boolean move(double step) {
		return flightSimulator.move(step);
	}

}
