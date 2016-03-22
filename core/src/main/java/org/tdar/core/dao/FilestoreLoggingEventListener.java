package org.tdar.core.dao;

import org.hibernate.HibernateException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.XmlLoggable;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.hibernateEvents.EventListener;
import org.tdar.core.service.AutowireHelper;
import org.tdar.core.service.SerializationService;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.utils.jaxb.XMLFilestoreLogger;

@Component
public class FilestoreLoggingEventListener extends AbstractEventListener<XmlLoggable>
		implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener, FlushEntityEventListener, 
		FlushEventListener, SaveOrUpdateEventListener, EventListener {

	private static final long serialVersionUID = -2773973927518207238L;

	private final transient Logger logger = LoggerFactory.getLogger(getClass());
	private SerializationService serializationService;

	private final TdarConfiguration CONFIG = TdarConfiguration.getInstance();
	
	public FilestoreLoggingEventListener() throws ClassNotFoundException {
		super("Filestore");
	      SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}

	private boolean isEnabled() {
        if (!CONFIG.shouldLogToFilestore()) {
            return false;
        }
	    try {
	        if (serializationService == null) {
	            AutowireHelper.autowire(this, serializationService);
	        }
	        if (serializationService != null) {
	            return true;
	        }
	    } catch (Exception e) {
	        logger.error("error intializing FilestoreLogEventListener",e);
	    }
	    return false;
	}
	
	@Override
	public void onPostDelete(PostDeleteEvent event) {
		if (testSession(event.getSession())) {
			logger.error("trying to logToXML: {} but session is closed", event.getEntity());
			return;
		}
		if (event.getEntity() instanceof XmlLoggable) {
			//addToSession(event.getSession(), (XmlLoggable) event.getEntity());
			XmlLoggable old = (XmlLoggable)event.getEntity();
			XmlLoggable newInstance;
			try {
				newInstance = (XmlLoggable)event.getEntity().getClass().newInstance();
			newInstance.setId(old.getId());
			if (old instanceof HasStatus) {
				((HasStatus)newInstance).setStatus(Status.DELETED);
			}
			serializationService.convertToXML(newInstance);
			} catch (Exception e) {
				logger.warn("error in XML convert", old);
			}
		}
//		flush(event);
	}

	@Override
	protected void process(Object obj) {

		if (obj == null || !isEnabled()) {
			return;
		}

		if (obj instanceof XmlLoggable) {
    		try {
    				String xml = serializationService.convertToXML((XmlLoggable) obj);
    				XMLFilestoreLogger.writeToFilestore(FilestoreObjectType.fromClass(obj.getClass()), ((XmlLoggable)obj).getId(), xml);
    		        logger.trace("done saving");
    		} catch (Exception e) {
    			logger.error("error ocurred when serializing to XML: {}", e.getMessage(), e);
    		}
		}
	}

	@Override
	public void onPostUpdate(PostUpdateEvent event) {
		if (testSession(event.getSession())) {
			logger.error("trying to logToXML: {} but session is closed", event.getEntity());
			return;
		}

		if (event.getEntity() instanceof XmlLoggable) {
			addToSession(event.getSession(), (XmlLoggable) event.getEntity());
		}
		flush(event);

	}

	private boolean testSession(EventSource session) {
		return !isEnabled() && session.isClosed();
	}

	@Override
	public void onPostInsert(PostInsertEvent event) {
		if (testSession(event.getSession())) {
			logger.error("trying to logToXML: {} but session is closed", event.getEntity());
			return;
		}
		if (event.getEntity() instanceof XmlLoggable) {
			addToSession(event.getSession(), (XmlLoggable) event.getEntity());
		}
		flush(event);

	}

	@Override
	public boolean requiresPostCommitHanding(EntityPersister persister) {
		return false;
	}

	@Override
	public void onSaveOrUpdate(SaveOrUpdateEvent event) throws HibernateException {
		if (event.getEntity() instanceof XmlLoggable && !event.getSession().isReadOnly(event.getEntity())) {
			addToSession(event.getSession(), (XmlLoggable) event.getEntity());
		}
	}

	@Override
	public void onFlush(FlushEvent event) throws HibernateException {
		flush(event);
	}

	@Override
	public void onFlushEntity(FlushEntityEvent event) throws HibernateException {
		if (event.getEntity() instanceof XmlLoggable && !event.getSession().isReadOnly(event.getEntity())) {
			flush(event);
		}
	}


    @Autowired
    public void setSerializationService(SerializationService serializationService) {
        this.serializationService = serializationService;
    }

}
