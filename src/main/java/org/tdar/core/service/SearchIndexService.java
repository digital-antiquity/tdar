package org.tdar.core.service;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.ScrollableResults;
import org.hibernate.SessionFactory;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.configuration.TdarConfiguration;

@Service
@Transactional(readOnly = true)
public class SearchIndexService {

    private static final Logger log = Logger.getLogger(SearchIndexService.class);
    @Autowired
    private SessionFactory sessionFactory;
    @Autowired
    private GenericService genericService;

    private static final int FLUSH_EVERY = TdarConfiguration.getInstance().getIndexerFlushSize();

    private static final int INDEXER_BATCH_SIZE_TO_LOAD_OBJECTS = 50;
    private static final int INDEXER_THREADS_FOR_SUBSEQUENT_FETCHING = 5;
    private static final int INDEXER_THREADS_TO_LOAD_OBJECTS = 5;
    private Class<?>[] defaultClassesToIndex = { Resource.class, Person.class, Institution.class, GeographicKeyword.class,
            CultureKeyword.class, InvestigationType.class, MaterialKeyword.class, SiteNameKeyword.class, SiteTypeKeyword.class, TemporalKeyword.class,
            OtherKeyword.class,
            ResourceAnnotationKey.class };

    public void indexAll(AsyncUpdateReceiver updateReceiver) {
        indexAll(updateReceiver, defaultClassesToIndex);
    }

    public void indexAll(AsyncUpdateReceiver updateReceiver, Class<?>[] classesToIndex) {
        if (updateReceiver == null) {
            updateReceiver = getDefaultUpdateReceiver();
        }

        genericService.synchronize();
        updateReceiver.setPercentComplete(0);

        FullTextSession fullTextSession = getFullTextSession();
        FlushMode previousFlushMode = fullTextSession.getFlushMode();
        fullTextSession.setFlushMode(FlushMode.MANUAL);
        fullTextSession.setCacheMode(CacheMode.IGNORE);
        SearchFactory sf = fullTextSession.getSearchFactory();
        float percent = 0f;
        float maxPer = (1f / (float) classesToIndex.length) * 100f;
        for (Class<?> toIndex : classesToIndex) {
            fullTextSession.purgeAll(toIndex);
            sf.optimize(toIndex);
            Number total = genericService.count(toIndex);
            ScrollableResults scrollableResults = genericService.findAllScrollable(toIndex);
            updateReceiver.setStatus(total + " " + toIndex.getSimpleName() + "(s) to be indexed");
            int divisor = getDivisor(total);
            float currentProgress = 0f;
            int numProcessed = 0;
            String MIDDLE = " of " + total.intValue() + " " + toIndex.getSimpleName() + "(s) ";

            while (scrollableResults.next()) {
                Object item = scrollableResults.get(0);
                currentProgress = (float) numProcessed / total.floatValue();
                fullTextSession.index(item);
                numProcessed++;
                float totalProgress = (currentProgress * maxPer + percent);

                if (numProcessed % divisor == 0) {
                    updateReceiver.setStatus("indexed " + numProcessed + MIDDLE + totalProgress + "%");
                    updateReceiver.setPercentComplete(totalProgress / 100f);
                }
                if ((numProcessed % FLUSH_EVERY) == 0) {
                    updateReceiver.setStatus("indexed " + numProcessed + MIDDLE + totalProgress + "% ... (flushing)");
                    log.trace("flushing search index");
                    fullTextSession.flushToIndexes();
                    fullTextSession.clear();
                    log.trace("flushed search index");
                }
            }
            scrollableResults.close();
            fullTextSession.flushToIndexes();
            fullTextSession.clear();
            percent += maxPer;
            updateReceiver.setStatus("finished indexing all " + toIndex.getSimpleName() + "(s).");
        }

        fullTextSession.flushToIndexes();
        fullTextSession.clear();
        updateReceiver.setStatus("index all complete");
        updateReceiver.setPercentComplete(100f);
        fullTextSession.setFlushMode(previousFlushMode);
    }

    public int getDivisor(Number total) {
        int divisor = 5;
        if (total.intValue() < 50) {
            divisor = 2;
        } else if (total.intValue() < 100) {
            divisor = 20;
        } else if (total.intValue() < 1000) {
            divisor = 50;
        } else if (total.intValue() < 10000) {
            divisor = 500;
        } else {
            divisor = 5000;
        }
        return divisor;
    }

    public <C> void indexCollection(Collection<C> indexable) {
        if (indexable == null)
            return;
        FullTextSession fullTextSession = getFullTextSession();
        int numProcessed = 0;
        for (C toIndex : indexable) {
            fullTextSession.index(toIndex);
            numProcessed++;
            if ((numProcessed % FLUSH_EVERY) == 0) {
                fullTextSession.flushToIndexes();
                fullTextSession.clear();
                log.debug("flusing search index");
            }
        }
        fullTextSession.flushToIndexes();
        fullTextSession.clear();
    }

    public void indexAll() {
        indexAll(getDefaultUpdateReceiver(), defaultClassesToIndex);
    }

    public void indexAll(Class<?>... classes) {
        indexAll(getDefaultUpdateReceiver(), classes);
    }

    // an update receiver that doesn't do anything
    private AsyncUpdateReceiver getDefaultUpdateReceiver() {
        return AsyncUpdateReceiver.DEFAULT_RECEIVER;
    }

    // Warning, this type of indexing does not use lazy fetching, which as of
    // the current build is causing exceptions
    public void massIndex(Class<?>... classes) {
        try {
            getFullTextSession().createIndexer(classes).purgeAllOnStart(true).batchSizeToLoadObjects(INDEXER_BATCH_SIZE_TO_LOAD_OBJECTS)
                    .cacheMode(CacheMode.IGNORE).threadsToLoadObjects(INDEXER_THREADS_TO_LOAD_OBJECTS)
                    .threadsForSubsequentFetching(INDEXER_THREADS_FOR_SUBSEQUENT_FETCHING).startAndWait();
        } catch (InterruptedException e) {
            log.error("index failed", e);
        }
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private FullTextSession getFullTextSession() {
        return Search.getFullTextSession(sessionFactory.getCurrentSession());
    }

    public void purgeAll() {
        purgeAll(defaultClassesToIndex);
    }

    public void purgeAll(Class<?>[] classes) {
        FullTextSession fullTextSession = getFullTextSession();
        for (Class<?> clss : classes) {
            fullTextSession.purgeAll(clss);
        }
    }

    /**
 * 
 */
    public void optimizeAll() {
        FullTextSession fullTextSession = getFullTextSession();
        SearchFactory sf = fullTextSession.getSearchFactory();
        for (Class<?> toIndex : defaultClassesToIndex) {
            sf.optimize(toIndex);
            log.info("optimizing " + toIndex.getSimpleName());
        }
    }

}
