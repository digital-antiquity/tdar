package org.tdar.search;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.search.query.SearchResult;

public class MultiCoreITCase extends AbstractResourceSearchITCase {

    @Test
    @Ignore
    @Rollback
    public void testMulticoreSearch() throws SolrServerException, IOException, ParseException {
        SearchResult<Indexable> result = new SearchResult<>(10000);
        ResourceCollection createAndSaveNewResourceCollection = createAndSaveNewResourceCollection("test test");
        searchIndexService.index(createAndSaveNewResourceCollection);
        resourceSearchService.mulitCoreSearch("test",result);
        for (Indexable r : result.getResults()) {
        	logger.debug("{} {}", r.getId(), r.getClass().getSimpleName());
        	if (r instanceof InformationResource) {
        		InformationResource ir = (InformationResource)r;
        		logger.debug("\t{}", ir.getProject());
        	}
//        	logger.debug("\t{}",r.getActiveLatitudeLongitudeBoxes());
//        	logger.debug("\t{}",r.getPrimaryCreators());
        }
    }
    
}
