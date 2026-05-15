package edu.nd.dronology.core.coordinate;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * A terrestrial position in WGS-84. this class implements utility methods for
 * subclasses.
 * 
 * @author Michael Murphy
 *
 */ 
public abstract class AbstractPosition {

	/**
	 * Convert this position if necessary to an NVector.
	 * 
	 * @return a terrestrial position defined an NVector and an altitude.
	 */
	public abstract NVector toNVector();

	/**
	 * Convert this position if necessary to a PVector.
	 * 
	 * @return a terrestrial position defined an X, Y and Z coordinate.
	 */
	public abstract PVector toPVector();

	/**
	 * Convert this position if necessary to an LlaCoordinate.
	 * 
	 * @return a terrestrial position defined by a latitude, longitude, and
	 *         altitude.
	 */
	public abstract LlaCoordinate toLlaCoordinate();

	/**
	 * Calculates the distance from this position to other position. This is the
	 * distance a laser bean would travel to reach the other point.
	 * 
	 * @param other
	 *            the position of the point to calculate the distance to.
	 * @return the distance to the other position in meters
	 */
	public double distance(AbstractPosition other) {
		return NVector.laserDistance(this.toNVector(), other.toNVector());
	}

	/**
	 * Calculates the distance a drone would realistically travel to get from
	 * this position to the other position.
	 * 
	 * Warning! this code is slow. The time it takes to run is proportional to the
	 * distance from this to other.
	 * 
	 * @param other
	 *            the position of the point to calculate the distance to.
	 * @return the distance a drone would need to travel to get to the other
	 *         position in meters
	 */
	public double travelDistance(AbstractPosition other) {
		return NVector.travelDistance(this.toNVector(), other.toNVector());
	}

	/**
	 * Calculate the rotation matrix representation of this position. This
	 * rotation matrix can take displacement vectors in ECEF coordinates and
	 * rotate them into NED coordinates at this position.
	 * 
	 * This position cannot be at the poles as north and east directions don't
	 * make sense there.
	 * 
	 * This is the matrix inverse of equation 11 in <a href=
	 * "http://www.navlab.net/Publications/A_Nonsingular_Horizontal_Position_Representation.pdf">this
	 * paper.</a>
	 * 
	 * 
	 * @return a 3x3 rotation matrix where the rows can be interpreted as
	 *         unit vectors pointing in the north, east and down directions
	 *         respectively.
	 */
	public RealMatrix toRotMatrix() {
		NVector n = this.toNVector();
		Vector3D nvec = new Vector3D(n.getX(), n.getY(), n.getZ());
		Vector3D z = new Vector3D(0, 0, 1);
		Vector3D east = z.crossProduct(nvec).normalize();
		Vector3D north = nvec.crossProduct(east).normalize();
		Vector3D down = nvec.negate();
		double[][] data = { north.toArray(), east.toArray(), down.toArray() };
		return new Array2DRowRealMatrix(data);
	}

	/**
	 * Calculates the number of meters North, East and down (NED coordinates)
	 * from this position to another global position.
	 * 
	 * @param other
	 *            the terrestrial position to transform into NED coordinates
	 *            using this position as the origin
	 * @return the NED coordinates as a vector with 3 elements where the first
	 *         (0th) element is the number of meters north, the second element
	 *         is the number of meters east and the third element is the number
	 *         of meters down.
	 */
	public Vector3D findNed(AbstractPosition other) {
		Vector3D self = makeVector3D(this);
		Vector3D otherVec = makeVector3D(other);
		Vector3D displacement = otherVec.subtract(self);
		RealMatrix tmp = new Array2DRowRealMatrix(displacement.toArray());
		return new Vector3D(this.toRotMatrix().multiply(tmp).getColumn(0));
	}

	/**
	 * Calculates the latitude, longitude and altitude of a relative position
	 * given as the number of meters North, East, and down from this position.
	 * 
	 * @param ned
	 *            a vector with three elements where the first is the number of
	 *            meters north, the second is the number of meters east, and the
	 *            third is the number of meters down.
	 * @return the latitude longitude and altitude of the other position
	 */
	public LlaCoordinate findLla(Vector3D ned) {
		Vector3D self = makeVector3D(this);
		RealMatrix tmp = new Array2DRowRealMatrix(ned.toArray());
		Vector3D d = new Vector3D(this.toRotMatrix().transpose().multiply(tmp).getColumn(0));
		Vector3D p = self.add(d);
		return new PVector(p.getX(), p.getY(), p.getZ()).toLlaCoordinate();
	}

	private static Vector3D makeVector3D(AbstractPosition pos) {
		PVector p = pos.toPVector();
		return new Vector3D(p.getX(), p.getY(), p.getZ());
	}

}
