package org.tdar.search.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.ScrollableResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.AsyncUpdateReceiver;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.resource.ProjectDao;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.service.ActivityManager;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.external.EmailService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.search.converter.AnnotationKeyDocumentConverter;
import org.tdar.search.converter.CollectionDocumentConverter;
import org.tdar.search.converter.InstitutionDocumentConverter;
import org.tdar.search.converter.KeywordDocumentConverter;
import org.tdar.search.converter.PersonDocumentConverter;
import org.tdar.search.converter.ResourceDocumentConverter;
import org.tdar.search.index.LookupSource;
import org.tdar.utils.ImmutableScrollableCollection;
import org.tdar.utils.activity.Activity;


@Service
@Transactional(readOnly = true)
public class SearchIndexService {

    private final Logger logger = LoggerFactory.getLogger(SearchIndexService.class);
    public static final String INDEXING_COMPLETED = "indexing completed";
    public static final String INDEXING_STARTED = "indexing of %s on %s complete.\n Started: %s \n Completed: %s";

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ResourceCollectionDao resourceCollectionDao;

    @Autowired
    private ResourceCollectionService resourceCollectionService;
    
    @Autowired
    private ResourceService resourceService;
    
    @Autowired
    private ProjectDao projectDao;

    private static final int FLUSH_EVERY = TdarConfiguration.getInstance().getIndexerFlushSize();

    private static final int INDEXER_BATCH_SIZE_TO_LOAD_OBJECTS = 50;
    private static final int INDEXER_THREADS_FOR_SUBSEQUENT_FETCHING = 5;
    private static final int INDEXER_THREADS_TO_LOAD_OBJECTS = 5;

    public static final String BUILD_LUCENE_INDEX_ACTIVITY_NAME = "Build Lucene Search Index";

    public void indexAll(AsyncUpdateReceiver updateReceiver, Person person) {
        indexAll(updateReceiver, Arrays.asList(LookupSource.values()), person);
    }

    @Autowired
    private SolrClient template;

    private String generateId(Persistable pers) {
        return SearchUtils.createKey(pers);
    }

    /**
     * The default classes to reindex
     * 
     * @return
     */
    public List<Class<? extends Indexable>> getDefaultClassesToIndex() {
        return getClassesToReindex(Arrays.asList(LookupSource.values()));
    }

    public List<Class<? extends Indexable>> getClassesToReindex(List<LookupSource> values) {
        List<Class<? extends Indexable>> toReindex = new ArrayList<>();
        for (LookupSource source : values) {
            switch (source) {
                case RESOURCE:
                    toReindex.add(Resource.class);
                    break;
                case PERSON:
                    toReindex.add(Person.class);
                    break;
                default:
                    toReindex.addAll(Arrays.asList(source.getClasses()));
            }
        }
        return toReindex;
    }

    /**
     * Index all of the @link Indexable items. Uses a ScrollableResult to manage memory and object complexity
     * 
     * @param updateReceiver
     * @param classesToIndex
     * @param person
     */
    @SuppressWarnings("deprecation")
    @Transactional(readOnly = true)
    public void indexAll(AsyncUpdateReceiver updateReceiver, List<LookupSource> sources, Person person) {
        if (updateReceiver == null) {
            updateReceiver = getDefaultUpdateReceiver();
        }
        Activity activity = new Activity();
        activity.setName(BUILD_LUCENE_INDEX_ACTIVITY_NAME);
        activity.setIndexingActivity(true);
        activity.setUser(person);
        activity.setMessage(String.format("reindexing %s", StringUtils.join(sources, ", ")));
        activity.start();
        ActivityManager.getInstance().addActivityToQueue(activity);

        CacheMode cacheMode = genericDao.getCacheModeForCurrentSession();
        try {
            genericDao.synchronize();
            float percent = 0f;
            updateAllStatuses(updateReceiver, activity, "initializing...", 0f);
            float maxPer = (1f / sources.size()) * 100f;
            
            genericDao.setCacheModeForCurrentSession(CacheMode.IGNORE);
            for (LookupSource src : sources) {
                purgeCore(src.getCoreName());
	            for (Class<? extends Indexable> toIndex : src.getClasses()) {
	                Number total = genericDao.count(toIndex);
	                updateAllStatuses(updateReceiver, activity, "initializing... ["+toIndex.getSimpleName()+": "+total+"]", percent);
	
	                ScrollableResults scrollableResults = genericDao.findAllScrollable(toIndex);
	                indexScrollable(updateReceiver, activity, percent, maxPer, toIndex, total, scrollableResults, false);
	                percent += maxPer;
	                String message = "finished indexing all " + toIndex.getSimpleName() + "(s)";
	                updateAllStatuses(updateReceiver, activity, message, percent);
	            }            
            }

            complete(updateReceiver, activity, null, null);
        } catch (Throwable ex) {
            logger.warn("exception: {}", ex);
            if (updateReceiver != null) {
                updateReceiver.addError(ex);
            }
        }
        genericDao.setCacheModeForCurrentSession(cacheMode);
        activity.end();
    }

