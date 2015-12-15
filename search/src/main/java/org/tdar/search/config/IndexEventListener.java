package org.tdar.search.config;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.hibernate.HibernateException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.event.spi.AbstractCollectionEvent;
import org.hibernate.event.spi.FlushEvent;
import org.hibernate.event.spi.FlushEventListener;
import org.hibernate.event.spi.PostCollectionRecreateEvent;
import org.hibernate.event.spi.PostCollectionRecreateEventListener;
import org.hibernate.event.spi.PostCollectionRemoveEvent;
import org.hibernate.event.spi.PostCollectionRemoveEventListener;
import org.hibernate.event.spi.PostCollectionUpdateEvent;
import org.hibernate.event.spi.PostCollectionUpdateEventListener;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.tdar.core.bean.Indexable;
import org.tdar.core.service.AutowireHelper;
import org.tdar.search.service.SearchIndexService;

@Component
public class IndexEventListener implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener, PostCollectionRemoveEventListener, PostCollectionRecreateEventListener, PostCollectionUpdateEventListener, FlushEventListener {

    private static final long serialVersionUID = -1947369283868859290L;

    protected static final transient Logger logger = LoggerFactory.getLogger(HibernateSolrIntegrator.class);
    
    private SearchIndexService searchIndexService;
    private SolrClient solrClient;
    
    private boolean isEnabled() {
        if (searchIndexService == null) {
            AutowireHelper.autowire(this, searchIndexService, solrClient); 
        }
        if (getSolrClient() != null) {
            return true;
        }
        return false;
    }
    
    public IndexEventListener() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this); 
    }
    
    @Override
    public void onFlush(FlushEvent event) throws HibernateException {
        if (isEnabled()) {
            try {
                getSolrClient().commit();
            } catch (SolrServerException | IOException e) {
                logger.error("error flushing",e);
            }
        }
    }

    @Override
    public void onPostUpdateCollection(PostCollectionUpdateEvent event) {
        processCollectionEvent( event );
    }

    @Override
    public void onPostRecreateCollection(PostCollectionRecreateEvent event) {
        processCollectionEvent( event );        
    }

    @Override
    public void onPostRemoveCollection(PostCollectionRemoveEvent event) {
        processCollectionEvent( event );        
    }

    private void processCollectionEvent(AbstractCollectionEvent event) {
        if (!isEnabled()) {
            return;
        }
        Object entity = event.getAffectedOwnerOrNull();
        if (entity == null) {
            return;
        }
        
        PersistentCollection persistentCollection = event.getCollection();
        final String collectionRole;
        if ( persistentCollection != null ) {
            if ( !persistentCollection.wasInitialized() ) {
                return;
            }
            collectionRole = persistentCollection.getRole();
        } else {
            collectionRole = null;
        }
        Class v = persistentCollection.getValue().getClass();
        logger.trace("{} {} {} {}", event.getClass(), v, event.getAffectedOwnerEntityName());
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        if (!isEnabled()) {
            return;
        }
        Object entity = event.getEntity();
        if (entity instanceof Indexable) {
            try {
                logger.debug("purging: {}", entity);
                getSearchIndexService().purge((Indexable)entity);
            } catch (SolrServerException | IOException e) {
                logger.error("error purging",e);
            }
        }
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        index(event.getEntity());
    }

    private void index(Object entity) {
        logger.debug("{}", entity);
        if (entity instanceof Indexable && isEnabled()) {
            try {
                logger.debug("indexing: {}", entity);
                getSearchIndexService().index((Indexable)entity);
            } catch (SolrServerException | IOException e) {
                logger.error("error indexing",e);
            }
        }
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        index(event.getEntity());        
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        // TODO Auto-generated method stub
        return false;
    }

    public SolrClient getSolrClient() {
        return solrClient;
    }

    @Autowired
    public void setSolrClient(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    public SearchIndexService getSearchIndexService() {
        return searchIndexService;
    }

    @Autowired
    public void setSearchIndexService(SearchIndexService searchIndexService) {
        this.searchIndexService = searchIndexService;
    }

}
