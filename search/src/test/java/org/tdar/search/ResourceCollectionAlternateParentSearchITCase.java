package org.tdar.search;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.search.bean.ReservedSearchParameters;
import org.tdar.search.bean.SearchParameters;
import org.tdar.search.query.SearchResult;

public class ResourceCollectionAlternateParentSearchITCase extends AbstractResourceSearchITCase {



    @Test
    @Rollback(true)
    public void testAltenrateParentInCollectionPageSearch() throws SolrServerException, IOException, ParseException {
        Long dsId = setupDataset(Status.ACTIVE);
        // setup beans
        Dataset d = genericService.find(Dataset.class, dsId); 
        ResourceCollection parent = createCollection("parent", getAdminUser());
        ResourceCollection alternate = createCollection("alternate", getAdminUser());
        ResourceCollection child = createCollection("child", getAdminUser());
        ResourceCollection grantChild = createCollection("actual", getAdminUser());
        grantChild.getResources().add(d);
        d.getResourceCollections().add(grantChild);
        genericService.saveOrUpdate(d);
        genericService.synchronize();
        d= null;
        // set alternate parent
        genericService.saveOrUpdate(grantChild);
        grantChild.setParent(child);
        child.setParent(parent);
        child.setAlternateParent(alternate);
        child.getParentIds().add(parent.getId());
        grantChild.getParentIds().add(parent.getId());
        grantChild.getParentIds().add(child.getId());
        genericService.saveOrUpdate(grantChild);
        genericService.saveOrUpdate(child);
        genericService.synchronize();
        
        searchIndexService.index(genericService.find(Dataset.class, dsId));
        ReservedSearchParameters rparams = new ReservedSearchParameters();
        SearchParameters sp = new SearchParameters();
        sp.getCollections().add(parent);
        SearchResult<Resource> result = doSearch("", null, sp, rparams);
        assertTrue("expected to find resource", resultsContainId(result, dsId));

        rparams = new ReservedSearchParameters();
        sp = new SearchParameters();
        sp.getCollections().add(alternate);
        result = doSearch("", null, sp, rparams);
        assertTrue("expected to find resource (alternate parent)", resultsContainId(result, dsId));
}

    private ResourceCollection createCollection(String name, TdarUser tdarUser) {
        ResourceCollection c = new ResourceCollection(CollectionType.SHARED);
        c.setName(name);
        c.setDescription(name);
        c.markUpdated(tdarUser);
        genericService.saveOrUpdate(c);
        return c;
    }

    
    
}
