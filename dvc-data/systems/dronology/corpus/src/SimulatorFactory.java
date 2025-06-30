package edu.nd.dronology.core.simulator;

import edu.nd.dronology.core.simulator.nvecsimulator.NVECSimulator;
import edu.nd.dronology.core.simulator.simplesimulator.SimpleSimulator;
import edu.nd.dronology.core.vehicle.internal.VirtualDrone;

public class SimulatorFactory {

	private static final boolean USE_SIMPLE_SIMULATOR = true;

	public static IFlightSimulator getSimulator(VirtualDrone drone) {
		if (USE_SIMPLE_SIMULATOR) {
			return new SimpleSimulator(drone);
		} else {
			return new NVECSimulator(drone);
		}
	}

}
