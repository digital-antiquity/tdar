package org.tdar.core.dao.hibernateEvents;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionProxy {

    private static final int QUEUE_CAPACITY = 1000;

    private final static SessionProxy INSTANCE = new SessionProxy();

    private final BlockingQueue<Integer> sessionQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Set<EventListener> listeners = new HashSet<>();
    
    private SessionProxy() {
    }

    /**
     * Get the ActivityManager
     * 
     * @return
     */
    public static SessionProxy getInstance() {
        return INSTANCE;
    }
    
    public synchronized void registerEventListener(EventListener listener) {
        logger.debug("registering listener:" + listener);
        listeners.add(listener);
    }

    public synchronized void registerSession(Integer sessionId) {
        sessionQueue.add(sessionId);
    }

    public boolean isSessionManaged(Session session) {
        return sessionQueue.contains(session.hashCode());
    }

    public synchronized void registerSessionClose(Integer sessionId) {
        sessionQueue.remove(sessionId);
        for (EventListener listener : listeners) {
            listener.flush(sessionId);
        }
    }
}
