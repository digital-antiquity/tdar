package org.tdar.search.query.part;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.search.AbstractResourceSearchITCase;
import org.tdar.search.bean.ReservedSearchParameters;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.SearchResult;
import org.tdar.utils.PersistableUtils;

public class ResourcePermissionsSearchITCase extends AbstractResourceSearchITCase {

    @Test
    @Rollback
    public void testDraftResourceWithRights() throws ParseException, SolrServerException, IOException {
        Long imgId = setupImage();
        searchIndexService.indexAll(getAdminUser(), LookupSource.RESOURCE);
        
        ReservedSearchParameters rsp = new ReservedSearchParameters();
        rsp.setStatuses(Arrays.asList(Status.DRAFT));

        SearchResult<Resource> result = doSearch("description", getBasicUser(), null, rsp);
        List<Long> ids = PersistableUtils.extractIds(result.getResults());
        assertTrue(ids.contains(imgId));
    }
    
    @Test
    @Rollback
    public void testDraftResourceWithoutRights() throws ParseException, SolrServerException, IOException {
        Long imgId = setupImage(getAdminUser());
        searchIndexService.indexAll(getAdminUser(), LookupSource.RESOURCE);
        
        ReservedSearchParameters rsp = new ReservedSearchParameters();
        rsp.setStatuses(Arrays.asList(Status.DRAFT));

        SearchResult<Resource> result = doSearch("description", getBasicUser(), null, rsp);
        List<Long> ids = PersistableUtils.extractIds(result.getResults());
        assertFalse(ids.contains(imgId));
    }


    @Test
    @Rollback
    public void testDraftResourceWithResourceCollectionRights() throws ParseException, SolrServerException, IOException {
        Long imgId = setupImage(getAdminUser());
        searchIndexService.indexAll(getAdminUser(), LookupSource.RESOURCE);
        ResourceCollection rc = new ResourceCollection(CollectionType.INTERNAL);
        rc.markUpdated(getAdminUser());
        rc.setName("test");
        rc.getAuthorizedUsers().add(new AuthorizedUser(getBasicUser(),GeneralPermissions.VIEW_ALL));
        genericService.saveOrUpdate(rc);
        Image img = genericService.find(Image.class, imgId);
        rc.getResources().add(img);
        img.getResourceCollections().add(rc);
        genericService.saveOrUpdate(rc, img);
        ReservedSearchParameters rsp = new ReservedSearchParameters();
        rsp.setStatuses(Arrays.asList(Status.DRAFT));

        SearchResult<Resource> result = doSearch("description", getBasicUser(), null, rsp);
        List<Long> ids = PersistableUtils.extractIds(result.getResults());
        assertFalse(ids.contains(imgId));
    }
    
    @Test
    @Rollback
    public void testDraftResourceWithoutResourceCollectionRights() throws ParseException, SolrServerException, IOException {
        Long imgId = setupImage(getAdminUser());
        searchIndexService.indexAll(getAdminUser(), LookupSource.RESOURCE);
        ResourceCollection rc = new ResourceCollection(CollectionType.INTERNAL);
        rc.markUpdated(getAdminUser());
        rc.setName("test");
        rc.getAuthorizedUsers().add(new AuthorizedUser(getEditorUser(),GeneralPermissions.VIEW_ALL));
        genericService.saveOrUpdate(rc);
        Image img = genericService.find(Image.class, imgId);
        rc.getResources().add(img);
        img.getResourceCollections().add(rc);
        genericService.saveOrUpdate(rc, img);
        ReservedSearchParameters rsp = new ReservedSearchParameters();
        rsp.setStatuses(Arrays.asList(Status.DRAFT));

        SearchResult<Resource> result = doSearch("description", getBasicUser(), null, rsp);
        List<Long> ids = PersistableUtils.extractIds(result.getResults());
        assertFalse(ids.contains(imgId));
    }

}
