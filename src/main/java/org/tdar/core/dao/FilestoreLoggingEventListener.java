package org.tdar.core.dao;

import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.XmlService;

public class FilestoreLoggingEventListener implements PostInsertEventListener,
		PostUpdateEventListener, PostDeleteEventListener {

	private static final long serialVersionUID = -2773973927518207238L;

	private final transient Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void onPostDelete(PostDeleteEvent event) {
		logToXml(event.getEntity());
	}

	private void logToXml(Object obj) {
		if (obj instanceof Resource) {
			logger.debug("serializing record to XML: {}", obj);
//			getXmlService().logRecordXmlToFilestore((Resource) obj);
		}
	}

	@Override
	public void onPostUpdate(PostUpdateEvent event) {
		logToXml(event.getEntity());
	}

	@Override
	public void onPostInsert(PostInsertEvent event) {
		logToXml(event.getEntity());
	}

	@Override
	public boolean requiresPostCommitHanding(EntityPersister persister) {
		return false;
	}

}
