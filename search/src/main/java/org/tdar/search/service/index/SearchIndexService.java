package org.tdar.search.service.index;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.AsyncUpdateReceiver;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.service.event.TxMessageBus;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.index.LookupSource;

@Service
@Transactional(readOnly = true)
public interface SearchIndexService extends TxMessageBus<SolrDocumentContainer> {

    public List<Class<? extends Indexable>> getDefaultClassesToIndex();

    public List<Class<? extends Indexable>> getClassesToReindex(List<LookupSource> values);

    public void indexAllResourcesInCollectionSubTree(ResourceCollection collectionToReindex);

    public void indexAllResourcesInCollectionSubTreeAsync(final ResourceCollection collectionToReindex);

    public <C extends Indexable> void indexCollectionAsync(final Collection<C> collectionToReindex) throws SearchIndexException, IOException;

    public <C extends Indexable> void index(C... indexable) throws SearchIndexException, IOException;

    public <C extends Indexable> boolean indexCollection(Collection<C> indexable)
            throws SearchIndexException, IOException;

    public void indexAll(TdarUser person);

    public void indexAll(TdarUser person, LookupSource... sources);

    public void indexAll(AsyncUpdateReceiver update, TdarUser person);

    public void purgeAll();

    public void purgeAll(LookupSource... sources);

    public void optimizeAll();

    public boolean indexProject(Project project) throws SearchIndexException, IOException;

    public void indexProjectAsync(final Project project) throws SearchIndexException, IOException;

    public boolean indexProject(Long id) throws SearchIndexException, IOException;

    public void indexAllAsync(final AsyncUpdateReceiver reciever, final List<LookupSource> toReindex,
            final TdarUser person);
    
    public void indexAll(final AsyncUpdateReceiver reciever, final List<LookupSource> toReindex,
            final TdarUser person);

    public void sendEmail(Date date, final List<LookupSource> toReindex);

    public void purge(Indexable entity) throws SolrServerException, IOException;

    public void purge(String core, String id) throws SolrServerException, IOException;

    public void clearIndexingActivities();

    public boolean isUseTransactionalEvents();

    public void setUseTransactionalEvents(boolean useTransactionalEvents);

    public void post(SolrDocumentContainer o) throws Exception;

    public void partialIndexAllResourcesInCollectionSubTree(ResourceCollection persistable);

    public void partialIndexAllResourcesInCollectionSubTreeAsync(ResourceCollection persistable);

    public void partialIndexProject();

    public void purgeCore(LookupSource src);

    public SolrInputDocument index(LookupSource src, Indexable item, boolean deleteFirst);

    public void commit(String coreForClass) throws SearchIndexException, IOException;

}
