package org.tdar.search.collection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.search.bean.CollectionSearchQueryObject;
import org.tdar.search.exception.SearchException;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.query.SearchResult;
import org.tdar.utils.MessageHelper;

public class CollectionLookupITCase extends AbstractCollectionSearchTestCase {


    @Test
    @Rollback(true)
    public void testCollectionLookup() throws IOException, SearchException, SearchIndexException {
        setupCollections();
        SearchResult<ResourceCollection> results = search(null, null, "Kin");
        for (ResourceCollection collection : results.getResults()) {
            logger.info("{}", collection);
            if (collection != null) {
                assertFalse(collection.getTitle().equals("Kleis"));
            }
        }
        results = search(null, null, "Kintigh - C");
        for (ResourceCollection collection : results.getResults()) {
            logger.info("{}", collection);
            if (collection != null) {
                assertTrue(collection.getTitle().contains("Kintigh - C"));
            }
        }

    }

    private SearchResult<ResourceCollection> search(TdarUser user, GeneralPermissions permission, String title)
            throws SearchException, IOException {
        SearchResult<ResourceCollection> results = new SearchResult<>(100);
        CollectionSearchQueryObject csqo = new CollectionSearchQueryObject();
        csqo.setPermission(permission);
        csqo.getTitles().add(title);
        collectionSearchService.lookupCollection(user, csqo, results, MessageHelper.getInstance());
        return results;
    }

    @Test
    @Rollback(true)
    public void testCollectionLookupUnauthenticated() throws SearchException, SearchIndexException, IOException {
        setupCollections();
        SearchResult<ResourceCollection> result = search(null, null, "Kintigh - C");
        for (ResourceCollection collection : result.getResults()) {
            logger.info("{}", collection);
            if (collection != null) {
                assertTrue(collection.getTitle().contains("Kintigh - C"));
            }
        }

    }

    private void setupCollections() throws SearchException, SearchIndexException, IOException {
        List<ResourceCollection> collections = new ArrayList<ResourceCollection>();
        for (String collectionName : collectionNames) {
            ResourceCollection e = new ResourceCollection(collectionName, collectionName, getBasicUser());
            collections.add(e);
            e.markUpdated(getBasicUser());

        }
        genericService.save(collections);
        searchIndexService.index(collections.toArray(new ResourceCollection[0]));
    }

    @Test
    @Rollback(true)
    public void testInvisibleCollectionLookupFoundByBasicOwner() throws SearchException, IOException, SearchIndexException {
        ResourceCollection e = setupResourceCollectionForPermissionsTests(getAdminUser(), false, getBasicUser(),  null);
        SearchResult<ResourceCollection> result = search(null, null, "test");
        assertTrue(result.getResults().contains(e));
    }

    @Test
    @Rollback(true)
    public void testInvisibleCollectionLookupFoundByBasicUser() throws SearchException, SearchIndexException, IOException {
        ResourceCollection e = setupResourceCollectionForPermissionsTests(getAdminUser(), false, getBasicUser(),   GeneralPermissions.ADD_TO_COLLECTION);
        SearchResult<ResourceCollection> result = search(getBasicUser(), null, "test");
        assertTrue(result.getResults().contains(e));
    }

    @Test
    @Rollback(true)
    public void testInvisibleCollectionLookupFoundByBasicUserForModification() throws SearchException, SearchIndexException, IOException {
        ResourceCollection e = setupResourceCollectionForPermissionsTests(getAdminUser(), false, getBasicUser(),  GeneralPermissions.REMOVE_FROM_COLLECTION);
        SearchResult<ResourceCollection> result = search(getBasicUser(), GeneralPermissions.ADMINISTER_SHARE, "test");
        assertFalse(result.getResults().contains(e));
    }

    
    @Test
    @Rollback(true)
    public void testInvisibleShareLookupFoundByBasicOwner() throws SearchException, SearchIndexException, IOException {
        ResourceCollection e = setupResourceCollectionForPermissionsTests(getAdminUser(), false, getBasicUser(),  GeneralPermissions.VIEW_ALL);
        SearchResult<ResourceCollection> result = search(null, null, "test");
        assertTrue(result.getResults().contains(e));
    }

    @Test
    @Rollback(true)
    public void testInvisibleShareLookupFoundByBasicUser() throws SearchException, SearchIndexException, IOException {
        ResourceCollection e = setupResourceCollectionForPermissionsTests(getAdminUser(), false, getBasicUser(),   GeneralPermissions.VIEW_ALL);
        SearchResult<ResourceCollection> result = search(getBasicUser(), null, "test");
        assertTrue(result.getResults().contains(e));
    }

    @Test
    @Rollback(true)
    public void testInvisibleShareLookupFoundByBasicUserForModification() throws SearchException, SearchIndexException, IOException {
        ResourceCollection e = setupResourceCollectionForPermissionsTests(getAdminUser(), false, getBasicUser(),  GeneralPermissions.VIEW_ALL);
        SearchResult<ResourceCollection> result = search(getBasicUser(), GeneralPermissions.ADMINISTER_SHARE, "test");
        assertFalse(result.getResults().contains(e));
    }

    private ResourceCollection setupResourceCollectionForPermissionsTests(TdarUser owner, boolean visible, TdarUser user, GeneralPermissions permission)
            throws SearchIndexException, IOException {
        assertFalse(getSessionUser().equals(getAdminUser()));
        ResourceCollection e = new ResourceCollection("a test", "a Name",  owner);
        e.markUpdated(owner);
        genericService.save(e);
        if (user != null && permission != null) {
            AuthorizedUser au = new AuthorizedUser(getAdminUser(), user, permission);
            e.getAuthorizedUsers().add(au);
        }
        searchIndexService.index(e);
        return e;
    }

}
