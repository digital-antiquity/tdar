package org.tdar.search;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.search.bean.ReservedSearchParameters;
import org.tdar.search.bean.SearchParameters;
import org.tdar.search.exception.SearchException;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.query.SearchResult;

public class ResourceCollectionAlternateParentSearchITCase extends AbstractResourceSearchITCase {

    @Autowired
    private ResourceCollectionService resourceCollectionService;

    @Test
    @Rollback(true)
    public void testAltenrateParentInCollectionPageSearch() throws SolrServerException, IOException, ParseException, SearchException, SearchIndexException {
        Long dsId = setupDataset(Status.ACTIVE);
        // setup beans
        Dataset d = genericService.find(Dataset.class, dsId); 
        SharedCollection parent = createCollection("parent", getAdminUser());
        SharedCollection alternate = createCollection("alternate", getAdminUser());
        SharedCollection child = createCollection("child", getAdminUser());
        SharedCollection grantChild = createCollection("actual", getAdminUser());
        grantChild.getResources().add(d);
        d.getSharedCollections().add(grantChild);
        genericService.saveOrUpdate(d);
        genericService.saveOrUpdate(grantChild);
        genericService.synchronize();
        d= null;
        // set alternate parent
        resourceCollectionService.updateCollectionParentTo(getAdminUser(), grantChild, child, SharedCollection.class);
        resourceCollectionService.updateCollectionParentTo(getAdminUser(), child, parent, SharedCollection.class);
        resourceCollectionService.updateAlternateCollectionParentTo(getAdminUser(), child, alternate, SharedCollection.class);
        genericService.saveOrUpdate(grantChild);
        genericService.saveOrUpdate(alternate);
        genericService.saveOrUpdate(child);
        genericService.synchronize();
        
        searchIndexService.index(genericService.find(Dataset.class, dsId));
        ReservedSearchParameters rparams = new ReservedSearchParameters();
        SearchParameters sp = new SearchParameters();
        sp.getShares().add(parent);
        SearchResult<Resource> result = doSearch("", null, sp, rparams);
        assertTrue("expected to find resource", resultsContainId(result, dsId));

        rparams = new ReservedSearchParameters();
        sp = new SearchParameters();
        sp.getShares().add(alternate);
        result = doSearch("", null, sp, rparams);
        assertTrue("expected to find resource (alternate parent)", resultsContainId(result, dsId));
}

    private SharedCollection createCollection(String name, TdarUser tdarUser) {
        SharedCollection c = new SharedCollection();
        c.setName(name);
        c.setDescription(name);
        c.markUpdated(tdarUser);
        genericService.saveOrUpdate(c);
        return c;
    }

    
    
}
