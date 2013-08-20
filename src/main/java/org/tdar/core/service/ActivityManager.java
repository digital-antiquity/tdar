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
 * FIXME: should probably convert this back into a spring managed component
 * 
 * @author Adam Brin
 * @version $Rev$
 */
public class ActivityManager {

    private final static ActivityManager INSTANCE = new ActivityManager();

    private final BlockingQueue<Activity> activityQueue = new ArrayBlockingQueue<Activity>(1000);
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ActivityManager() {
    }

    public static ActivityManager getInstance() {
        return INSTANCE;
    }

    public synchronized boolean addActivityToQueue(final Activity activity) {
        return activityQueue.offer(activity);
    }

    public BlockingQueue<Activity> getActivityQueue() {
        return activityQueue;
    }

    public synchronized Activity findActivity(String name) {
        for (Activity activity : activityQueue) {
            if (StringUtils.equals(activity.getName(), name)) {
                return activity;
            }
        }
        return null;
    }
    
    

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

    public synchronized Activity getIndexingTask() {
        Activity active = null;
        Activity latest = null;
        /*
         * we could have multiple indexing activities, try reporting the one that's active before reporting that it's done
         */
        for (Activity activity : ActivityManager.getInstance().getActivityQueue()) {
            if (activity.isIndexingActivity() ) {
                latest = activity;
                if (!activity.hasEnded())
                    active = activity;
            }
        }
        if (active != null) {
            return active;
        }
        return latest;
    }
}
