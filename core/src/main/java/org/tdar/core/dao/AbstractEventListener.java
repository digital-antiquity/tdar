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
    Cache<Integer, Set<C>> idChangeMap = CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(5, TimeUnit.MINUTES).removalListener(new EventRemovalListener()).build();
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

    public void flush(Integer sessionId) {
        Integer session = null;
        for (Integer sess : idChangeMap.asMap().keySet()) {
            if (Objects.equal(sessionId.intValue(), sess)) {
                session = sess;
                break;
            }
        }
        if (session == null) {
            logger.trace("session is null for id: {}", sessionId);
            return;
        }
        flushInternal(session);
    }

    private void flushInternal(Integer session) {
        Set<C> set = idChangeMap.getIfPresent(session);
        if (!CollectionUtils.isEmpty(set)) {
            int counter = 0;
            for (Object obj : set) {
                try {
                    // logger.debug("fl:{}",obj);
//                    if (!session.contains(obj) || session.isReadOnly(obj)) {
//                        continue;
//                    }
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

    protected void addToSession(EventSource session, C entity) {
        int hashCode = session.hashCode();
		if (idChangeMap.getIfPresent(hashCode) == null) {
            idChangeMap.put(hashCode, new HashSet<>());
        }
        if (logger.isTraceEnabled()) {
            logger.trace("adding to session: {}", entity);
        }
        
        if (!session.contains(entity)) {
        	return;
        }
        try {
	        if (session.isReadOnly(entity)) {
	        	return;
	        }
        } catch (HibernateException he) {
        	return;
        }
//		logger.debug("{} [{}]",event.getSession().contains(event.getEntity()),event.getEntity());

        idChangeMap.getIfPresent(hashCode).add((C) entity);
    }

    protected void cleanup() {

    }

}
