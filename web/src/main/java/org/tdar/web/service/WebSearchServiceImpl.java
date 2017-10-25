package org.tdar.web.service;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.collection.CollectionResourceSection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.exception.TdarAuthorizationException;
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

	@Autowired 
	private GenericService genericService;
	
	@Autowired
	private ResourceSearchService resourceSearchService;

	@Autowired
	private ResourceCollectionService resourceCollectionService;
	
	@Autowired
	private AuthorizationService authorizationService;
	
	private Integer maxNumOfRecords = 500;
	
	@Transactional(readOnly=false)
	@Async
	public void saveSearchResultsForUserAsync(AdvancedSearchQueryObject asqo, Long userId, Long resourceCollectionId, boolean addAsManagedResource) throws SearchException, IOException{
        
		TdarUser user = genericService.find(TdarUser.class, userId);
		LuceneSearchResultHandler<Resource> result = new SearchResult<>();
		result.setRecordsPerPage(maxNumOfRecords);
		
		resourceSearchService.buildAdvancedSearch(asqo, user, result, MessageHelper.getInstance());
		
		ResourceCollection collection = genericService.find(ResourceCollection.class, resourceCollectionId);
		
		if(!authorizationService.canAddToCollection(user, collection)){
			throw new TdarAuthorizationException("No permission to add to collection");
		}
		
		for(Resource resource : result.getResults()){
			CollectionResourceSection section = CollectionResourceSection.UNMANAGED;
			Set<ResourceCollection> current = resource.getUnmanagedResourceCollections();
			
			if(addAsManagedResource && authorizationService.canEdit(user, resource)){
				section = CollectionResourceSection.MANAGED;
				current = resource.getManagedResourceCollections();
			}
			
			resourceCollectionService.addResourceCollectionToResource(resource, current, user, true, ErrorHandling.NO_VALIDATION, collection, section);
		}
	}
	
}
