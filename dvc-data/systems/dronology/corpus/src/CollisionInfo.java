package edu.nd.dronology.monitoring.simplechecker.checker;

import java.io.Serializable;

public class CollisionInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2177691764314219425L;
	private final String uavid1;
	private final String uavid2;
	private final double distance;

	public CollisionInfo(String uavid1, String uavid2, double distance) {
		this.uavid1 = uavid1;
		this.uavid2 = uavid2;
		this.distance = distance;
	}

}
