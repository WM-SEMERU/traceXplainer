package edu.nd.dronology.core.simulator.nvecsimulator;

import edu.nd.dronology.core.coordinate.NVector;

/**
 * 
 * @author Michael Murphy
 *
 */
public class NvecInterpolator {
	public static final double TOLERANCE = 0.001; // 1 Millimeter

	private static NVector mid(NVector a, NVector b) {
		double x = average(a.getX(), b.getX());
		double y = average(a.getY(), b.getY());
		double z = average(a.getZ(), b.getZ());
		double alt = average(a.getAltitude(), b.getAltitude());
		return new NVector(x, y, z, alt);
	}
 
	private static double average(double a, double b) {
		return (a + b) / 2.0;
	}

	/**
	 * Uses the bisection method to iteratively hone in on the nvector that is
	 * metersToTravel distance away from the current n-vector along the path
	 * that goes from current to target. A path can be thought of as the set of
	 * NVectors you can create by interpolating between current and target. This
	 * code takes advantage of this to find the NVector on this path that is the
	 * right distance away from the current distance.
	 * 
	 * @param current
	 *            drone current position
	 * @param target
	 *            drone target position
	 * @param metersToTravel
	 *            the distance the drone should travel along the path from
	 *            current to target
	 * @return the nvector taht is meters distance away from current along the
	 *         path that goes from current to target
	 */
	public static NVector move(NVector current, NVector target, double metersToTravel) {
		// a negative value for metersToTravel causes an infinite loop
		if (metersToTravel < 0.0) {
			throw new IllegalArgumentException();
		}
		if (current.distance(target) < metersToTravel) {
			return target;
		}
		NVector lowBall = current;
		NVector highBall = target;
		NVector nextGuess = mid(lowBall, highBall);
		double guessDistance = current.distance(nextGuess);
		while (Math.abs(guessDistance - metersToTravel) > TOLERANCE) {
			if (guessDistance > metersToTravel) {
				highBall = nextGuess;
			} else {
				lowBall = nextGuess;
			}
			nextGuess = mid(lowBall, highBall);
			guessDistance = current.distance(nextGuess);
		}
		return nextGuess;
	}

}
