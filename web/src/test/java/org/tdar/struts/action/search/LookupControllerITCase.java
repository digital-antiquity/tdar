package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.search.QuietIndexReciever;
import org.tdar.search.index.LookupSource;
import org.tdar.struts.action.AbstractIntegrationControllerTestCase;
import org.tdar.struts.action.api.lookup.ResourceLookupAction;

public class LookupControllerITCase extends AbstractIntegrationControllerTestCase {

    
    @Test
    @Rollback
    public void testVerifyManagedAndUnmanagedCollections() throws SolrServerException, IOException {
        searchIndexService.indexAll(new QuietIndexReciever(),Arrays.asList( LookupSource.RESOURCE), getAdminUser());
    
        ResourceLookupAction controller = generateNewInitializedController(ResourceLookupAction.class);
        controller.setRecordsPerPage(99);
        List<Long> collectionIds = new ArrayList<Long>();
        collectionIds.add(1000L);
        controller.setCollectionId(collectionIds);
        controller.setSelectResourcesFromCollectionid(1000L);
        controller.lookupResource();

        Set<Long> unmanaged = (Set<Long>) controller.getResult().get("unmanagedResourceResults");
        Set<Long> managed = (Set<Long>) controller.getResult().get("managedResourceResults");

        getLogger().debug("Umanaged resource is: {}", controller.getResult());

        assertEquals("The unmanaged has 2 elements", 2, unmanaged.size());

    }

}
