package org.tdar.core.dao;

import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Session;
import org.hibernate.event.spi.AbstractEvent;
import org.hibernate.event.spi.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.dao.hibernateEvents.EventListener;
import org.tdar.core.dao.hibernateEvents.SessionProxy;

import com.google.common.base.Objects;

public abstract class AbstractEventListener<C> implements EventListener {

    private static final SessionProxy EVENT_PROXY = SessionProxy.getInstance();
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private WeakHashMap<Session, Set<C>> idChangeMap = new WeakHashMap<>();
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

        flush(session);
    }

    public void flush(Integer sessionId) {
        Session session = null;
        for (Session sess : idChangeMap.keySet()) {
            if (Objects.equal(sessionId.intValue(), sess.hashCode())) {
                session = sess;
                break;
            }
        }
        if (session == null) {
            logger.error("session is null for id: {}", sessionId);
            return;
        }
        flush(session);
    }

    private void flush(Session session) {
        Set<C> set = idChangeMap.get(session);
        if (!CollectionUtils.isEmpty(set)) {
            logger.debug("flush to {} ({})", name, set.size());
            for (Object obj : set) {
                try {
                    // logger.debug("fl:{}",obj);
                    if (!session.contains(obj) || session.isReadOnly(obj)) {
                        continue;
                    }
                    process(obj);
                } catch (Exception e) {
                    logger.error("error batch processing {}", name, e);
                }
            }
            set.clear();
            cleanup();
        }
    }

    protected void addToSession(EventSource session, C entity) {
        if (idChangeMap.get(session) == null) {
            idChangeMap.put(session, new HashSet<>());
        }
        idChangeMap.get(session).add((C) entity);
    }

    protected void cleanup() {

    }

}
