package org.tdar.search.config;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.collections4.CollectionUtils;
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
import org.hibernate.event.spi.PreCollectionUpdateEvent;
import org.hibernate.event.spi.PreCollectionUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.tdar.core.bean.Indexable;
import org.tdar.core.service.AutowireHelper;
import org.tdar.search.service.SearchIndexService;

/**
 * See for major changes:
 * http://stackoverflow.com/questions/26438813/get-old-values-in-collection-at-onpostupdatecollection-event-in-hibernate
 * 
 * also useful
 * http://stackoverflow.com/questions/812364/how-to-determine-collection-changes-in-a-hibernate-postupdateeventlistener
 * 
 * @author abrin
 *
 */
@Component
public class IndexEventListener implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener, PostCollectionRemoveEventListener,
        PostCollectionRecreateEventListener, PostCollectionUpdateEventListener, PreCollectionUpdateEventListener, FlushEventListener {

    private static final long serialVersionUID = -1947369283868859290L;

    protected static final transient Logger logger = LoggerFactory.getLogger(HibernateSolrIntegrator.class);

    private WeakHashMap<String, Collection<?>> idChangeMap = new WeakHashMap<>();
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
    @Transactional(readOnly = true)
    public void onFlush(FlushEvent event) throws HibernateException {
        if (isEnabled()) {
            try {
                getSolrClient().commit();
            } catch (SolrServerException | IOException e) {
                logger.error("error flushing", e);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void onPostUpdateCollection(PostCollectionUpdateEvent event) {
        processCollectionEvent(event);
    }

    @Override
    @Transactional(readOnly = true)
    public void onPostRecreateCollection(PostCollectionRecreateEvent event) {
        processCollectionEvent(event);
    }

    @Override
    @Transactional(readOnly = true)
    public void onPostRemoveCollection(PostCollectionRemoveEvent event) {
        processCollectionEvent(event);
    }

    private void processCollectionEvent(AbstractCollectionEvent event) {
        if (!isEnabled()) {
            return;
        }
        Object entity = event.getAffectedOwnerOrNull();
        // logger.debug("--------");
        // logger.debug("postUpdate: {} - {}", getCollectionKey(event), event.getCollection());
        if (entity == null) {
            return;
        }

        PersistentCollection persistentCollection = event.getCollection();
        if (persistentCollection != null) {
            if (!persistentCollection.wasInitialized()) {
                return;
            }

            Collection<?> old = idChangeMap.get(getCollectionKey(event));
            if (old == null) {
                old = Collections.emptyList();
            }
            Collection<?> intersection = CollectionUtils.intersection((Collection<?>) persistentCollection, old);
            Collection<?> disjunction = CollectionUtils.disjunction((Collection<?>) persistentCollection, old);
            index(intersection);
            index(disjunction);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void onPostDelete(PostDeleteEvent event) {
        if (!isEnabled()) {
            return;
        }
        Object entity = event.getEntity();
        if (entity instanceof Indexable) {
            try {
                logger.debug("purging: {}", entity);
                getSearchIndexService().purge((Indexable) entity);
            } catch (SolrServerException | IOException e) {
                logger.error("error purging", e);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void onPostUpdate(PostUpdateEvent event) {
        index(event.getEntity());
    }

    private void index(Object entity) {
        if (!isEnabled()) {
            return;
        }
        if (entity instanceof Indexable) {
            try {
                logger.trace("indexing: {}", entity);
                getSearchIndexService().index((Indexable) entity);
            } catch (SolrServerException | IOException e) {
                logger.error("error indexing", e);
            }
        }
        if (entity instanceof Collection<?>) {
            logger.trace("indexing collection: {}", entity);
            for (Object obj : (Collection<?>) entity) {
                index(obj);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void onPostInsert(PostInsertEvent event) {
        index(event.getEntity());
    }

    @Override
    @Transactional(readOnly = true)
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

    @Override
    public void onPreUpdateCollection(PreCollectionUpdateEvent event) {
        String key = getCollectionKey(event);
        if (logger.isTraceEnabled()) {
            logger.trace("preUpdate: {} - {}", key, event.getCollection());
        }
        Serializable serializedSnapshot = event.getCollection().getStoredSnapshot();
        if (serializedSnapshot instanceof Map) {
            Map snapshot = (Map) serializedSnapshot;
            // set values are also stored as map values with same key and valu
            idChangeMap.put(key, snapshot.values());
        }
        if (serializedSnapshot instanceof Collection) {
            idChangeMap.put(key, (Collection) serializedSnapshot);
        }
    }

    private String getCollectionKey(AbstractCollectionEvent event) {
        String role = event.getCollection().getRole();
        Long id = (Long) event.getAffectedOwnerIdOrNull();
        String key = id + role;
        return key;
    }

}
