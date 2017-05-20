package org.tdar.search;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.search.bean.AdvancedSearchQueryObject;
import org.tdar.search.bean.SearchParameters;
import org.tdar.search.exception.SearchException;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SearchResult;
import org.tdar.search.query.facet.FacetWrapper;
import org.tdar.utils.MessageHelper;

public class ResourceTypeBoostITCase extends AbstractResourceSearchITCase {

    @Test
    @Rollback
    public void testFacetPivotStats() throws SolrServerException, IOException, ParseException , SearchException, SearchIndexException {
        SearchResult<Resource> result = new SearchResult<>();
        FacetWrapper facetWrapper = new FacetWrapper();
        facetWrapper.setMapFacet(true);
        facetWrapper.facetBy(QueryFieldNames.RESOURCE_TYPE, ResourceType.class);
        result.setFacetWrapper(facetWrapper);
        result.setRecordsPerPage(1000);
        result.setSortField(SortOption.RELEVANCE);
        AdvancedSearchQueryObject asqo = new AdvancedSearchQueryObject();
        SearchParameters sp = new SearchParameters();
        asqo.getSearchParameters().add(sp);
        resourceSearchService.buildAdvancedSearch(asqo, null, result, MessageHelper.getInstance());
        logger.debug("{}", result.getFacetWrapper().getFacetResults());
        boolean seen = false;
        for (Resource r : result.getResults()) {
            logger.debug("{}", r.getResourceType());
            if (r.getResourceType().isSupporting()) {
                seen = true;
            } else {
                if (seen == true) {
                    fail("shouldn't be switching between supporting and non-supporting");
                }
                seen = false;
            }
        }
        assertTrue(seen);
    }

}
