package edu.nd.dronology.services.extensions.missionplanning.sync;

/**
 * Synchronization IDs for predefined, built-in {@link SynchronizationPoint} .
 * 
 * @author Michael Vierhauser 
 * 
 * 
 *
 */
public class SyncConstants {
 
	public static final String LANDING_ASC_REACHED = "SP-Landing-AscentTargetReached";
	public static final String LANDING_LONLAT_REACHED = "SP-Landing-LonLatReached";
	public static final String LANDING_HOME_REACHED = "SP-Landing-HomeReached";

	public static final String TAKEOFF_ASC_REACHED = "SP-TakeOff-AscentTargetReached";
	public static final String TAKEOFF_LATLON_REACHED = "SP-TakeOff-LonLatReached";
	public static final String TAKEOFF_WP_REACHED = "SP-TakeOff-FirstWayPointReached";

}
