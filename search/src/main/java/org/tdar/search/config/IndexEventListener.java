package org.tdar.search.config;

import java.io.IOException;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.tdar.core.bean.Indexable;
import org.tdar.core.dao.AbstractEventListener;
import org.tdar.core.dao.hibernateEvents.EventListener;
import org.tdar.core.service.AutowireHelper;
import org.tdar.search.index.LookupSource;
import org.tdar.search.service.index.SearchIndexService;

/**
 * See for major changes:
 * http://stackoverflow.com/questions/26438813/get-old-values-in-collection-at-
 * onpostupdatecollection-event-in-hibernate
 * 
 * also useful
 * http://stackoverflow.com/questions/812364/how-to-determine-collection-changes
 * -in-a-hibernate-postupdateeventlistener
 * 
 * @author abrin
 *
 */
@Component
public class IndexEventListener extends AbstractEventListener<Indexable>
		implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener, FlushEventListener,
		FlushEntityEventListener, SaveOrUpdateEventListener, EventListener {

	private static final long serialVersionUID = -1947369283868859290L;

	protected static final transient Logger logger = LoggerFactory.getLogger(HibernateSolrIntegrator.class);

	private SearchIndexService searchIndexService;
	private SolrClient solrClient;
	private SessionFactory sessionFactory;

	private boolean isEnabled() {
		try {
			if (searchIndexService == null) {
				AutowireHelper.autowire(this, searchIndexService, solrClient, sessionFactory);
			}
		} catch (Exception e) {
			logger.warn("Exception in IndexEventListener enableCheck", e.getMessage());
		}
		if (solrClient != null) {
			return true;
		}
		return false;
	}

	public IndexEventListener() {
		super("solr");
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}

	@Override
	@Transactional(readOnly = true)
	public void onFlush(FlushEvent event) throws HibernateException {
		if (isEnabled()) {
			flush(event);
		} else {
			logger.error("NOT ENABLED");
		}
	}

	protected void cleanup() {
        if (!isEnabled()) {
            return;
        }
		try {
			for (LookupSource src : LookupSource.values()) {
				solrClient.commit(src.getCoreName());
			}
		} catch (Throwable e) {
			logger.error("error flushing", e);
		}
	}

	public Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}

	@Override
	@Transactional(readOnly = true)
	public void onPostDelete(PostDeleteEvent event) {
		if (!isEnabled()) {
			return;
		}
		if (event.getEntity() instanceof Indexable) {
			try {
				searchIndexService.purge((Indexable)event.getEntity());
			} catch (SolrServerException | IOException e) {
				logger.error("error in purge", e);
			}
//			addToSession(event.getSession(), (Indexable) event.getEntity());
		}
	}

	@Override
	@Transactional(readOnly = true)
	public void onPostUpdate(PostUpdateEvent event) {
        if (!isEnabled()) {
            return;
        }
		if (event.getEntity() instanceof Indexable) {
			if (logger.isTraceEnabled()) {
				logger.trace("update called ({}): {}" ,event.getSession().hashCode(), event.getEntity());
			}
			addToSession(event.getSession(), (Indexable) event.getEntity());
		}
	}

	@Override
	protected void process(Object entity) {
		if (!isEnabled() || entity == null) {
			return;
		}
		if (entity instanceof Indexable) {
			try {
				logger.debug("indexing: {}", entity);
				searchIndexService.index((Indexable) entity);
			} catch (SolrServerException | IOException e) {
				logger.error("error indexing", e);
			}
		}
		if (entity instanceof Collection<?>) {
			logger.trace("indexing collection: {}", entity);
			for (Object obj : (Collection<?>) entity) {
				process(obj);
			}
		}
	}

	@Override
	@Transactional(readOnly = true)
	public void onPostInsert(PostInsertEvent event) {
		if (event.getEntity() instanceof Indexable) {
			if (logger.isTraceEnabled()) {
				logger.trace("insert called ({}): {}" ,event.getSession().hashCode(), event.getEntity());
			}
			addToSession(event.getSession(), (Indexable) event.getEntity());
		}
	}

	@Transactional(readOnly = true)
	public boolean requiresPostCommitHanding(EntityPersister persister) {
		return false;
	}

	@Autowired
	public void setSolrClient(SolrClient solrClient) {
		this.solrClient = solrClient;
	}

	@Autowired
	public void setSearchIndexService(SearchIndexService searchIndexService) {
		this.searchIndexService = searchIndexService;
	}

	@Autowired
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public void onFlushEntity(FlushEntityEvent event) throws HibernateException {
		if (event.getEntity() instanceof Indexable) {
            if (logger.isTraceEnabled()) {
                logger.trace("flush entity called ({}): {}" ,event.getSession().hashCode(), event.getEntity());
            }
			flush(event);
		}
	}

	@Override
	public void onSaveOrUpdate(SaveOrUpdateEvent event) throws HibernateException {
		if (event.getEntity() instanceof Indexable) {
			if (logger.isTraceEnabled()) {
				logger.trace("save/update called ({}): {}",event.getSession().hashCode() , event.getEntity());
			}
			addToSession(event.getSession(), (Indexable) event.getEntity());
		}
	}

}
