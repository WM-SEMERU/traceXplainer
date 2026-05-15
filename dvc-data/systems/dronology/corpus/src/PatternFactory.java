package edu.nd.dronology.services.extensions.missionplanning.patterns;

/**
 * Factory class for predefined {@link IFlightPattern}.
 * 
 * @author Michael Vierhauser
 *
 */
public class PatternFactory {

	public enum PatternType {
		COORDINATED_TAKEOFF, COORDINATED_LANDING
	}

	/**
	 * 
	 * @param type
	 *          The {@link PatternType} of the flight pattern to create.
	 * @return A flight pattern for the given pattern type.
	 * @throws An
	 * @{@link IllegalArgumentException} in case the pattern is not supported.
	 */
	public static IFlightPattern getPattern(PatternType type) {
		switch (type) {
			case COORDINATED_TAKEOFF:
				return new CoordinatedTakeoffPattern();

			case COORDINATED_LANDING:
				return new CoordinatedLandingPattern();

			default:
				throw new IllegalArgumentException("Type " + type + " not supported");
		}
	}

}
