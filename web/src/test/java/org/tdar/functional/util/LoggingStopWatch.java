package org.tdar.functional.util;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jimdevos on 4/22/15.
 */
public class LoggingStopWatch extends StopWatch {
    private Logger logger = null;
    private String name = "";

    public LoggingStopWatch() {
        this(LoggingStopWatch.class, "StopWatch", 0, 0);
    }

    public LoggingStopWatch(Class loggingContext, String stopWatchName, int splitTimeout, int stopTimeout) {
        this.logger = LoggerFactory.getLogger(loggingContext);
        this.name = stopWatchName;
        this.splitTimeout = splitTimeout;
        this.stopTimeout = splitTimeout;
    }

    // 0=no timeout
    private int splitTimeout = 0;
    private int stopTimeout = 0;

    public int getStopTimeout() {
        return stopTimeout;
    }

    public void setStopTimeout(int stopTimeout) {
        this.stopTimeout = stopTimeout;
    }

    public int getSplitTimeout() {
        return splitTimeout;
    }

    public void setSplitTimeout(int splitTimeout) {
        this.splitTimeout = splitTimeout;
    }

    @Override
    public void split() {
        super.split();
        logger.trace("split:{}\t total:{}", getSplitTime(), getTime());
        if (splitTimeout > 0 && splitTimeout > getSplitTime()) {
            logger.warn("({} is slow - splitLimit:{}\t actualSplit:{})", name, getSplitTimeout(), getSplitTime());
        }

    }

    @Override
    public void stop() {
        super.stop();
        logger.trace("{} stopped:{}", name, getTime());
        if (stopTimeout > 0 && stopTimeout > getTime()) {
            logger.warn("({} is slow - limit:{}\t actual:{}", name, getStopTimeout(), getTime());
        }
    }

    @Override
    public void start() {
        super.reset(); // never throw an already-started exception
        super.start();
        logger.trace("{} started:{}", name, getStartTime());
    }

}
