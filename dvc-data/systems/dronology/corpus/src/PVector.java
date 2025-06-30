package edu.nd.dronology.core.coordinate;

/**
 * A position on Earth, represented as an X, Y and Z coordinate in an
 * Earth centered Earth fixed reference frame.
 *
 * The origin of this frame is at the center of mass of the Earth. The X-axis
 * points towards where the equator meets the prime meridean (latitude 0,
 * longitude 0). The Z-axis points towards the geographic north pole (latitude
 * 90, longitude undefined). The Y-axis can be derived using right hand rule and
 * points towards latitude 0, longitude 90. All units are in meters.
 * 
 * @author Michael Murphy 
 * 
 */ 
public class PVector  extends AbstractPosition {
	private double x;
	private double y;
	private double z;

	/**
	 * @param x
	 *            the distance in meters along the axis that points from the
	 *            center of mass of the Earth towards where the equator meets
	 *            the prime meridean (latitude 0, longitude 0)
	 * @param y
	 *            the distance in meters along the axis that points from the
	 *            center of mass of the Earth towards latitude 0, longitude 90
	 * @param z
	 *            the distance in meters along the axis that points from the
	 *            center of mass of the Earth towards the geographic north pole
	 *            (latitude 90, longitude undefined)
	 */
	public PVector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	/**
	 * @return the z
	 */
	public double getZ() {
		return z;
	}

	/**
	 * @return the n-vector plus altitude for this position
	 */
	@Override
	public NVector toNVector() {
		/*
		 * The formula this code is based on can be found in a journal article
		 * called: "A Non-singular Horizontal Position Representation" by
		 * Kenneth Gade. You can find it at https://goo.gl/iCqdCn (see equation
		 * 23 in Appendix B.2.)
		 * 
		 * Note: equation 23 is unconventional as it swaps the z component with
		 * x component. This code follows the more common convention and returns
		 * z and x to their proper place
		 */
		double a = NVector.SEMI_MAJOR;
		double b = NVector.SEMI_MINOR;
		double e = Math.sqrt(1 - (b * b) / (a * a));
		double q = (1.0 - e * e) / (a * a) * this.z * this.z;
		double p = (this.y * this.y + this.x * this.x) / (a * a);
		double r = (p + q - Math.pow(e, 4.0)) / 6.0;
		double s = (Math.pow(e, 4) * p * q) / (4 * Math.pow(r, 3));
		double t = Math.pow((1.0 + s + Math.sqrt(s * (2.0 + s))), 1.0 / 3.0);
		double u = r * (1.0 + t + 1.0 / t);
		double v = Math.sqrt(u * u + Math.pow(e, 4.0) * q);
		double w = e * e * (u + v - q) / (2 * v);
		double k = Math.sqrt(u + v + w * w) - w;
		double d = (k * Math.sqrt(this.y * this.y + this.x * this.x)) / (k + e * e);
		double f = 1.0 / (Math.sqrt(d * d + this.z * this.z));
		double f2 = k / (k + e * e);
		double x = f * f2 * this.x;
		double y = f * f2 * this.y;
		double z = f * this.z;
		double alt = (k + e * e - 1) / k * Math.sqrt(d * d + this.z * this.z);

		return new NVector(x, y, z, alt);
	}

	/**
	 * @return the latitude, longitude, and altitude for this position
	 */
	@Override
	public LlaCoordinate toLlaCoordinate() {
		return toNVector().toLlaCoordinate();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(z);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public String toString() {
		return String.format("PVector(%f, %f, %f)", this.x, this.y, this.z);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PVector))
			return false;
		PVector other = (PVector) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))
			return false;
		return true;
	}

	@Override
	public PVector toPVector() {
		return this;
	}
}
