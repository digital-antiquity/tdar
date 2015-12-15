package org.tdar.search.config;

import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.DuplicationStrategy;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateSolrIntegrator implements Integrator {

    protected static final transient Logger logger = LoggerFactory.getLogger(HibernateSolrIntegrator.class);

    public static final String AUTO_REGISTER = "hibernate.search.autoregister_listeners";


    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
    }


    private void registerHibernateSearchEventListener(IndexEventListener eventListener, SessionFactoryServiceRegistry serviceRegistry) {
        EventListenerRegistry listenerRegistry = serviceRegistry.getService( EventListenerRegistry.class );
        listenerRegistry.addDuplicationStrategy( new DuplicationStrategyImpl( IndexEventListener.class ) );

        listenerRegistry.getEventListenerGroup( EventType.POST_INSERT).appendListener( eventListener );
        listenerRegistry.getEventListenerGroup( EventType.POST_UPDATE).appendListener( eventListener );
        listenerRegistry.getEventListenerGroup( EventType.POST_DELETE).appendListener( eventListener );
        listenerRegistry.getEventListenerGroup( EventType.POST_COLLECTION_RECREATE).appendListener( eventListener );
        listenerRegistry.getEventListenerGroup( EventType.POST_COLLECTION_REMOVE).appendListener( eventListener );
        listenerRegistry.getEventListenerGroup( EventType.POST_COLLECTION_UPDATE).appendListener( eventListener );
        listenerRegistry.getEventListenerGroup( EventType.FLUSH).appendListener( eventListener );
    }

    public static class DuplicationStrategyImpl implements DuplicationStrategy {
        private final Class checkClass;

        public DuplicationStrategyImpl(Class checkClass) {
            this.checkClass = checkClass;
        }

        @Override
        public boolean areMatch(Object listener, Object original) {
            return checkClass == original.getClass() && checkClass == listener.getClass();
        }

        @Override
        public Action getAction() {
            return Action.KEEP_ORIGINAL;
        }
    }

    @Override
    public void integrate(Configuration configuration, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
        IndexEventListener eventListener = new IndexEventListener();
        registerHibernateSearchEventListener( eventListener, serviceRegistry );

        ClassLoaderService hibernateClassLoaderService = serviceRegistry.getService( ClassLoaderService.class );
//        SessionFactoryObserver observer = new SessionFactoryObserver(
//                metadata,
//                configurationService,
//                fullTextIndexEventListener,
//                hibernateClassLoaderService
//        );
//        sessionFactory.addObserver( observer );
//        // TODO Auto-generated method stub
                
    }

    @Override
    public void integrate(MetadataImplementor metadata, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {

    }

}
