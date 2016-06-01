package org.tdar.search.collection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.AbstractWithIndexIntegrationTestCase;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.search.QuietIndexReciever;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.SearchResult;
import org.tdar.search.service.query.CollectionSearchService;
import org.tdar.utils.MessageHelper;

public class CollectionLookupITCase extends AbstractWithIndexIntegrationTestCase {

    @Override
    public void reindex() {
        searchIndexService.purgeAll(LookupSource.COLLECTION);
        searchIndexService.indexAll(new QuietIndexReciever(), Arrays.asList( LookupSource.COLLECTION), getAdminUser());
    };

    @Autowired
    CollectionSearchService collectionSearchService;

    String[] collectionNames = new String[] { "Kalaupapa National Historical Park, Hawaii", "Kaloko-Honokohau National Historical Park, Hawaii", "Kapsul",
            "KBP Artifact Photographs", "KBP Field Notes", "KBP Level Maps", "KBP Maps", "KBP Profiles", "KBP Reports", "KBP Site Photographs",
            "Kharimkotan 1", "Kienuka", "Kintigh - Carp Fauna Coding Sheets", "Kintigh - Cibola Excavation", "Kintigh - Cibola Research",
            "Kintigh - Cibola Survey Projects", "Kintigh - Context Ontologies", "Kintigh - Fauna Ontologies", "Kintigh - HARP Coding Sheets",
            "Kintigh - Quantitative and Formal Methods Class - Assignments & Data", "Kleis", "Klinko", "Kokina 1", "Kompaneyskyy 1",
            "Kuril Biocomplexity Research", "Kuybyshevskaya 1",
            "Spielmann/Kintigh - Fauna Ontologies - Current" };

    @Test
    @Rollback(true)
    public void testCollectionLookup() throws IOException, SolrServerException, ParseException {
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
            throws ParseException, SolrServerException, IOException {
        SearchResult<ResourceCollection> results = new SearchResult<>(100);
        collectionSearchService.findCollection(user, permission, title, results, MessageHelper.getInstance());
        return results;
    }

    @Test
    @Rollback(true)
    public void testCollectionLookupUnauthenticated() throws SolrServerException, IOException, ParseException {
        setupCollections();
        SearchResult<ResourceCollection> result = search(null, null, "Kintigh - C");
        for (ResourceCollection collection : result.getResults()) {
            logger.info("{}", collection);
            if (collection != null) {
                assertTrue(collection.getTitle().contains("Kintigh - C"));
            }
        }

    }

    private void setupCollections() throws SolrServerException, IOException {
        List<ResourceCollection> collections = new ArrayList<ResourceCollection>();
        for (String collectionName : collectionNames) {
            ResourceCollection e = new ResourceCollection(collectionName, collectionName, SortOption.TITLE, CollectionType.SHARED, true, getBasicUser());
            collections.add(e);
            e.markUpdated(getBasicUser());

        }
        genericService.save(collections);
        searchIndexService.index(collections.toArray(new ResourceCollection[0]));
    }

    @Test
    @Rollback(true)
    public void testInvisibleCollectionLookupFoundByBasicOwner() throws SolrServerException, IOException, ParseException {
        ResourceCollection e = setupResourceCollectionForPermissionsTests(getAdminUser(), false, getBasicUser(), GeneralPermissions.VIEW_ALL);
        SearchResult<ResourceCollection> result = search(null, null, "test");
        assertTrue(result.getResults().contains(e));
    }

    @Test
    @Rollback(true)
    public void testInvisibleCollectionLookupFoundByBasicUser() throws SolrServerException, IOException, ParseException {
        ResourceCollection e = setupResourceCollectionForPermissionsTests(getAdminUser(), false, getBasicUser(), GeneralPermissions.VIEW_ALL);
        SearchResult<ResourceCollection> result = search(getBasicUser(), null, "test");
        assertTrue(result.getResults().contains(e));
    }

    @Test
    @Rollback(true)
    public void testInvisibleCollectionLookupFoundByBasicUserForModification() throws SolrServerException, IOException, ParseException {
        ResourceCollection e = setupResourceCollectionForPermissionsTests(getAdminUser(), false, getBasicUser(), GeneralPermissions.VIEW_ALL);
        SearchResult<ResourceCollection> result = search(getBasicUser(), GeneralPermissions.ADMINISTER_GROUP, "test");
        assertFalse(result.getResults().contains(e));
    }

    private ResourceCollection setupResourceCollectionForPermissionsTests(TdarUser owner, boolean visible, TdarUser user, GeneralPermissions permission)
            throws SolrServerException, IOException {
        assertFalse(getSessionUser().equals(getAdminUser()));
        ResourceCollection e = new ResourceCollection("a test", "a Name", SortOption.TITLE, CollectionType.SHARED, visible, owner);
        e.markUpdated(owner);
        genericService.save(e);
        if (user != null) {
            AuthorizedUser au = new AuthorizedUser(user, permission);
            e.getAuthorizedUsers().add(au);
        }
        searchIndexService.index(e);
        return e;
    }

}
