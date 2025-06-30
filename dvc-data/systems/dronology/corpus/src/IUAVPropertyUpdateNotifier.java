package edu.nd.dronology.core;

import edu.nd.dronology.core.coordinate.LlaCoordinate;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;


public interface IUAVPropertyUpdateNotifier {

	void update(LlaCoordinate location, double batteryLevel, double speed, Vector3D velocity, Vector3D attitude);

	void updateCoordinates(LlaCoordinate location);

	void updateDroneState(String status);

	void updateBatteryLevel(double batteryLevel);

	void updateVelocity(double velocity);

	void updateCollisionAvoidance(LlaCoordinate position, Vector3D velocity, Vector3D attitude);

	void updateMode(String mode);

}
