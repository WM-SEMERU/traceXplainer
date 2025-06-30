package edu.nd.dronology.core.monitoring;

import java.io.Serializable;

import edu.nd.dronology.core.monitoring.messages.UAVMonitorableMessage.MessageType;
import edu.nd.dronology.core.util.PreciseTimestamp;

/**
 *  Interface for all messages that shall be subscribeable via the monitoring API.
 * 
 * 
 * @author Michael Vierhauser
 *
 */
public interface IMonitorableMessage extends Serializable{

	ArtifactIdentifier<?> getIdentifier();

	String getUavid();

	String getData();

	PreciseTimestamp getTimestamp();

	MessageType getType();

}
