package edu.nd.dronology.core.monitoring;

import java.util.concurrent.BlockingQueue;

public interface IMonitoringDataHandler extends Runnable {

	void setQueue(BlockingQueue<IMonitorableMessage> queue);

}
