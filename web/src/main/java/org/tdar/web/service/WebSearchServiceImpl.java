package org.tdar.web.service;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.collection.CollectionResourceSection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.exception.TdarAuthorizationException;
import org.tdar.core.service.AsynchronousProcessManager;
import org.tdar.core.service.AsynchronousStatus;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ErrorHandling;
import org.tdar.search.bean.AdvancedSearchQueryObject;
import org.tdar.search.exception.SearchException;
import org.tdar.search.query.LuceneSearchResultHandler;
import org.tdar.search.query.SearchResult;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.search.service.query.ResourceSearchService;
import org.tdar.utils.MessageHelper;

@Service
public class WebSearchServiceImpl implements WebSearchService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private GenericService genericService;

    @Autowired
    private ResourceSearchService resourceSearchService;

    @Autowired
    private ResourceCollectionService resourceCollectionService;

    @Autowired
    private AuthorizationService authorizationService;

    private Integer maxNumOfRecords = 1000;

    @Autowired
    private ApplicationEventPublisher publisher;
    
    @Autowired
    private SearchIndexService searchIndexService;
    
    /* (non-Javadoc)
     * @see org.tdar.web.service.WebSearchService#saveSearchResultsForUserAsync(org.tdar.search.bean.AdvancedSearchQueryObject, java.lang.Long, java.lang.Long, boolean)
     */
    @Override
    @Transactional(readOnly = false)
    @Async
    public AsynchronousStatus saveSearchResultsForUserAsync(AdvancedSearchQueryObject asqo, Long userId, Long resourceCollectionId,
            boolean addAsManagedResource) throws SearchException, IOException {
        return saveSearchResultsForUser(asqo, userId, resourceCollectionId, addAsManagedResource);
    }
    
    /* (non-Javadoc)
     * @see org.tdar.web.service.WebSearchService#saveSearchResultsForUser(org.tdar.search.bean.AdvancedSearchQueryObject, java.lang.Long, java.lang.Long, boolean)
     */
    @Override
    @Transactional(readOnly = false)
    public AsynchronousStatus saveSearchResultsForUser(AdvancedSearchQueryObject asqo, Long userId, Long resourceCollectionId,
            boolean addAsManagedResource) throws SearchException, IOException {
        logger.debug("called saveSearchResultsForUserAsync");

        AsynchronousStatus status = new AsynchronousStatus(constructKey(resourceCollectionId, userId));
        AsynchronousProcessManager.getInstance().addActivityToQueue(status);

        try {
            // Load the user and collection, and verify the user has permission to add to the collection.
            TdarUser user = genericService.find(TdarUser.class, userId);
            ResourceCollection collection = genericService.find(ResourceCollection.class, resourceCollectionId);
            if (!authorizationService.canAddToCollection(user, collection)) {
                throw new TdarAuthorizationException("No permission to add to collection");
            }

            // Construct a search query object
            LuceneSearchResultHandler<Resource> result = new SearchResult<>();
            result.setRecordsPerPage(maxNumOfRecords);
            resourceSearchService.buildAdvancedSearch(asqo, user, result, MessageHelper.getInstance());

            // Initalize the progress counters.
            Integer totalRecords = CollectionUtils.size(result.getResults());
            Integer recordsProcessed = 0;
            logger.debug("There are {} total records to process");

            // Iterate over the search results and add the resources to the collection.
            for (Resource resource : result.getResults()) {
                CollectionResourceSection sectionToAddTo = CollectionResourceSection.UNMANAGED;
                Set<ResourceCollection> currentResources = resource.getUnmanagedResourceCollections();

                // If the user has permission to the resource, then add the resource to the managed section of the collection.
                // Otherwise it will stay in the unmanaged.
                if (addAsManagedResource && authorizationService.canEdit(user, resource)) {
                    sectionToAddTo = CollectionResourceSection.MANAGED;
                    currentResources = resource.getManagedResourceCollections();
                }

                // Update the progress status.
                recordsProcessed++;
                Float percentDone = ((float) recordsProcessed / totalRecords) * 100;
                status.setPercentComplete(percentDone);
                status.setMessage(String.format("Saving %s of %s records", recordsProcessed,totalRecords));
                status.update(status.getPercentComplete(), String.format("saving %s", resource.getTitle()));
                logger.debug("{} percent complete", percentDone);

                // Add the resource to the collection and publish the event to add index.
                resourceCollectionService.addResourceCollectionToResource(resource, currentResources, user, true, ErrorHandling.NO_VALIDATION, collection,
                        sectionToAddTo);
            }
            
            // Update and set the status as completed.
            searchIndexService.indexAllResourcesInCollectionSubTree(collection);
            status.setPercentComplete(100f);
            status.setCompleted();
        } catch (Throwable t) {
            logger.debug("An error happened during adding resources");
            logger.debug("{}", t);
            status.addError(t);
            status.setCompleted();
            status.setPercentComplete(100f);
        }
        return status;
    }

    /* (non-Javadoc)
     * @see org.tdar.web.service.WebSearchService#constructKey(java.lang.Long, java.lang.Long)
     */
    @Override
    public String constructKey(Long collectionId, Long userId) {
        return "SaveSearchResult::" + collectionId + "::" + userId;
    }

	public SearchIndexService getSearchIndexService() {
		return searchIndexService;
	}

	public void setSearchIndexService(SearchIndexService searchIndexService) {
		this.searchIndexService = searchIndexService;
	}
}
