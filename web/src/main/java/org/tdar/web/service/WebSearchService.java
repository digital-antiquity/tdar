package org.tdar.web.service;

import java.io.IOException;

import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.service.AsynchronousStatus;
import org.tdar.search.bean.AdvancedSearchQueryObject;
import org.tdar.search.exception.SearchException;

public interface WebSearchService {

    AsynchronousStatus saveSearchResultsForUserAsync(AdvancedSearchQueryObject asqo, Long userId, Long resourceCollectionId,
            boolean addAsManagedResource) throws SearchException, IOException;

    AsynchronousStatus saveSearchResultsForUser(AdvancedSearchQueryObject asqo, Long userId, Long resourceCollectionId,
            boolean addAsManagedResource) throws SearchException, IOException;

    /**
     * This is a unique identifier for the Async status object.
     * 
     * @param collectionId
     * @param userId
     * @return
     */
    String constructKey(Long collectionId, Long userId);

}