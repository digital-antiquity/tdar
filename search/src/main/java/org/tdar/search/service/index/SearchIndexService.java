package org.tdar.search.service.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.hibernate.ScrollableResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.AsyncUpdateReceiver;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.dao.resource.ProjectDao;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.event.EventType;
import org.tdar.core.event.TdarEvent;
import org.tdar.core.service.ActivityManager;
import org.tdar.core.service.event.EventBusResourceHolder;
import org.tdar.core.service.event.EventBusUtils;
import org.tdar.core.service.event.TxMessageBus;
import org.tdar.core.service.external.EmailService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.search.converter.AnnotationKeyDocumentConverter;
import org.tdar.search.converter.CollectionDocumentConverter;
import org.tdar.search.converter.ContentDocumentConverter;
import org.tdar.search.converter.DataValueDocumentConverter;
import org.tdar.search.converter.InstitutionDocumentConverter;
import org.tdar.search.converter.IntegrationDocumentConverter;
import org.tdar.search.converter.KeywordDocumentConverter;
import org.tdar.search.converter.PersonDocumentConverter;
import org.tdar.search.converter.ResourceDocumentConverter;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.service.CoreNames;
import org.tdar.search.service.SearchUtils;
import org.tdar.utils.PersistableUtils;

@Service
@Transactional(readOnly = true)
public class SearchIndexService implements TxMessageBus<SolrDocumentContainer> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    public static final String INDEXING_COMPLETED = "indexing completed";
    public static final String INDEXING_STARTED = "indexing of %s on %s complete.\n Started: %s \n Completed: %s";
    private static final TdarConfiguration CONFIG = TdarConfiguration.getInstance();

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ResourceCollectionDao resourceCollectionDao;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private DatasetDao datasetDao;

    @Autowired
    private ProjectDao projectDao;
    private boolean useTransactionalEvents = true;

    private static final int FLUSH_EVERY = CONFIG.getIndexerFlushSize();
    public static final String BUILD_LUCENE_INDEX_ACTIVITY_NAME = "Build Lucene Search Index";

    @Transactional(readOnly = true)
    public void indexAll(AsyncUpdateReceiver updateReceiver, TdarUser person) {
        indexAll(updateReceiver, Arrays.asList(LookupSource.values()), person);
    }

    @Transactional(readOnly = true)
    public void indexAll(AsyncUpdateReceiver updateReceiver, List<LookupSource> toReindex, TdarUser person) {
        BatchIndexer batch = new BatchIndexer(genericDao, datasetDao, this);
        batch.indexAll(updateReceiver, Arrays.asList(LookupSource.values()), person);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @EventListener
    public void handleIndexingEvent(TdarEvent event) throws Exception {
        if (!(event.getRecord() instanceof Indexable)) {
            return;
        }

        Indexable record = (Indexable) event.getRecord();

        if (PersistableUtils.isNullOrTransient(record)) {
            return;
        }

        if (!isUseTransactionalEvents() || !CONFIG.useTransactionalEvents()) {
            index(record);
            return;
        }

        Optional<EventBusResourceHolder> holder = EventBusUtils.getTransactionalResourceHolder(this);
        SolrInputDocument doc = createDocument(record);
        //
        String recordId = generateId(record);
        File tempDirectory = CONFIG.getTempDirectory();
        File dir = new File(tempDirectory, "index");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File temp = new File(dir, String.format("%s-%s.ser", recordId, System.nanoTime()));
        temp.deleteOnExit();

        SerializationUtils.serialize(doc, new FileOutputStream(temp));
        SolrDocumentContainer container = new SolrDocumentContainer(temp, recordId, event.getType(), LookupSource.getCoreForClass(record.getClass()));

        if (holder.isPresent()) {
            holder.get().addMessage(container);
        } else {
            post(container);
        }
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
            toReindex.addAll(Arrays.asList(source.getClasses()));
        }
        return toReindex;
    }

    /**
     * Index an item of some sort.
     * 
     * @param fullTextSession
     * @param item
     */
    SolrInputDocument index(LookupSource src, final Indexable item, boolean deleteFirst) {
        if (item == null) {
            return null;
        }
        try {
            String core = LookupSource.getCoreForClass(item.getClass());

            SolrInputDocument document = createDocument(item);

            if (Objects.equals(src, LookupSource.DATA)) {
                List<SolrInputDocument> convert = DataValueDocumentConverter.convert((InformationResource) item,
                        resourceService);
                template.add(CoreNames.DATA_MAPPINGS, convert);
                if (deleteFirst) {
                    template.deleteByQuery(CoreNames.DATA_MAPPINGS, "id:" + item.getId());
                }
                return null;
            }

            if (document == null) {
                return null;
            }
            index(core, generateId(item), document);
            return document;
        } catch (Throwable t) {
            logger.error("error ocurred in indexing", t);
        }
        return null;
    }

    private void index(String core, String id, SolrInputDocument doc) throws SolrServerException, IOException {
        purge(core, id);
        template.add(core, doc);
    }

    private SolrInputDocument createDocument(final Indexable item) {
        SolrInputDocument document = null;
        if (item instanceof Person) {
            document = PersonDocumentConverter.convert((Person) item);
        }
        if (item instanceof Institution) {
            document = InstitutionDocumentConverter.convert((Institution) item);
        }
        if (item instanceof Resource) {
            document = ResourceDocumentConverter.convert((Resource) item);
        }
        if (item instanceof DataIntegrationWorkflow) {
            document = IntegrationDocumentConverter.convert((DataIntegrationWorkflow) item);
        }
        if (item instanceof ResourceCollection) {
            document = CollectionDocumentConverter.convert((ResourceCollection) item);
        }
        if (item instanceof Keyword) {
            document = KeywordDocumentConverter.convert((Keyword) item);
        }
        if (item instanceof ResourceAnnotationKey) {
            document = AnnotationKeyDocumentConverter.convert((ResourceAnnotationKey) item);
        }
        if (item instanceof InformationResourceFile) {
            document = ContentDocumentConverter.convert((InformationResourceFile) item);
        }

        return document;
    }

    /**
     * Reindex a set of @link ResourceCollection Entries and their subtrees to
     * update rights and permissions
     * 
     * @param collectionToReindex
     */
    @Transactional
    public void indexAllResourcesInCollectionSubTree(ResourceCollection collectionToReindex) {
        logger.trace("indexing collection async");
        Long total = resourceCollectionDao.countAllResourcesInCollectionAndSubCollection(collectionToReindex);
        ScrollableResults results = resourceCollectionDao
                .findAllResourcesInCollectionAndSubCollectionScrollable(collectionToReindex);
        BatchIndexer batch = new BatchIndexer(genericDao, datasetDao, this);
        batch.indexScrollable(total, Resource.class, results);
    }

    /**
     * Reindex a set of @link ResourceCollection Entries and their subtrees to
     * update rights and permissions
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
    public <C extends Indexable> void indexCollectionAsync(final Collection<C> collectionToReindex)
            throws SearchIndexException, IOException {
        indexCollection(collectionToReindex);
    }

    /**
     * @see #indexCollection(Collection)
     * @param indexable
     * @throws IOException
     * @throws SolrServerException
     */
    @SuppressWarnings("unchecked")
    public <C extends Indexable> void index(C... indexable) throws SearchIndexException, IOException {
        indexCollection(Arrays.asList(indexable));
    }

    /**
     * Index a collection of @link Indexable entities
     * 
     * @param indexable
     * @throws IOException
     * @throws SolrServerException
     */
    public <C extends Indexable> boolean indexCollection(Collection<C> indexable)
            throws SearchIndexException, IOException {
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
                core = LookupSource.getCoreForClass(toIndex.getClass());
                try {
                    // if we were called via async, the objects will belong to
                    // managed by the current hib session.
                    // purge them from the session and merge w/ transient object
                    // to get it back on the session before indexing.
                    if (genericDao.sessionContains(toIndex)) {
                        index(null, toIndex, true);
                    } else {
                        index(null, genericDao.merge(toIndex), true);
                    }
                    if (count % FLUSH_EVERY == 0) {
                        logger.debug("indexing: {}", toIndex);
                        logger.debug("flush to index ... every {}", FLUSH_EVERY);
                    }
                } catch (Throwable e) {
                    logger.error("exception in indexing, {} [{}]", toIndex, e);
                    logger.error(
                            String.format("%s %s", ExceptionUtils.getRootCauseMessage(e),
                                    Arrays.asList(ExceptionUtils.getRootCauseStackTrace(e))),
                            ExceptionUtils.getRootCause(e));
                    exceptions = true;
                }
            }
            logger.trace("begin flushing");
            // fullTextSession.flushToIndexes();
            commit(core);
            // processBatch(docs);
        }

        logger.debug("Done indexing: {} items ", CollectionUtils.size(indexable));
        return exceptions;
    }

    void commit(String core) throws SearchIndexException, IOException {
        try {
            UpdateResponse commit = template.commit(core);
            logger.trace("response: {}", commit.getResponseHeader());
        } catch (Exception e) {
            throw new SearchIndexException("issue committing", e);
        }
    }

    /**
     * Index/Reindex everything. Requested by the @link Person
     * 
     * @param person
     */
    public void indexAll(TdarUser person) {
        BatchIndexer batch = new BatchIndexer(genericDao, datasetDao, this);
        batch.indexAll(getDefaultUpdateReceiver(), Arrays.asList(LookupSource.values()), person);
    }

    /**
     * Index all items of the Specified Class; person is the person requesting
     * the index
     * 
     * @param person
     * @param classes
     */
    public void indexAll(TdarUser person, LookupSource... sources) {
        BatchIndexer batch = new BatchIndexer(genericDao, datasetDao, this);
        batch.indexAll(getDefaultUpdateReceiver(), Arrays.asList(sources), person);
    }

    /**
     * The AsyncUpdateReciever allows us to pass data about the indexing back to
     * the requester. The default one does nothing.
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
    public void purgeAll(LookupSource... sources) {
        for (LookupSource src : sources) {
            purgeCore(src);
        }
    }

    void purgeCore(LookupSource src) {
        try {
            // in most cases this is *:*, but for the shared core (Resources/Collections) it is limited by type
            template.deleteByQuery(src.getCoreName(), src.getDeleteQuery());
            commit(src.getCoreName());
        } catch (Exception e) {
            logger.error("error purging index: {}", src.getCoreName(), e);
        }
    }

    /**
     * Optimizes all lucene indexes
     * 
     */
    public void optimizeAll() {
        for (Class<? extends Indexable> toIndex : getDefaultClassesToIndex()) {
            logger.info("optimizing {}", toIndex.getSimpleName());
            String core = LookupSource.getCoreForClass(toIndex);
            try {
                template.optimize(core);
            } catch (SolrServerException | IOException e) {
                logger.error("error in optimize of {}", core, e);
            }
        }
    }

    /**
     * Indexes a @link Project and it's contents. It loads the project's
     * child @link Resource entries before indexing
     * 
     * @param project
     * @throws IOException
     * @throws SolrServerException
     */
    public boolean indexProject(Project project) throws SearchIndexException, IOException {
        index(project);
        logger.debug("reindexing project contents");
        ScrollableResults scrollableResults = projectDao.findAllResourcesInProject(project);
        BatchIndexer batch = new BatchIndexer(genericDao, datasetDao, this);
        batch.indexScrollable(0L, Resource.class, scrollableResults);
        logger.debug("completed reindexing project contents");
        return false;
    }

    /**
     * @see #indexProject(Project)
     * @param project
     * @throws IOException
     * @throws SolrServerException
     */
    @Async
    @Transactional(readOnly = true)
    public void indexProjectAsync(final Project project) throws SearchIndexException, IOException {
        indexProject(project);
    }

    @Transactional(readOnly = true)
    public boolean indexProject(Long id) throws SearchIndexException, IOException {
        return indexProject(genericDao.find(Project.class, id));
    }

    @Async
    @Transactional(readOnly = false)
    public void indexAllAsync(final AsyncUpdateReceiver reciever, final List<LookupSource> toReindex,
            final TdarUser person) {
        Date startDate = new Date();
        logger.info("reindexing indexall");
        BatchIndexer batch = new BatchIndexer(genericDao, datasetDao, this);
        batch.indexAll(reciever, toReindex, person);
        sendEmail(startDate, toReindex);

    }

    @Transactional(readOnly = false)
    public void sendEmail(Date date, final List<LookupSource> toReindex) {
        if (CONFIG.isProductionEnvironment()) {
            Email email = new Email();
            email.setSubject(INDEXING_COMPLETED);
            email.setMessage(String.format(INDEXING_STARTED, toReindex, CONFIG.getHostName(), date, new Date()));
            email.setUserGenerated(false);
            emailService.send(email);
        }
    }

    @Transactional(readOnly = true)
    public void purge(Indexable entity) throws SolrServerException, IOException {
        String core = LookupSource.getCoreForClass(entity.getClass());
        purge(core, generateId(entity));
    }

    @Transactional(readOnly = true)
    public void purge(String core, String id) throws SolrServerException, IOException {
        template.deleteById(core, id);
    }

    @Transactional(readOnly = true)
    public void clearIndexingActivities() {
        ActivityManager.getInstance().clearIndexingActivities();
    }

    public boolean isUseTransactionalEvents() {
        return useTransactionalEvents;
    }

    public void setUseTransactionalEvents(boolean useTransactionalEvents) {
        this.useTransactionalEvents = useTransactionalEvents;
    }

    @Override
    public void post(SolrDocumentContainer o) throws Exception {

        Object object = SerializationUtils.deserialize(new FileInputStream(o.getDoc()));
        SolrInputDocument doc = (SolrInputDocument) object;
        if (doc == null || !doc.containsKey(QueryFieldNames._ID)) {
            logger.trace("possibly null doc:{}", doc);
            return;
        }
        String id = (String) doc.getField(QueryFieldNames._ID).getFirstValue();
        String core = o.getCore();
        if (o.getEventType() == EventType.DELETE) {
            purge(core, id);
        } else {
            logger.trace("indexing {}, {}, {}", core, id, doc);
            index(core, id, doc);
        }
        commit(core);
    }

    /**
     * Solr supports indexing parts of documents, we can use this to optimize collection indexing which will
     * only ever change a few fields in a resources...
     * 
     * https://cwiki.apache.org/confluence/display/solr/Updating+Parts+of+Documents
     * http://blog.thedigitalgroup.com/ujwalap/2015/05/11/atomic-updates-in-solr/
     * 
     * @param persistable
     */
    @Transactional(readOnly = true)
    public void partialIndexAllResourcesInCollectionSubTree(ResourceCollection persistable) {
        Long total = resourceCollectionDao.countAllResourcesInCollectionAndSubCollection(persistable);
        logger.debug("partially indexing {} resources from {} ({})", total, persistable.getName(), persistable.getId());
        ScrollableResults results = resourceCollectionDao.findAllResourcesInCollectionAndSubCollectionScrollable(persistable);
        int numProcessed = 0;
        String coreName = LookupSource.RESOURCE.getCoreName();
        while (results.next()) {
            Resource r = (Resource) results.get(0);
            SolrInputDocument doc = ResourceDocumentConverter.replaceCollectionFields(r);
            try {
                template.add(LookupSource.RESOURCE.getCoreName(), doc);
            } catch (SolrServerException | IOException e1) {
                logger.error("error adding: {}", e1);
            }
            if ((numProcessed % FLUSH_EVERY) == 0) {
                logger.trace("flushing search index");
                commitAndClearSession(coreName);
                logger.trace("flushed search index");
            }
            numProcessed++;

        }
        commitAndClearSession(coreName);
        logger.debug("completed partial indexing of {} ({})", total, persistable.getName(), persistable.getId());

    }

    private void commitAndClearSession(String coreName) {
        try {
            commit(coreName);
        } catch (SearchIndexException | IOException e) {
            logger.error("error in partial index: {}", e);
        } finally {
            genericDao.clearCurrentSession();
        }
    }

    @Transactional(readOnly = true)
    @Async
    public void partialIndexAllResourcesInCollectionSubTreeAsync(ResourceCollection persistable) {
        indexAllResourcesInCollectionSubTree(persistable);
    }

    public void partialIndexProject() {
        ScrollableResults results = datasetDao.findAllResourceWithProjectsScrollable();
        int numProcessed = 0;
        String coreName = LookupSource.RESOURCE.getCoreName();
        while (results.next()) {
            InformationResource r = (InformationResource) results.get(0);
            SolrInputDocument doc = ResourceDocumentConverter.replaceProjectFields(r);
            try {
                template.add(LookupSource.RESOURCE.getCoreName(), doc);
            } catch (SolrServerException | IOException e1) {
                logger.error("error adding: {}", e1);
            }
            if ((numProcessed % FLUSH_EVERY) == 0) {
                logger.debug("flushing search index - partial: {}", numProcessed);
                commitAndClearSession(coreName);
                logger.trace("flushed search index");
            }
            numProcessed++;

        }
        commitAndClearSession(coreName);
        logger.debug("completed partial indexing of projectTitle");

    }

}
