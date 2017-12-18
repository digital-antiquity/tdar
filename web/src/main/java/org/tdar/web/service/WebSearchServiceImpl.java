package org.tdar.web.service;

import java.io.IOException;
import java.util.List;
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
import org.tdar.core.event.EventType;
import org.tdar.core.event.TdarEvent;
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
import org.tdar.search.service.query.ResourceSearchService;
import org.tdar.utils.MessageHelper;

@Service
public class WebSearchServiceImpl {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private GenericService genericService;

	@Autowired
	private ResourceSearchService resourceSearchService;

	@Autowired
	private ResourceCollectionService resourceCollectionService;

	@Autowired
	private AuthorizationService authorizationService;

	private Integer maxNumOfRecords = 500;

	@Autowired
	private ApplicationEventPublisher publisher;

	@Transactional(readOnly = false)
	@Async
	public void saveSearchResultsForUserAsync(AdvancedSearchQueryObject asqo, Long userId, Long resourceCollectionId,
			boolean addAsManagedResource) throws SearchException, IOException {
		logger.debug("called saveSearchResultsForUserAsync");

		AsynchronousStatus status = new AsynchronousStatus(constructKey(resourceCollectionId, userId));
		AsynchronousProcessManager.getInstance().addActivityToQueue(status);

		try {
			TdarUser user = genericService.find(TdarUser.class, userId);
			LuceneSearchResultHandler<Resource> result = new SearchResult<>();
			result.setRecordsPerPage(maxNumOfRecords);

			resourceSearchService.buildAdvancedSearch(asqo, user, result, MessageHelper.getInstance());

			ResourceCollection collection = genericService.find(ResourceCollection.class, resourceCollectionId);

			if (!authorizationService.canAddToCollection(user, collection)) {
				throw new TdarAuthorizationException("No permission to add to collection");
			}

			Integer totalRecords = CollectionUtils.size(result.getResults());
			Integer recordsProcessed = 0;

			logger.debug("There are {} total records to process");
			for (Resource resource : result.getResults()) {
				recordsProcessed++;

				CollectionResourceSection section = CollectionResourceSection.UNMANAGED;
				Set<ResourceCollection> current = resource.getUnmanagedResourceCollections();

				if (addAsManagedResource && authorizationService.canEdit(user, resource)) {
					section = CollectionResourceSection.MANAGED;
					current = resource.getManagedResourceCollections();
				}
				Float percentDone = ((float) recordsProcessed / totalRecords)*100;
				status.setPercentComplete(percentDone);
				status.update(status.getPercentComplete(), String.format("saving %s", resource.getTitle()));

				logger.debug("{} percent complete", percentDone);

				resourceCollectionService.addResourceCollectionToResource(resource, current, user, true,
						ErrorHandling.NO_VALIDATION, collection, section);
				// publisher.publishEvent(new TdarEvent(resource,
				// EventType.CREATE_OR_UPDATE));
			}
			status.setPercentComplete(100f);
			status.setCompleted();
		} catch (Throwable t) {
			logger.debug("An error happened during adding resources");
			logger.debug("{}",t);
			status.addError(t);
			status.setCompleted();
			status.setPercentComplete(100f);
		}

		// Update status?
	}

	public String constructKey(Long collectionId, Long userId) {
		return "SaveSearchResult::" + collectionId + "::" + userId;
	}

}
