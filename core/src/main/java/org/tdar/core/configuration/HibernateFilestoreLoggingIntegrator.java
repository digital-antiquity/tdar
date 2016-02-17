package org.tdar.core.configuration;

import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.dao.FilestoreLoggingEventListener;

public class HibernateFilestoreLoggingIntegrator implements Integrator {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory,
            SessionFactoryServiceRegistry serviceRegistry) {
    }

    @Override
    public void integrate(Metadata arg0, SessionFactoryImplementor arg1, SessionFactoryServiceRegistry serviceRegistry) {
        final EventListenerRegistry registry = serviceRegistry.getService(EventListenerRegistry.class);
        try {
            FilestoreLoggingEventListener listener = new FilestoreLoggingEventListener();
            logger.debug("registering hibernate listener");
            registry.getEventListenerGroup(EventType.POST_COMMIT_INSERT).appendListener(listener);
            registry.getEventListenerGroup(EventType.POST_COMMIT_UPDATE).appendListener(listener);
            registry.getEventListenerGroup(EventType.POST_COMMIT_DELETE).appendListener(listener);
            registry.getEventListenerGroup( EventType.SAVE_UPDATE).appendListener( listener );
            registry.getEventListenerGroup( EventType.FLUSH).appendListener( listener );
//            registry.getEventListenerGroup( EventType.FLUSH_ENTITY).appendListener( listener );
        } catch (Exception e) {
            logger.error("could not add listener: {}", e);
        }
        
    }

}
