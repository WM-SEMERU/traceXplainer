package edu.nd.dronology.core.monitoring;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import edu.nd.dronology.util.NullUtil;

/**
 * A structured Identifier. Each object holds an id part and refers to child IDs
 * which in turn<br/> 
 * hold again one id part, etc, which altogether form the whole identifier.
 * <p/>
 * 
 * @author Michael Vierhauser
 * 
 */
public class ArtifactIdentifier<TYPE> implements Serializable {

	private static final long serialVersionUID = 9173775383058285758L;
	private static final String SEPARATOR = "::";
	public static final String ROOT = "DRONOLOGY";

	// Never null
	private final String id;
	// May be null, when this is the last part of an identifier.
	public static AtomicInteger COUNTER = new AtomicInteger(0);
 
	private final ArtifactIdentifier child;
	private Set<TYPE> attacheditems = new HashSet<>();

	/**
	 * 
	 * @param ids
	 *            The ids that will be mapped to root and child ArtifactIdentifier
	 */
	public ArtifactIdentifier(String... ids) {
		if (ids.length < 1) {
			throw new IllegalArgumentException("At least 1 id is required");
		}
		COUNTER.addAndGet(1);
		this.id = ids[0];
		NullUtil.checkNull(id);
		if (ids.length > 1) {
			child = new ArtifactIdentifier(Arrays.copyOfRange(ids, 1, ids.length));
		} else {
			child = null;
		}
	}

	/**
	 * 
	 * @return The id of the parent ArtifactIdentifier
	 */
	public synchronized String getId() {
		return id;
	}

	/**
	 * 
	 * @return The child ArtifactIdentifier
	 */
	public synchronized ArtifactIdentifier getChild() {
		return child;
	}

	/**
	 * Returns all artifact identifier concatenated (separated by ::)
	 */
	@Override
	public synchronized String toString() {
		if (child == null) {
			return id;
		}
		return String.format("%s" + SEPARATOR + "%s", id, child.toString());
	}

	@Override
	public synchronized int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((child == null) ? 0 : child.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public synchronized boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ArtifactIdentifier other = (ArtifactIdentifier) obj;
		if (child == null) {
			if (other.child != null) {
				return false;
			}
		} else if (!child.equals(other.child)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			COUNTER.addAndGet(-1);
		} finally {
			super.finalize();
		}
	}

	public Collection<TYPE> getAttachedItems() {
		return Collections.unmodifiableCollection(attacheditems);
	}

	public void attachItem(TYPE item) {
		attacheditems.add(item);

	}

	public void removeAttachedItem(TYPE handler) {
		attacheditems.remove(handler);

	}

	public void attachItems(Collection<TYPE> toAdd) {
		attacheditems.addAll(toAdd);

	}
}
