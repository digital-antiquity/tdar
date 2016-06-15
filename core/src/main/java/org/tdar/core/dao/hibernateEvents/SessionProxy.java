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
    private final BlockingQueue<Integer> sessionIgnoreQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
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
        logger.trace("register session: {}", sessionId);
        sessionQueue.add(sessionId);
    }

    public boolean isSessionManaged(Session session) {
        return sessionQueue.contains(session.hashCode());
    }
    
    public boolean ignoreSession(Session session) {
        return sessionIgnoreQueue.contains(session.hashCode());
    }

    public void registerSessionClose(Integer sessionId, boolean isReadOnly) {
        logger.trace("register sessionClosed: {}", sessionId);
        sessionQueue.remove(sessionId);
        sessionIgnoreQueue.remove(sessionId);
        for (EventListener listener : listeners) {
        	if (isReadOnly) {
        		listener.clear(sessionId);
        	} else {
        		listener.flush(sessionId);
        	}
        }
    }

	public void flushAll() {
		for (Integer id : sessionQueue) {
	        for (EventListener listener : listeners) {
	            listener.flush(id);
	        }
		}
		sessionQueue.clear();
		
	}

	public void registerSessionCancel(Integer sessionId) {
        logger.trace("register sessionCancel: {}", sessionId);
        sessionQueue.remove(sessionId);
        sessionIgnoreQueue.remove(sessionId);
        for (EventListener listener : listeners) {
        		listener.clear(sessionId);
        }
		
	}

    public void registerIgnoreSession(Integer sessionId) {
        sessionIgnoreQueue.add(sessionId);
        registerSession(sessionId);
        
    }
}
