package org.tdar.search.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
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
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.KeywordType;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.resource.ProjectDao;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.service.ActivityManager;
import org.tdar.core.service.external.EmailService;
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
    private ProjectDao projectDao;

    private static final int FLUSH_EVERY = TdarConfiguration.getInstance().getIndexerFlushSize();

    private static final int INDEXER_BATCH_SIZE_TO_LOAD_OBJECTS = 50;
    private static final int INDEXER_THREADS_FOR_SUBSEQUENT_FETCHING = 5;
    private static final int INDEXER_THREADS_TO_LOAD_OBJECTS = 5;

    public static final String BUILD_LUCENE_INDEX_ACTIVITY_NAME = "Build Lucene Search Index";

    public void indexAll(AsyncUpdateReceiver updateReceiver, Person person) {
        indexAll(updateReceiver, getDefaultClassesToIndex(), person);
    }

    @Autowired
    private SolrClient template;

    public void indexAllPeople() throws SolrServerException, IOException {
        List<Person> findAll = genericDao.findAll(Person.class);
        for (Person person : findAll) {
            template.deleteById(generateId(person));
            SolrInputDocument document = PersonDocumentConverter.convert(person);
            template.add(document);
            logger.trace("adding: " + person.getId() + " " + person.getProperName());
        }
        template.commit();
    }

    public void indexAllInstitutions() throws SolrServerException, IOException {
        List<Institution> findAll = genericDao.findAll(Institution.class);
        for (Institution inst : findAll) {
            template.deleteById(generateId(inst));
            SolrInputDocument document = InstitutionDocumentConverter.convert(inst);
            template.add("institutions", document);
            logger.debug("adding: " + inst.getId() + " " + inst.getProperName());
        }
        template.commit();
    }

    public void indexAllKeywords() throws SolrServerException, IOException {
        for (KeywordType type : KeywordType.values()) {
            List<? extends Keyword> findAll = genericDao.findAll(type.getKeywordClass());
            for (Keyword kwd : findAll) {
                template.deleteById(generateId(kwd));
                SolrInputDocument document = KeywordDocumentConverter.convert(kwd);
                template.add("keywords", document);
                logger.debug("adding: " + kwd.getId() + " " + kwd.getLabel());
            }
            template.commit();
        }
    }

    public void indexAllCollections() throws SolrServerException, IOException {
        for (ResourceCollection collection : genericDao.findAll(ResourceCollection.class)) {
            if (collection.getType() != CollectionType.SHARED) {
                continue;
            }
            template.deleteById(generateId(collection));
            SolrInputDocument document = CollectionDocumentConverter.convert(collection);
            template.add("collections", document);
            logger.debug("adding: " + collection.getId() + " " + collection.getName());
        }
        template.commit();
    }

    public void indexAllResources() throws SolrServerException, IOException {
        for (Resource resource : genericDao.findAll(Resource.class)) {
            template.deleteById(generateId(resource));
            SolrInputDocument document = ResourceDocumentConverter.convert(resource);
            template.add("resources", document);
            logger.debug("adding: " + resource.getId() + " " + resource.getName());
        }
        template.commit();
    }

    private String generateId(Persistable pers) {
        return pers.getClass().getSimpleName() + "-" + pers.getId();
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
    public void indexAll(AsyncUpdateReceiver updateReceiver, List<Class<? extends Indexable>> classesToIndex, Person person) {
        if (updateReceiver == null) {
            updateReceiver = getDefaultUpdateReceiver();
        }
        Activity activity = new Activity();
        activity.setName(BUILD_LUCENE_INDEX_ACTIVITY_NAME);
        activity.setIndexingActivity(true);
        activity.setUser(person);
        activity.setMessage(String.format("reindexing %s", StringUtils.join(classesToIndex, ", ")));
        activity.start();
        ActivityManager.getInstance().addActivityToQueue(activity);

        try {
            genericDao.synchronize();
            // solrTemplate.
            // FullTextSession fullTextSession = getFullTextSession();
            // FlushMode previousFlushMode = prepare(fullTextSession);
            // SearchFactory sf = fullTextSession.getSearchFactory();
            float percent = 0f;
            updateAllStatuses(updateReceiver, activity, "initializing...", 0f);
            float maxPer = (1f / classesToIndex.size()) * 100f;
            for (Class<?> toIndex : classesToIndex) {
                // fullTextSession.purgeAll(toIndex);
                // sf.optimize(toIndex);
                Number total = genericDao.count(toIndex);
                ScrollableResults scrollableResults = genericDao.findAllScrollable(toIndex);
                indexScrollable(updateReceiver, activity, null, percent, maxPer, toIndex, total, scrollableResults, false);
                // fullTextSession.flushToIndexes();
                // fullTextSession.clear();
                percent += maxPer;
                String message = "finished indexing all " + toIndex.getSimpleName() + "(s).";
                updateAllStatuses(updateReceiver, activity, message, percent);
            }

            complete(updateReceiver, activity, null, null);
        } catch (Throwable ex) {
            logger.warn("exception: {}", ex);
            if (updateReceiver != null) {
                updateReceiver.addError(ex);
            }
        }
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
        // fullTextSession.flushToIndexes();
        // fullTextSession.clear();
        updateAllStatuses(updateReceiver, activity, "index all complete", 100f);
        // fullTextSession.setFlushMode(previousFlushMode);
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
    private void indexScrollable(AsyncUpdateReceiver updateReceiver, Activity activity, Object fullTextSession, float percent, float maxPer,
            Class<?> toIndex, Number total, ScrollableResults scrollableResults, boolean deleteFirst) {
        String message = total + " " + toIndex.getSimpleName() + "(s) to be indexed";
        updateAllStatuses(updateReceiver, activity, message, 0f);
        int divisor = getDivisor(total);
        float currentProgress = 0f;
        int numProcessed = 0;
        String MIDDLE = " of " + total.intValue() + " " + toIndex.getSimpleName() + "(s) ";
        Long prevId = 0L;
        Long currentId = 0L;
        while (scrollableResults.next()) {
            Indexable item = (Indexable) scrollableResults.get(0);
            currentId = item.getId();
            currentProgress = numProcessed / total.floatValue();
            index(fullTextSession, item, deleteFirst);
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
                // fullTextSession.flushToIndexes();
                // fullTextSession.clear();
                logger.trace("flushed search index");
            }
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
    private void index(Object fullTextSession, Indexable item, boolean deleteFirst) {
        try {
            if (deleteFirst) {
                // fullTextSession.purge(item.getClass(), item.getId());
            }

            if (item instanceof Project) {
                Project project = (Project) item;
                if (null == project.getCachedInformationResources()) {
                    setupProjectForIndexing(project);
                    // logger.debug("project contents null: {} {}", project, project.getCachedInformationResources());
                }
            }
            // fullTextSession.index(item);
        } catch (Throwable t) {
            logger.error("error ocurred in indexing", t);
            throw t;
        }
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
        indexScrollable(updateReceiver, null, null, 0f, 100f, Resource.class, count, results, true);
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
     */
    @Async
    public <C extends Indexable> void indexCollectionAsync(final Collection<C> collectionToReindex) {
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
     */
    @SuppressWarnings("unchecked")
    public <C extends Indexable> void index(C... indexable) {
        indexCollection(Arrays.asList(indexable));
    }

    /**
     * Index a collection of @link Indexable entities
     * 
     * @param indexable
     */
    public <C extends Indexable> boolean indexCollection(Collection<C> indexable) {
        boolean exceptions = false;
        if (indexable != null) {
            logger.debug("manual indexing ... {}", indexable.size());
            // FullTextSession fullTextSession = getFullTextSession();
            int count = 0;
            for (C toIndex : indexable) {
                count++;
                try {
                    // if we were called via async, the objects will belong to managed by the current hib session.
                    // purge them from the session and merge w/ transient object to get it back on the session before indexing.
                    index(null, genericDao.merge(toIndex), true);
                    if (count % FLUSH_EVERY == 0) {
                        logger.debug("indexing: {}", toIndex);
                        logger.debug("flush to index ... every {}", FLUSH_EVERY);
                        // fullTextSession.flushToIndexes();
                        // fullTextSession.clear();
                    }
                } catch (Throwable e) {
                    logger.error("exception in indexing, {} [{}]", toIndex, e);
                    logger.error(String.format("%s %s", ExceptionUtils.getRootCauseMessage(e), Arrays.asList(ExceptionUtils.getRootCauseStackTrace(e))),
                            ExceptionUtils.getRootCause(e));
                    exceptions = true;
                }
            }
            logger.debug("begin flushing");
            // fullTextSession.flushToIndexes();
        }
        logger.debug("Done indexing collection");
        return exceptions;
    }

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
        indexAll(getDefaultUpdateReceiver(), getDefaultClassesToIndex(), person);
    }

    /**
     * Index all items of the Specified Class; person is the person requesting the index
     * 
     * @param person
     * @param classes
     */
    @SuppressWarnings("unchecked")
    public void indexAll(Person person, Class<? extends Indexable>... classes) {
        indexAll(getDefaultUpdateReceiver(), Arrays.asList(classes), person);
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
        purgeAll(getDefaultClassesToIndex());
    }

    /**
     * Purge all objects of the specified Class frmo the index
     * 
     * @param classes
     */
    public void purgeAll(List<Class<? extends Indexable>> classes) {
        // FullTextSession fullTextSession = getFullTextSession();
        for (Class<?> clss : classes) {
            // fullTextSession.purgeAll(clss);
        }
    }

    /**
     * Optimizes all lucene indexes
     * 
     */
    public void optimizeAll() {
        // FullTextSession fullTextSession = getFullTextSession();
        // SearchFactory sf = fullTextSession.getSearchFactory();
        for (Class<?> toIndex : getDefaultClassesToIndex()) {
            // sf.optimize(toIndex);
            logger.info("optimizing {}", toIndex.getSimpleName());
        }
    }

    /**
     * Indexes a @link Project and it's contents. It loads the project's child @link Resource entries before indexing
     * 
     * @param project
     */
    public boolean indexProject(Project project) {
        setupProjectForIndexing(project);
        index(project);
        logger.debug("reindexing project contents");
        int total = 0;
        ScrollableResults scrollableResults = projectDao.findAllResourcesInProject(project);
        indexScrollable(getDefaultUpdateReceiver(), null, null, 0f, 100f, Resource.class, total, scrollableResults, true);
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
     */
    @Async
    @Transactional(readOnly = true)
    public void indexProjectAsync(final Project project) {
        indexProject(project);
    }

    @Transactional(readOnly = true)
    public boolean indexProject(Long id) {
        return indexProject(genericDao.find(Project.class, id));
    }

    @Async
    @Transactional(readOnly = false)
    public void indexAllAsync(final AsyncUpdateReceiver reciever, final List<Class<? extends Indexable>> toReindex, final Person person) {
        logger.info("reindexing indexall");
        indexAll(reciever, toReindex, person);
        sendEmail(toReindex);

    }

    @Transactional(readOnly = false)
    public void sendEmail(final List<Class<? extends Indexable>> toReindex) {
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
}
