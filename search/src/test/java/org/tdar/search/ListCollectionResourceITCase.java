package org.tdar.search;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.search.bean.ReservedSearchParameters;
import org.tdar.search.bean.ResourceLookupObject;
import org.tdar.search.exception.SearchException;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.query.LuceneSearchResultHandler;
import org.tdar.search.query.SearchResult;
import org.tdar.search.service.query.CreatorSearchService;
import org.tdar.utils.MessageHelper;

public class ListCollectionResourceITCase  extends AbstractResourceSearchITCase {

    @Autowired
    CreatorSearchService<Creator<?>> creatorSearchService;

    @Autowired
    ResourceService resourceService;

    @Autowired
    EntityService entityService;


    @Test 
    public void testFindResourcesForCollection() throws ParseException, SearchException, SearchIndexException, IOException,SearchException, SearchIndexException {
    	String term    = null;
		Long projectId = null;
		Boolean includeParent = false;
		Long collectionId = 1000L;
		Long shareId = null;
		Long categoryId = null;
		Permissions permission = Permissions.VIEW_ALL;
		ReservedSearchParameters reservedSearchParameters = null;
		
		ResourceLookupObject resourceLookupObject = new ResourceLookupObject(term, projectId, includeParent, collectionId, shareId, categoryId, permission, reservedSearchParameters);
        LuceneSearchResultHandler<Resource> result = new SearchResult<>();
		resourceSearchService.lookupResource(getAdminUser(), resourceLookupObject, result, MessageHelper.getInstance());

		assertNotEmpty("There were results found", result.getResults());
		getLogger().debug("Results are {}",result);
    }
    
    
    
    
    
    


    
}
