package org.tdar.core.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.ScrollableResults;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.AsyncUpdateReceiver;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.HibernateSearchDao;
import org.tdar.core.dao.resource.ProjectDao;
import org.tdar.core.service.resource.DatasetService;
import org.tdar.search.index.LookupSource;
import org.tdar.utils.activity.Activity;

@Service
@Transactional(readOnly = true)
public class SearchIndexService {

    private static final Logger log = Logger.getLogger(SearchIndexService.class);

    @Autowired
    private HibernateSearchDao hibernateSearchDao;

    @Autowired
    private GenericService genericService;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private ResourceCollectionService resourceCollectionService;

    @Autowired
    private ProjectDao projectDao;

    private static final int FLUSH_EVERY = TdarConfiguration.getInstance().getIndexerFlushSize();

    private static final int INDEXER_BATCH_SIZE_TO_LOAD_OBJECTS = 50;
    private static final int INDEXER_THREADS_FOR_SUBSEQUENT_FETCHING = 5;
    private static final int INDEXER_THREADS_TO_LOAD_OBJECTS = 5;
    // private Class<? extends Indexable>[] defaultClassesToIndex = { Resource.class, Person.class, Institution.class, GeographicKeyword.class,
    // CultureKeyword.class, InvestigationType.class, MaterialKeyword.class, SiteNameKeyword.class, SiteTypeKeyword.class, TemporalKeyword.class,
    // OtherKeyword.class, ResourceAnnotationKey.class, ResourceCollection.class };

    public static final String BUILD_LUCENE_INDEX_ACTIVITY_NAME = "Build Lucene Search Index";

    public void indexAll(AsyncUpdateReceiver updateReceiver) {
        indexAll(updateReceiver, getDefaultClassesToIndex());
    }

    private List<Class<? extends Indexable>> getDefaultClassesToIndex() {
        List<Class<? extends Indexable>> toReindex = new ArrayList<Class<? extends Indexable>>();
        for (LookupSource source : LookupSource.values()) {
            // FIXME::
            if (source == LookupSource.RESOURCE) {
                toReindex.add(Resource.class);
            } else {
                toReindex.addAll(Arrays.asList(source.getClasses()));
            }
        }
        return toReindex;
    }

    @SuppressWarnings("deprecation")
    public void indexAll(AsyncUpdateReceiver updateReceiver, List<Class<? extends Indexable>> classesToIndex) {
        if (updateReceiver == null) {
            updateReceiver = getDefaultUpdateReceiver();
        }
        Activity activity = new Activity();
        activity.setName(BUILD_LUCENE_INDEX_ACTIVITY_NAME);
        activity.setMessage(String.format("reindexing %s", StringUtils.join(classesToIndex, ", ")));
        activity.start();
        ActivityManager.getInstance().addActivityToQueue(activity);

        try {
            genericService.synchronize();
            updateReceiver.setPercentComplete(0);

            FullTextSession fullTextSession = getFullTextSession();
            FlushMode previousFlushMode = fullTextSession.getFlushMode();
            fullTextSession.setFlushMode(FlushMode.MANUAL);
            fullTextSession.setCacheMode(CacheMode.IGNORE);
            SearchFactory sf = fullTextSession.getSearchFactory();
            float percent = 0f;
            float maxPer = (1f / (float) classesToIndex.size()) * 100f;
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
                    index(fullTextSession, item);
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
            activity.end();
        } catch (Throwable ex) {
            log.warn(ex);
            updateReceiver.addError(ex);
        }
    }

    private void index(FullTextSession fullTextSession, Object item) {
        if (item instanceof InformationResource) {
            datasetService.assignMappedDataForInformationResource(((InformationResource) item));
        }

        if (item instanceof Project) {
            Project project = (Project) item;
            if (CollectionUtils.isEmpty(project.getCachedInformationResources())) {
                projectDao.findAllResourcesInProject(project);
            }
        }
        fullTextSession.index(item);
    }

