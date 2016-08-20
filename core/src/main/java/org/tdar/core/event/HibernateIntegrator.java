package org.tdar.core.event;

import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

public class HibernateIntegrator implements Integrator {


    @Override
    public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
        EventListenerRegistry eventListenerRegistry = serviceRegistry.getService( EventListenerRegistry.class );

        // EventListenerRegistry defines 3 ways to register listeners:
        //     1) This form overrides any existing registrations with 
        eventListenerRegistry.appendListeners(EventType.SAVE_UPDATE, HibernateObfuscationListener.class);
//        eventListenerRegistry.appendListeners(EventType.SAVE, HibernateObfuscationListener.class);
//        eventListenerRegistry.appendListeners(EventType.UPDATE, HibernateObfuscationListener.class);
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
        // TODO Auto-generated method stub
        
    }
}