    /**
     * Completes the reindexing process and resets the flush mode
     * 
     * @param updateReceiver
     * @param activity
     * @param fullTextSession
     * @param previousFlushMode
     */
    private void complete(AsyncUpdateReceiver updateReceiver, Activity activity, Object fullTextSession, FlushMode previousFlushMode) {
        updateAllStatuses(updateReceiver, activity, "index all complete", 100f);
        if (activity != null) {
            activity.end();
        }
    }

    /**
     * Extracting out the indexing for scrollable results so that it can be used and shared between methods.
     * 
     * @param updateReceiver
     * @param activity
     * @param fullTextSession
     * @param percent
     * @param maxPer
     * @param toIndex
     * @param total
     * @param scrollableResults
     */
    private void indexScrollable(AsyncUpdateReceiver updateReceiver, Activity activity, float percent, float maxPer,
            Class<? extends Indexable> toIndex, Number total, ScrollableResults scrollableResults, boolean deleteFirst) {
        String message = total + " " + toIndex.getSimpleName() + "(s) to be indexed";
        updateAllStatuses(updateReceiver, activity, message, 0f);
        int divisor = getDivisor(total);
        float currentProgress = 0f;
        int numProcessed = 0;
        String MIDDLE = " of " + total.intValue() + " " + toIndex.getSimpleName() + "(s) ";
        Long prevId = 0L;
        Long currentId = 0L;
        String coreForClass = getCoreForClass(toIndex);
        while (scrollableResults.next()) {
            Indexable item = (Indexable) scrollableResults.get(0);
            currentId = item.getId();
            currentProgress = numProcessed / total.floatValue();
            index(item, deleteFirst);
            numProcessed++;
            float totalProgress = ((currentProgress * maxPer) + percent);
            if ((numProcessed % divisor) == 0) {
                String range = String.format("(%s - %s)", prevId, currentId);
                message = String.format("indexed %s %s %s %% %s", numProcessed, MIDDLE, totalProgress, range);
                updateAllStatuses(updateReceiver, activity, message, totalProgress);
                logger.debug("last indexed: {}", item);
                prevId = ((Indexable) item).getId();
            }
            if ((numProcessed % FLUSH_EVERY) == 0) {
                message = String.format("indexed %s %s %s %% (flushing)", numProcessed, MIDDLE, totalProgress);
                updateAllStatuses(updateReceiver, activity, message, totalProgress);
                logger.trace("flushing search index");
                try {
                    template.commit(coreForClass);
                } catch (SolrServerException | IOException e) {
                    logger.error("error committing: {}", e);
                }
                // fullTextSession.flushToIndexes();
                // fullTextSession.clear();
                logger.trace("flushed search index");
            }
        }
        try {
            template.commit(coreForClass);
        } catch (SolrServerException | IOException e) {
            logger.error("error committing: {}", e);
        }
        scrollableResults.close();
    }

