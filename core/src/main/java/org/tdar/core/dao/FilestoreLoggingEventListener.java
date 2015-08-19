package org.tdar.core.dao;

import org.hibernate.event.spi.AbstractEvent;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.PostCommitDeleteEventListener;
import org.hibernate.event.spi.PostCommitInsertEventListener;
import org.hibernate.event.spi.PostCommitUpdateEventListener;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.Persistable;
import org.tdar.utils.jaxb.XMLFilestoreLogger;

public class FilestoreLoggingEventListener implements PostInsertEventListener,
        PostUpdateEventListener, PostDeleteEventListener, PostCommitUpdateEventListener, PostCommitInsertEventListener, PostCommitDeleteEventListener {

    private static final long serialVersionUID = -2773973927518207238L;

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    XMLFilestoreLogger xmlLogger;

    public FilestoreLoggingEventListener() throws ClassNotFoundException {
        xmlLogger = new XMLFilestoreLogger();
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        logToXml(event, event.getEntity());
    }

    private void logToXml(AbstractEvent event, Object obj) {
        if (testSession(event.getSession())) {
            logger.error("trying to logToXML: {} but session is closed", obj);
            return;
        }

        if (obj == null) {
            return;
        }

        try {
            if (obj instanceof Persistable) {
                xmlLogger.logRecordXmlToFilestore((Persistable) obj);
            }
        } catch (Exception e) {
            logger.error("error ocurred when serializing to XML: {}", e.getMessage(), e);
        }
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        logToXml(event, event.getEntity());
    }

    private boolean testSession(EventSource session) {
        return session.isClosed();
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        logToXml(event, event.getEntity());
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return false;
    }

    @Override
    public void onPostUpdateCommitFailed(PostUpdateEvent event) {
        logger.error("logging to filestore failed: {}", event);
    }

    @Override
    public void onPostDeleteCommitFailed(PostDeleteEvent event) {
        logger.error("logging to filestore failed: {}", event);
    }

    @Override
    public void onPostInsertCommitFailed(PostInsertEvent event) {
        logger.error("logging to filestore failed: {}", event);
    }

}
