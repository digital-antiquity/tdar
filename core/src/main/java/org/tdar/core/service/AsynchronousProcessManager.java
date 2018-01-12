package org.tdar.core.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * $Id$
 * 
 * The Activity Manager is a Singleton that fronts a BlockingQueue. It keeps track of http Requests, and long running activities (reindexing, bulk import, etc.)
 * that are happening on the system. This is used to help show things on the admin page and provide warnings on other pages.
 * 
 * @author Adam Brin
 * @version $Rev$
 */
public class AsynchronousProcessManager {

    private static final int QUEUE_CAPACITY = 1000;

    private final static AsynchronousProcessManager INSTANCE = new AsynchronousProcessManager();

    private final BlockingQueue<AsynchronousStatus> activityQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private AsynchronousProcessManager() {
    }

    /**
     * Get the ActivityManager
     * 
     * @return
     */
    public static AsynchronousProcessManager getInstance() {
        return INSTANCE;
    }

    /**
     * Add an @link Activity to the Queue
     * 
     * @param activity
     * @return
     */
    public synchronized boolean addActivityToQueue(final AsynchronousStatus status) {
        return activityQueue.offer(status);
    }

    /**
     * Expose a copy of the Queue to those that want to read it
     * 
     * @return
     */
    public BlockingQueue<AsynchronousStatus> getActivityQueueClone() {
        return new ArrayBlockingQueue<>(QUEUE_CAPACITY, true, activityQueue);
    }

    /**
     * Find an @link Activity by name so it can be removed
     * 
     * @param name
     * @return
     */
    public synchronized AsynchronousStatus findActivity(String name) {
        List<AsynchronousStatus> found = new ArrayList<>();
        for (AsynchronousStatus activity : activityQueue) {
            if (StringUtils.equals(activity.getKey(), name)) {
                if (!activity.hasEnded()) {
                    return activity;
                }
                found.add(activity);
            }
        }

        if (CollectionUtils.isEmpty(found)) {
            return null;
        }
        
        logger.debug("{}",found);
        return found.get(0);
    }

    /**
     * Based on the age, cleanup items that are older than the specified time.
     * 
     * @param expirationTimeInMillis
     */
    public synchronized void cleanup(long expirationTimeInMillis) {
        Iterator<AsynchronousStatus> iterator = activityQueue.iterator();
        while (iterator.hasNext()) {
            AsynchronousStatus activity = iterator.next();
            // if the task has ended and is older than 2 minutes, then remove it
            if (activity.hasExpired(expirationTimeInMillis)) {
                iterator.remove();
                logger.trace("removing {}", activity);
            }
        }
    }

    /**
     * Search through the Queue to find a task created by the BulkReIndexer
     * 
     * @return
     */
    public synchronized AsynchronousStatus getIndexingTask() {
        AsynchronousStatus active = null;
        AsynchronousStatus latest = null;
        /*
         * we could have multiple indexing activities, try reporting the one that's active before reporting that it's done
         */
        for (AsynchronousStatus activity : activityQueue) {
            if (activity.isIndexingActivity()) {
                latest = activity;
                if (!activity.hasEnded()) {
                    active = activity;
                }
            }
        }
        if (active != null) {
            return active;
        }
        return latest;
    }


    public synchronized void clearIndexingActivities() {
        Iterator<AsynchronousStatus> iter = activityQueue.iterator();
        while (iter.hasNext()) {
            AsynchronousStatus item = iter.next();
            if (item.isIndexingActivity()) {
                iter.remove();
            }
        }
    }
}