    private void updateAllStatuses(AsyncUpdateReceiver updateReceiver, Activity activity, String status, float complete) {
        if (updateReceiver != null) {
            updateReceiver.setPercentComplete(complete);
            updateReceiver.setStatus(status);
        }
        if (activity != null) {
            activity.setMessage(status);
            activity.setPercentDone(complete);
        }
        logger.debug("status: {} [{}%]", status, complete);
    }

    /**
     * Index an item of some sort.
     * 
     * @param fullTextSession
     * @param item
     */
    private SolrInputDocument index(Indexable item, boolean deleteFirst) {
        try {
            String core = getCoreForClass(item.getClass());
            if (deleteFirst) {
                purge(item);
                // fullTextSession.purge(item.getClass(), item.getId());
            }

//            if (item instanceof Project) {
//                Project project = (Project) item;
//                if (null == project.getCachedInformationResources()) {
//                    setupProjectForIndexing(project);
//                    // logger.debug("project contents null: {} {}", project, project.getCachedInformationResources());
//                }
//            }
            
            SolrInputDocument document = null;
            if (item instanceof Person) {
                document = PersonDocumentConverter.convert((Person)item);
            }
            if (item instanceof Institution) {
                document = InstitutionDocumentConverter.convert((Institution)item);
            }
            if (item instanceof Resource) {
                document = ResourceDocumentConverter.convert((Resource)item, resourceService, resourceCollectionService);
            }
            if (item instanceof ResourceCollection) {
                document = CollectionDocumentConverter.convert((ResourceCollection)item);
            }
            if (item instanceof Keyword) {
                document = KeywordDocumentConverter.convert((Keyword)item);
            }
            if (item instanceof ResourceAnnotationKey) {
                document = AnnotationKeyDocumentConverter.convert((ResourceAnnotationKey)item);
            }
            template.add(core,document);
            return document;
        } catch (Throwable t) {
            logger.error("error ocurred in indexing", t);
        }
        return null;
    }

    /**
     * Reindex a set of @link ResourceCollection Entries and their subtrees to update rights and permissions
     * 
     * @param collectionToReindex
     */
    @Transactional
    public void indexAllResourcesInCollectionSubTree(ResourceCollection collectionToReindex) {
        logger.trace("indexing collection async");
        Long count = resourceCollectionDao.countAllResourcesInCollectionAndSubCollection(collectionToReindex);
        ScrollableResults results = resourceCollectionDao.findAllResourcesInCollectionAndSubCollectionScrollable(collectionToReindex);
        // FlushMode previousFlushMode = prepare(getFullTextSession());
        AsyncUpdateReceiver updateReceiver = getDefaultUpdateReceiver();
        indexScrollable(updateReceiver, null, 0f, 100f, Resource.class, count, results, true);
        complete(updateReceiver, null, null, null);
    }

    /**
     * Reindex a set of @link ResourceCollection Entries and their subtrees to update rights and permissions
     * 
     * @param collectionToReindex
     */
    @Async
    @Transactional
    public void indexAllResourcesInCollectionSubTreeAsync(final ResourceCollection collectionToReindex) {
        indexAllResourcesInCollectionSubTree(collectionToReindex);
    }

    /**
     * @see #indexCollection(Collection)
     * @param collectionToReindex
     * @throws IOException 
     * @throws SolrServerException 
     */
    @Async
    public <C extends Indexable> void indexCollectionAsync(final Collection<C> collectionToReindex) throws SolrServerException, IOException {
        indexCollection(collectionToReindex);
    }

    /**
     * help's calcualate the percentage complete
     * 
     * @param total
     * @return
     */
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

    /**
     * @see #indexCollection(Collection)
     * @param indexable
     * @throws IOException 
     * @throws SolrServerException 
     */
    @SuppressWarnings("unchecked")
    public <C extends Indexable> void index(C... indexable) throws SolrServerException, IOException {
        indexCollection(Arrays.asList(indexable));
    }

