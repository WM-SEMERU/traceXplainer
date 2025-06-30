package edu.nd.dronology.core.simulator.simplesimulator;

/**
 * Voltage simulator.  For every minute of flight, the battery decreases by 0.25 volts.
 * @author Jane Cleland-Huang
 *
 */
public class DroneVoltageSimulator {
	
	private double voltage;
	private enum BatteryState {CHARGING, DEPLETING, STABLE}
	private BatteryState batteryState = BatteryState.STABLE;
	private static final double SIMULATOR_BATTERY_DRAIN_VOLTS = 0.25;
   
	private long checkPointTime;
	
	public DroneVoltageSimulator(){
		voltage = 15.0;
		checkPointTime = 0;
	}
	
	public void rechargeBattery(){
		batteryState = BatteryState.CHARGING;
		voltage = 15.0;
		batteryState = BatteryState.STABLE;
	}
	
	public void startBatteryDrain(){
		batteryState = BatteryState.DEPLETING;
		checkPointTime = System.currentTimeMillis();
	}
	
	public void stopBatteryDrain(){
		checkPoint();
		batteryState = BatteryState.STABLE;		
	}
	 
	public void checkPoint(){
		if(batteryState == BatteryState.DEPLETING){
			long timeSinceLastCheckPoint = System.currentTimeMillis() - checkPointTime;
			if (timeSinceLastCheckPoint > 5000) {
				checkPointTime = System.currentTimeMillis(); // Reset checkPoint time
			
				// Volts drained per second * number of elapsed seconds
				double voltageDrain = SIMULATOR_BATTERY_DRAIN_VOLTS/60 * (timeSinceLastCheckPoint/1000);
				voltage = voltage - voltageDrain;
			}
		} 
	}
	
	public double getVoltage(){
		return voltage;
	}
}
