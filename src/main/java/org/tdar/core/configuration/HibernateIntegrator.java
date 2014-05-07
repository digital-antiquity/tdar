package org.tdar.core.configuration;

import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.dao.FilestoreLoggingEventListener;

public class HibernateIntegrator implements Integrator {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void integrate(Configuration configuration, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
        final EventListenerRegistry registry = serviceRegistry.getService(EventListenerRegistry.class);
        FilestoreLoggingEventListener listener  = new FilestoreLoggingEventListener();
        logger.debug("registering hibernate listener");
		registry.getEventListenerGroup(EventType.POST_INSERT).appendListener(listener);
        registry.getEventListenerGroup(EventType.POST_COMMIT_UPDATE).appendListener(listener);
        registry.getEventListenerGroup(EventType.POST_DELETE).appendListener(listener);
        
	}

	@Override
	public void integrate(MetadataImplementor metadata,
			SessionFactoryImplementor sessionFactory,
			SessionFactoryServiceRegistry serviceRegistry) {
        final EventListenerRegistry registry = serviceRegistry.getService(EventListenerRegistry.class);
        FilestoreLoggingEventListener listener  = new FilestoreLoggingEventListener();
        logger.debug("registering hibernate listener2");
        
		registry.getEventListenerGroup(EventType.POST_COMMIT_INSERT).appendListener(listener);
        registry.getEventListenerGroup(EventType.POST_COMMIT_UPDATE).appendListener(listener);
        registry.getEventListenerGroup(EventType.POST_COMMIT_DELETE).appendListener(listener);

	}

	@Override
	public void disintegrate(SessionFactoryImplementor sessionFactory,
			SessionFactoryServiceRegistry serviceRegistry) {
	}

}
