package org.tdar.core.dao;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.event.spi.AbstractEvent;
import org.hibernate.event.spi.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.dao.hibernateEvents.EventListener;
import org.tdar.core.dao.hibernateEvents.SessionProxy;

import com.google.common.base.Objects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public abstract class AbstractEventListener<C> implements EventListener {

    private static final SessionProxy EVENT_PROXY = SessionProxy.getInstance();
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private Cache<Session, Set<C>> idChangeMap = CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(5, TimeUnit.MINUTES).weakKeys().removalListener(new EventRemovalListener()).build();
    private String name = "";

    public AbstractEventListener(String name) {
        this.name = name;
        EVENT_PROXY.registerEventListener(this);
    }

    protected void process(Object obj) {

    }

    protected void flush(AbstractEvent event) {
        EventSource session = event.getSession();
        if (EVENT_PROXY.isSessionManaged(session)) {
            logger.trace("skipping session managed");
            return;
        }

        flush(session.hashCode());
    }

    @Override
    public void flush(Integer sessionId) {
        Session session = null;
        for (Session sess : idChangeMap.asMap().keySet()) {
            if (Objects.equal(sessionId.intValue(), sess.hashCode())) {
                session = sess;
                flushInternal(session);
//                break;
            }
        }
        if (session == null) {
            logger.trace("session is null for id: {}", sessionId);
            return;
        }
    }

    @Override
    public void clear(Integer sessionId) {
        Session session = null;
        for (Session sess : idChangeMap.asMap().keySet()) {
            if (Objects.equal(sessionId.intValue(), sess.hashCode())) {
                session = sess;
                idChangeMap.getIfPresent(sess).clear();
            }
        }
        if (session == null) {
            logger.trace("session is null for id: {}", sessionId);
            return;
        }
    }

    private void flushInternal(Session session) {
        Set<C> set = idChangeMap.getIfPresent(session);
        if (!CollectionUtils.isEmpty(set)) {
            int counter = 0;
            for (Object obj : set) {
                try {
                	if (logger.isTraceEnabled()) {
                		logger.trace("  flush ({} - {}):{}", session.hashCode(),idChangeMap.hashCode(), obj);
                	}
                    counter++;
                    process(obj);
                } catch (Exception e) {
                    logger.error("error batch processing {}", name, e);
                }
            }
            if (counter > 0) {
            logger.debug("flushed to {} ({})", name, counter);
            }
            set.clear();
            cleanup();
        }
    }

    protected synchronized void addToSession(EventSource session, C entity) {
        if (EVENT_PROXY.ignoreSession(session)) {
            logger.trace("skipping session... manually managed");
            return;
        }
        
		if (idChangeMap.getIfPresent(session) == null) {
            idChangeMap.put(session, new HashSet<>());
        }
        if (logger.isTraceEnabled()) {
            logger.trace("adding to session ({} - {}): {}",session.hashCode(), idChangeMap.hashCode(), entity);
        }
        
        if (!session.contains(entity)) {
        	logger.trace("not on session({}): {}",session.hashCode(),entity);
        	return;
        }
        try {
	        if (session.isReadOnly(entity)) {
	        	logger.trace("session is read only ({}): {}",session.hashCode(), entity);
	        	return;
	        }
        } catch (HibernateException he) {
        	if (logger.isTraceEnabled()) {
        		logger.trace(" {}, {}", session.hashCode(), entity, he);
        	}
        }

        Set<C> ifPresent = idChangeMap.getIfPresent(session);
		ifPresent.add((C) entity);
    }

    protected void cleanup() {

    }

}