    /**
     * Index a collection of @link Indexable entities
     * 
     * @param indexable
     * @throws IOException 
     * @throws SolrServerException 
     */
    public <C extends Indexable> boolean indexCollection(Collection<C> indexable) throws SolrServerException, IOException {
        boolean exceptions = false;
        
        if (CollectionUtils.isNotEmpty(indexable)) {
            if (CollectionUtils.size(indexable) > 1) {
                logger.debug("manual indexing ... {}", indexable.size());
            }
            // FullTextSession fullTextSession = getFullTextSession();
            int count = 0;
            String core = "";
            for (C toIndex : indexable) {
                count++;
                core = getCoreForClass(toIndex.getClass());
                try {
                    // if we were called via async, the objects will belong to managed by the current hib session.
                    // purge them from the session and merge w/ transient object to get it back on the session before indexing.
                    index(toIndex, true);
                    if (count % FLUSH_EVERY == 0) {
                        logger.debug("indexing: {}", toIndex);
                        logger.debug("flush to index ... every {}", FLUSH_EVERY);
                    }
                } catch (Throwable e) {
                    logger.error("exception in indexing, {} [{}]", toIndex, e);
                    logger.error(String.format("%s %s", ExceptionUtils.getRootCauseMessage(e), Arrays.asList(ExceptionUtils.getRootCauseStackTrace(e))),
                            ExceptionUtils.getRootCause(e));
                    exceptions = true;
                }
            }
            logger.trace("begin flushing");
            // fullTextSession.flushToIndexes();
            UpdateResponse commit = template.commit(core);
            logger.trace("response: {}", commit.getResponseHeader());
//            processBatch(docs);
        }
        

        if (indexable != null && CollectionUtils.size(indexable) > 1) {
            logger.debug("Done indexing");
        }
        return exceptions;
    }

    private String getCoreForClass(Class<? extends Indexable> item) {
        if (Person.class.isAssignableFrom(item)) {
            return CoreNames.PEOPLE;
        }
        if (Institution.class.isAssignableFrom(item)) {
            return CoreNames.INSTITUTIONS;
        }
        if (Resource.class.isAssignableFrom(item)) {
            return CoreNames.RESOURCES;
        }
        if (ResourceCollection.class.isAssignableFrom(item)) {
            return CoreNames.COLLECTIONS;
        }
        if (Keyword.class.isAssignableFrom(item)) {
            return CoreNames.KEYWORDS;
        }
        if (ResourceAnnotationKey.class.isAssignableFrom(item)) {
            return CoreNames.ANNOTATION_KEY;
        }
        return null;
    }

//    private void processBatch(List<SolrInputDocument> docs) throws SolrServerException, IOException {
//        UpdateRequest req = new UpdateRequest();
//        req.setAction( UpdateRequest.ACTION.COMMIT, false, false );
//        req.add( docs );
//        UpdateResponse rsp = req.process( template );
//        logger.error("resp: {}", rsp);
//    }

    /**
     * Similar to @link GenericService.synchronize() forces all pending indexing actions to be written.
     * 
     * Should only be used in tests...
     * 
     */
    @Deprecated
    public void flushToIndexes() {
        // getFullTextSession().flushToIndexes();
    }

    /**
     * Index/Reindex everything. Requested by the @link Person
     * 
     * @param person
     */
    public void indexAll(Person person) {
        indexAll(getDefaultUpdateReceiver(), Arrays.asList(LookupSource.values()), person);
    }

    /**
     * Index all items of the Specified Class; person is the person requesting the index
     * 
     * @param person
     * @param classes
     */
    @SuppressWarnings("unchecked")
    public void indexAll(Person person, LookupSource ... sources) {
        indexAll(getDefaultUpdateReceiver(), Arrays.asList(sources), person);
    }

