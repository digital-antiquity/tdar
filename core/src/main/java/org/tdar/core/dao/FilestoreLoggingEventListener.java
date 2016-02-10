package org.tdar.core.dao;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.FlushEntityEvent;
import org.hibernate.event.spi.FlushEntityEventListener;
import org.hibernate.event.spi.FlushEvent;
import org.hibernate.event.spi.FlushEventListener;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.hibernate.event.spi.SaveOrUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.XmlLoggable;
import org.tdar.core.dao.hibernateEvents.EventListener;
import org.tdar.utils.jaxb.XMLFilestoreLogger;

public class FilestoreLoggingEventListener extends AbstractEventListener<XmlLoggable> implements PostInsertEventListener, PostUpdateEventListener,
        PostDeleteEventListener, FlushEntityEventListener, FlushEventListener, SaveOrUpdateEventListener, EventListener {

    private static final long serialVersionUID = -2773973927518207238L;

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    XMLFilestoreLogger xmlLogger;

    public FilestoreLoggingEventListener() throws ClassNotFoundException {
        super("Filestore");
        xmlLogger = new XMLFilestoreLogger();
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        if (testSession(event.getSession())) {
            logger.error("trying to logToXML: {} but session is closed", event.getEntity());
            return;
        }
        if (event.getEntity() instanceof XmlLoggable) {
            addToSession(event.getSession(), (XmlLoggable)event.getEntity());
        }
        flush(event);
    }

    @Override
    protected void process(Session session, Object obj) {
        if (!session.contains(obj) || session.isReadOnly(obj)) {
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
        if (testSession(event.getSession())) {
            logger.error("trying to logToXML: {} but session is closed", event.getEntity());
            return;
        }

        if (event.getEntity() instanceof XmlLoggable) {
            addToSession(event.getSession(), (XmlLoggable)event.getEntity());
        }
        flush(event);

    }

    private boolean testSession(EventSource session) {
        return session.isClosed();
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        if (testSession(event.getSession())) {
            logger.error("trying to logToXML: {} but session is closed", event.getEntity());
            return;
        }
        if (event.getEntity() instanceof XmlLoggable) {
            addToSession(event.getSession(), (XmlLoggable)event.getEntity());
        }
        flush(event);

    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return false;
    }

    @Override
    public void onSaveOrUpdate(SaveOrUpdateEvent event) throws HibernateException {
        if (event.getEntity() instanceof XmlLoggable) {
            addToSession(event.getSession(), (XmlLoggable)event.getEntity());
        }
    }

    @Override
    public void onFlush(FlushEvent event) throws HibernateException {
        flush(event);
    }

    @Override
    public void onFlushEntity(FlushEntityEvent event) throws HibernateException {
        // TODO Auto-generated method stub

    }

}