    public void indexAllResourcesInCollectionSubTree(ResourceCollection collectionToReindex) {
        log.info("indexing collection async");
        List<ResourceCollection> collections = resourceCollectionService.findAllChildCollectionsRecursive(collectionToReindex, CollectionType.SHARED);
        collections.add(collectionToReindex);
        Set<Resource> resources = new HashSet<Resource>();
        for (ResourceCollection collection : collections) {
            resources.addAll(collection.getResources());
        }

        indexCollection(resources);
    }

    @Async
    public void indexAllResourcesInCollectionSubTreeAsync(final ResourceCollection collectionToReindex) {
        indexAllResourcesInCollectionSubTree(collectionToReindex);
    }

    @Async
    public <C extends Indexable> void indexCollectionAsync(final Collection<C> collectionToReindex) {
        indexCollection(collectionToReindex);
    }

    // @SuppressWarnings("deprecation")
    // public <H extends Indexable & Persistable> void index(H... obj) {
    // log.debug("MANUAL INDEXING ... " + obj.length);
    // genericService.synchronize();
    //
    // FullTextSession fullTextSession = getFullTextSession();
    // FlushMode previousFlushMode = fullTextSession.getFlushMode();
    // fullTextSession.setFlushMode(FlushMode.MANUAL);
    // fullTextSession.setCacheMode(CacheMode.IGNORE);
    // fullTextSession.flushToIndexes();
    // for (H obj_ : obj) {
    // if (obj_ != null) {
    // fullTextSession.purge(obj_.getClass(), obj_.getId());
    // index(fullTextSession, obj_);
    // }
    // }
    // fullTextSession.flushToIndexes();
    // // fullTextSession.clear();
    // fullTextSession.setFlushMode(previousFlushMode);
    // }

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

    public <C extends Indexable> void index(C... indexable) {
        indexCollection(Arrays.asList(indexable));
    }

    public <C extends Indexable> void indexCollection(Collection<C> indexable) {
        log.debug("manual indexing ... " + indexable.size());
        if (indexable != null) {
            FullTextSession fullTextSession = getFullTextSession();

            for (C toIndex : indexable) {
                log.debug("indexing: " + toIndex);
                try {
                    // if we were called via async, the objects will belong to managed by the current hib session.
                    // purge them from the session and merge w/ transient object to get it back on the session before indexing.
                    fullTextSession.purge(toIndex.getClass(), toIndex.getId());
                    index(fullTextSession, genericService.merge(toIndex));
                } catch (Exception e) {
                    log.error("exception in indexing", e);
                    log.error(String.format("%s %s", ExceptionUtils.getRootCauseMessage(e), ExceptionUtils.getRootCauseStackTrace(e)),
                            ExceptionUtils.getRootCause(e));
                }
            }
            fullTextSession.flushToIndexes();
        }
    }

    /*
     * should only be used in tests...
     */
    @Deprecated
    public void flushToIndexes() {
        getFullTextSession().flushToIndexes();
    }

    public void indexAll() {
        indexAll(getDefaultUpdateReceiver(), getDefaultClassesToIndex());
    }

    public void indexAll(Class<? extends Indexable>... classes) {
        indexAll(getDefaultUpdateReceiver(), Arrays.asList(classes));
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

    private FullTextSession getFullTextSession() {
        return Search.getFullTextSession(hibernateSearchDao.getFullTextSession());
    }

    public void purgeAll() {
        purgeAll(getDefaultClassesToIndex());
    }

    public void purgeAll(List<Class<? extends Indexable>> classes) {
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
        for (Class<?> toIndex : getDefaultClassesToIndex()) {
            sf.optimize(toIndex);
            log.info("optimizing " + toIndex.getSimpleName());
        }
    }

    @Autowired
    public void setGenericService(GenericService genericService) {
        this.genericService = genericService;
    }

    public void indexProject(Project project) {
        project.setCachedInformationResources(new HashSet<InformationResource>(projectDao.findAllResourcesInProject(project)));
        project.setReadyToIndex(true);
        index(project);
        log.debug("reindexing project contents");
        indexCollection(project.getCachedInformationResources());
        log.debug("completed reindexing project contents");
    }

    @Async
    public void indexProjectAsync(final Project project) {
        indexProject(project);
    }
}
