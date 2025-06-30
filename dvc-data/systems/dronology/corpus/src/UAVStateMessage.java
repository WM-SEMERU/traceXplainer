package edu.nd.dronology.gstation.connector.messages;

import java.io.Serializable;

import edu.nd.dronology.core.coordinate.LlaCoordinate;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * State message received from the GCS for a specific UAV.
 * 
 * @author Michael Vierhauser
 *
 */
public class UAVStateMessage extends AbstractUAVMessage<Object> implements Serializable {

	private static final long serialVersionUID = -5703232763831907307L;
	public static final String MESSAGE_TYPE = "state";

	public static final transient String LOCATION = "location";
	public static final transient String ATTITUDE = "attitude";
	public static final transient String VELOCITY = "velocity";

	public static final transient String MODE = "mode";
	public static final transient String STATUS = "status";

	public static final transient String ARMED = "armed";
	public static final transient String ARMABLE = "armable";

	public static final transient String GROUNDSPEED = "groundspeed";
	public static final transient String BATTERYSTATUS = "batterystatus";

	public enum DroneMode {
		GUIDED, INIT, LAND, RTL, POSHOLD, OF_LOITER, STABILIZE, AUTO, THROW, DRIFT, FLIP, AUTOTUNE, ALT_HOLD, BRAKE, LOITER, AVOID_ADSB, POSITION, CIRCLE, SPORT, ACRO;
	}

	public enum DroneStatus {
		STANDBY, UNINIT, BOOT, CALIBRATING, ACTIVE, CRITICAL, EMERGENCY, POWEROFF, INIT;
	}

	public UAVStateMessage(String message, String groundstationid, String uavid) {
		super(message, groundstationid, uavid);
	}

	public LlaCoordinate getLocation() {
		return (LlaCoordinate) data.get(LOCATION);
	}

	public Vector3D getAttitude() {
		return (Vector3D) data.get(ATTITUDE);
	}

	public Vector3D getVelocity() {
		return (Vector3D) data.get(VELOCITY);
	}

	public boolean isArmable() {
		return (Boolean) data.get(ARMABLE);
	}

	public double getGroundspeed() {
		return (Double) data.get(GROUNDSPEED);
	}

	public DroneStatus getStatus() {
		return (DroneStatus) data.get(STATUS);
	}

	public boolean isArmed() {
		return (Boolean) data.get(ARMED);
	}

	public DroneMode getMode() {
		return (DroneMode) data.get(MODE);
	}

	public BatteryStatus getBatterystatus() {
		return (BatteryStatus) data.get(BATTERYSTATUS);
	}

	@Override
	public String toString() {
		return "armed=" + isArmed() + "| mode " + getMode() + " | Coordinate["
				+ Double.toString(getLocation().getLatitude()) + "," + Double.toString(getLocation().getLongitude()) + ","
				+ Double.toString(getLocation().getAltitude()) + "]";
	}

	public static class BatteryStatus implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -7752170927927875169L;
		private double current;
		private double voltage;
		private double level;

		public BatteryStatus(double current, double voltage, double level) {
			this.current = current;
			this.voltage = voltage;
			this.level = level;
		}

		public double getBatteryLevel() {
			return level;
		}

		public double getBatteryCurrent() {
			return current;
		}

		public double getBatteryVoltage() {
			return voltage;
		}
	}
}
