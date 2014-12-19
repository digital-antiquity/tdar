package org.tdar.utils.activity;

import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ActivityMonitor implements Serializable {

    private static final long serialVersionUID = 1698345026238694374L;
    private final int MAX_QUEUE_SIZE = 10;
    private static ActivityMonitor INSTANCE = new ActivityMonitor();

    // This may be the wrong type of object -- this will prune based on length, but really we want to
    // prune based on age... or some other thing so we can capture things like the reindexing
    // or another task that takes a long time
    private ArrayBlockingQueue<Activity> queue = new ArrayBlockingQueue<Activity>(MAX_QUEUE_SIZE);

    public static ActivityMonitor getInstance() {
        return INSTANCE;
    }

    public void addActivity(Activity activity) {
        getQueue().add(activity);
    }

    @XmlElementWrapper(name = "activities")
    @XmlElement(name = "activity")
    public ArrayBlockingQueue<Activity> getQueue() {
        return queue;
    }

    public void setQueue(ArrayBlockingQueue<Activity> queue) {
        this.queue = queue;
    }

}
