package org.tdar.core.service;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.utils.activity.Activity;

/**
 * $Id$
 * 
 * The Activity Manager is a Singleton that fronts a BlockingQueue. It keeps track of http Requests, and long running activities (reindexing, bulk import, etc.)
 * that are happening on the system. This is used to help show things on the admin page and provide warnings on other pages.
 * 
 * @author Adam Brin
 * @version $Rev$
 */
public class ActivityManager {

    private static final int QUEUE_CAPACITY = 1000;

    private final static ActivityManager INSTANCE = new ActivityManager();

    private final BlockingQueue<Activity> activityQueue = new ArrayBlockingQueue<Activity>(QUEUE_CAPACITY);
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ActivityManager() {
    }

    /**
     * Get the ActivityManager
     * 
     * @return
     */
    public static ActivityManager getInstance() {
        return INSTANCE;
    }

    /**
     * Add an @link Activity to the Queue
     * 
     * @param activity
     * @return
     */
    public synchronized boolean addActivityToQueue(final Activity activity) {
        return activityQueue.offer(activity);
    }

    /**
     * Expose a copy of the Queue to those that want to read it
     * 
     * @return
     */
    public BlockingQueue<Activity> getActivityQueueClone() {
        return new ArrayBlockingQueue<>(QUEUE_CAPACITY, true, activityQueue);
    }

    /**
     * Find an @link Activity by name so it can be removed
     * 
     * @param name
     * @return
     */
    public synchronized Activity findActivity(String name) {
        for (Activity activity : activityQueue) {
            if (StringUtils.equals(activity.getName(), name)) {
                return activity;
            }
        }
        return null;
    }

    /**
     * Based on the age, cleanup items that are older than the specified time.
     * 
     * @param expirationTimeInMillis
     */
    public synchronized void cleanup(long expirationTimeInMillis) {
        Iterator<Activity> iterator = activityQueue.iterator();
        while (iterator.hasNext()) {
            Activity activity = iterator.next();
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
    public synchronized Activity getIndexingTask() {
        Activity active = null;
        Activity latest = null;
        /*
         * we could have multiple indexing activities, try reporting the one that's active before reporting that it's done
         */
        for (Activity activity : activityQueue) {
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
}