    /**
     * The AsyncUpdateReciever allows us to pass data about the indexing back to the requester. The default one does nothing.
     * 
     * @return
     */
    private AsyncUpdateReceiver getDefaultUpdateReceiver() {
        return AsyncUpdateReceiver.DEFAULT_RECEIVER;
    }

    /**
     * Wipe everything from the index
     * 
     */
    public void purgeAll() {
        purgeAll(LookupSource.values());
    }

    /**
     * Purge all objects of the specified Class frmo the index
     * 
     * @param classes
     */
    public void purgeAll(LookupSource ... sources) {
        for (LookupSource src : sources) {
            for (Class<? extends Indexable> clss : src.getClasses()) {
                purgeCore(getCoreForClass(clss));
            }
        }
    }

    private void purgeCore(String core) {
        try {
            template.deleteByQuery(core, "*:*");
            template.commit(core);
        } catch (SolrServerException | IOException e) {
            logger.error("error purging index: {}", core, e);
        }
    }

    /**
     * Optimizes all lucene indexes
     * 
     */
    public void optimizeAll() {
        for (Class<? extends Indexable> toIndex : getDefaultClassesToIndex()) {
            logger.info("optimizing {}", toIndex.getSimpleName());
            String core = getCoreForClass(toIndex);
            try {
                template.optimize(core);
            } catch (SolrServerException | IOException e) {
                logger.error("error in optimize of {}", core, e);
            }
        }
    }

    /**
     * Indexes a @link Project and it's contents. It loads the project's child @link Resource entries before indexing
     * 
     * @param project
     * @throws IOException 
     * @throws SolrServerException 
     */
    public boolean indexProject(Project project) throws SolrServerException, IOException {
        setupProjectForIndexing(project);
        index(project);
        logger.debug("reindexing project contents");
        int total = 0;
        ScrollableResults scrollableResults = projectDao.findAllResourcesInProject(project);
        indexScrollable(getDefaultUpdateReceiver(), null, 0f, 100f, Resource.class, total, scrollableResults, true);
        logger.debug("completed reindexing project contents");
        return false;
    }

    private void setupProjectForIndexing(Project project) {
        Collection<InformationResource> irs = new ImmutableScrollableCollection<InformationResource>(projectDao.findAllResourcesInProject(project,
                Status.ACTIVE,
                Status.DRAFT));
        project.setCachedInformationResources(irs);
        project.setReadyToIndex(true);
    }

    /**
     * @see #indexProject(Project)
     * @param project
     * @throws IOException 
     * @throws SolrServerException 
     */
    @Async
    @Transactional(readOnly = true)
    public void indexProjectAsync(final Project project) throws SolrServerException, IOException {
        indexProject(project);
    }

    @Transactional(readOnly = true)
    public boolean indexProject(Long id) throws SolrServerException, IOException {
        return indexProject(genericDao.find(Project.class, id));
    }

    @Async
    @Transactional(readOnly = false)
    public void indexAllAsync(final AsyncUpdateReceiver reciever, final List<LookupSource> toReindex, final Person person) {
        logger.info("reindexing indexall");
        indexAll(reciever, toReindex, person);
        sendEmail(toReindex);

    }

    @Transactional(readOnly = false)
    public void sendEmail(final List<LookupSource> toReindex) {
        Date date = new Date();
        TdarConfiguration CONFIG = TdarConfiguration.getInstance();
        if (CONFIG.isProductionEnvironment()) {
            Email email = new Email();
            email.setSubject(INDEXING_COMPLETED);
            email.setMessage(String.format(INDEXING_STARTED, toReindex, CONFIG.getHostName(), date, new Date()));
            email.setUserGenerated(false);
            emailService.send(email);
        }
    }

    @Transactional(readOnly=true)
    public void purge(Indexable entity) throws SolrServerException, IOException {
        String core = getCoreForClass(entity.getClass());
        template.deleteById(core,generateId(entity));
//        if (entity instanceof ResourceCollection) {
//            ResourceCollection rc = (ResourceCollection)entity;
//            
//        }
        // TODO Auto-generated method stub
        
    }
}
