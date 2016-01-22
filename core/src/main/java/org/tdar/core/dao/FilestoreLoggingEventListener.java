package org.tdar.core.dao;

import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.collections4.CollectionUtils;
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
import org.tdar.utils.jaxb.XMLFilestoreLogger;

public class FilestoreLoggingEventListener implements PostInsertEventListener, PostUpdateEventListener,
		PostDeleteEventListener, FlushEntityEventListener, FlushEventListener, SaveOrUpdateEventListener {

	private static final long serialVersionUID = -2773973927518207238L;

	private final transient Logger logger = LoggerFactory.getLogger(getClass());
	XMLFilestoreLogger xmlLogger;

	private WeakHashMap<Session, Set<Object>> idChangeMap = new WeakHashMap<>();

	public FilestoreLoggingEventListener() throws ClassNotFoundException {
		xmlLogger = new XMLFilestoreLogger();
	}

	@Override
	public void onPostDelete(PostDeleteEvent event) {
		if (testSession(event.getSession())) {
			logger.error("trying to logToXML: {} but session is closed", event.getEntity());
			return;
		}
		logToXml(event.getSession(), event.getEntity());
	}

	private void logToXml(Session session, Object obj) {
		if (session.isReadOnly(obj)) {
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

		Object obj = event.getEntity();
		// only skip on updates
		if (obj instanceof XmlLoggable && !((XmlLoggable) obj).isReadyToStore()) {
			logger.debug("skipping xml logging for: {}", obj);
			return;
		}

		logToXml(event.getSession(), event.getEntity());

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
		logToXml(event.getSession(), event.getEntity());

	}

	@Override
	public boolean requiresPostCommitHanding(EntityPersister persister) {
		return false;
	}

	@Override
	public void onSaveOrUpdate(SaveOrUpdateEvent event) throws HibernateException {
		if (idChangeMap.get(event.getSession()) == null) {
			idChangeMap.put(event.getSession(), new HashSet<>());
		}
		idChangeMap.get(event.getSession()).add(event.getEntity());

	}

	@Override
	public void onFlush(FlushEvent event) throws HibernateException {
		Set<Object> set = idChangeMap.get(event.getSession());
		if (!CollectionUtils.isEmpty(set)) {
			logger.debug("flush to filestore ({})", set.size());
			for (Object obj : set) {
				try {
					logToXml(event.getSession(), obj);
				} catch (Exception e) {
					logger.error("error writing to filestore", e);
				}
			}
			set.clear();
		}
	}

	@Override
	public void onFlushEntity(FlushEntityEvent event) throws HibernateException {
		// TODO Auto-generated method stub

	}

}
