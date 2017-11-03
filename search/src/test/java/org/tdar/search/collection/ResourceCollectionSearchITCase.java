package org.tdar.search.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.collection.VisibleCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.search.bean.CollectionSearchQueryObject;
import org.tdar.search.exception.SearchException;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.query.SearchResult;
import org.tdar.search.service.query.CollectionSearchService;
import org.tdar.utils.MessageHelper;

public class ResourceCollectionSearchITCase extends AbstractCollectionSearchTestCase {

    @Autowired
    CollectionSearchService collectionSearchService;

    public void init() {
        boolean first = true;
        for (String name : collectionNames) {
            SharedCollection collection = new SharedCollection(name, name,  getAdminUser());
            collection.setDescription(name);
            collection.markUpdated(collection.getOwner());
            genericService.saveOrUpdate(collection);
            if (first) {
                collection.setOwner(getAdminUser());
                collection.setHidden(true);
                collection.markUpdated(collection.getOwner());
                collection.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(), getBasicUser(), GeneralPermissions.ADMINISTER_SHARE));
                genericService.saveOrUpdate(collection.getAuthorizedUsers());
                genericService.saveOrUpdate(collection);
            }
            first = false;
            logger.debug("{} {} {}", collection.getId(), collection.getTitle(), collection.isHidden());
        }
        SharedCollection find = genericService.find(SharedCollection.class, 1003L);
        find.setHidden(false);
        genericService.saveOrUpdate(find);
        reindex();
    }

    @Test
    @Rollback
    public void testCustomStemming() throws SearchException, SearchIndexException, IOException {
        init();
        CollectionSearchQueryObject csqo = new CollectionSearchQueryObject();
        csqo.getAllFields().add("Australia");
        SearchResult<ResourceCollection> result = new SearchResult<>();
        collectionSearchService.buildResourceCollectionQuery(getEditorUser(), csqo,  result, MessageHelper.getInstance());
        logger.debug("{}", result.getResults());
        assertNotEmpty("should have results", result.getResults());
        assertEquals("should have one result",  1, result.getResults().size());
    }

    @Test
    @Rollback
    public void testBasicCollectionSearch() throws SearchException, SearchIndexException, IOException {
        init();
        CollectionSearchQueryObject csqo = new CollectionSearchQueryObject();
        SearchResult<ResourceCollection> result = runQuery(null, csqo);
        assertNotEmpty("should have results", result.getResults());
        for (ResourceCollection c : result.getResults()) {
            logger.debug("{} {}", c.getId(), c);
        }
    }
    
    

    @Test
    @Rollback
    public void testAddPermission() throws SearchException, SearchIndexException, IOException {
        SharedCollection collection = createAndSaveNewResourceCollection("testRemove");
        collection.getAuthorizedUsers().clear();
        collection.getAuthorizedUsers().add(new AuthorizedUser(getBasicUser(), getBasicUser(), GeneralPermissions.REMOVE_FROM_SHARE));
        genericService.saveOrUpdate(collection);
        searchIndexService.index(collection);
        // create collection
        // grant user add permissions
        // search for collection
        // remove permission
        // search for collection
        assertCollectionMatch(collection, GeneralPermissions.ADD_TO_SHARE, true);
        assertCollectionMatch(collection, GeneralPermissions.REMOVE_FROM_SHARE, true);
        assertCollectionMatch(collection, GeneralPermissions.ADMINISTER_SHARE, false);
        collection.getAuthorizedUsers().forEach(au -> {au.setGeneralPermission(GeneralPermissions.MODIFY_METADATA);} );
        genericService.saveOrUpdate(collection);
        searchIndexService.index(collection);
        assertCollectionMatch(collection, GeneralPermissions.ADD_TO_SHARE, false);
        assertCollectionMatch(collection, GeneralPermissions.REMOVE_FROM_SHARE, false);
        assertCollectionMatch(collection, GeneralPermissions.ADMINISTER_SHARE, false);
    }

    private void assertCollectionMatch(SharedCollection collection, GeneralPermissions addToShare, boolean bool)
            throws SearchException, SearchIndexException, IOException {
        CollectionSearchQueryObject csqo = new CollectionSearchQueryObject();
        logger.debug(" {} ~~ {} {}", bool, collection, addToShare);
        csqo.getTitles().add("testRemove");
        csqo.setPermission(addToShare);
        SearchResult<ResourceCollection> result = runQuery(getBasicUser(), csqo);
        if (bool) {
        assertNotEmpty("should have results", result.getResults());
        } else {
            assertEmpty("should not have results", result.getResults());            
        }
        for (ResourceCollection c : result.getResults()) {
            logger.debug("{} {}", c.getId(), c);
            logger.debug("\t{}", c.getAuthorizedUsers());
        }
        if (bool) {
            assertTrue(result.getResults().contains(collection));
        } else {
            assertFalse(result.getResults().contains(collection));
            
        }
    }

    @Test
    @Rollback
    public void testParents() throws SearchException, SearchIndexException, IOException {
        init();
        CollectionSearchQueryObject csqo = new CollectionSearchQueryObject();
        csqo.setLimitToTopLevel(false);
        SearchResult<ResourceCollection> result = runQuery(getAdminUser(), csqo);
        assertNotEmpty("should have results", result.getResults());
        boolean seen = false;
        for (ResourceCollection c : result.getResults()) {
            logger.debug("{} {}", c.getId(), c);
            if (c.getId().equals(1002L)) {
                seen = true;
            }
        }
        assertTrue("should see 1002 ", seen);

        csqo.setLimitToTopLevel(true);
        result = runQuery(getAdminUser(), csqo);
        assertNotEmpty("should have results", result.getResults());

        seen = false;
        for (ResourceCollection c : result.getResults()) {
            logger.debug("{} {}", c.getId(), c);
            if (c.getId().equals(1002L) && c instanceof ListCollection) {
                ListCollection shared = (ListCollection)c;
                logger.debug("parent: {}", shared.getParent());
                logger.debug("parent: {}", shared.isTopLevel());
                seen = true;
            }
        }
        assertFalse("should not see 1002 ", seen);
    }

    @Test
    @Rollback
    public void testHiddenBoolean() throws SearchException, SearchIndexException, IOException {
        init();
        // test basic user
        CollectionSearchQueryObject csqo = new CollectionSearchQueryObject();
        SearchResult<ResourceCollection> result = runQuery(getBasicUser(), csqo);
        assertNotEmpty("should have results", result.getResults());
        for (ResourceCollection c : result.getResults()) {
            if (c instanceof VisibleCollection) {
                logger.debug("{} {} {}", c.getId(), c, ((VisibleCollection) c).isHidden());
                assertFalse("should not be hidden", ((VisibleCollection) c).isHidden());
            } else {
                fail("should not be indexing InternalCollections");
            }
        }
        
        // test admin user
        csqo = new CollectionSearchQueryObject();
        result = runQuery(getBasicUser(), csqo);
        assertNotEmpty("should have results", result.getResults());
        for (ResourceCollection c : result.getResults()) {
            if (c instanceof VisibleCollection) {
                logger.debug("{} {} {}", c.getId(), c, ((VisibleCollection) c).isHidden());
        //        assertFalse("should not be hidden", ((VisibleCollection) c).isHidden());
            } else {
                fail("should not be indexing InternalCollections");
            }
        }

    }

    @Test
    @Rollback
    public void testBasicCollectionSearchTerms() throws SearchException, SearchIndexException, IOException {
        init();
        CollectionSearchQueryObject csqo = new CollectionSearchQueryObject();
        csqo.getAllFields().add("Kintigh");
        csqo.getAllFields().add("KBP");
        SearchResult<ResourceCollection> result = runQuery(null, csqo);
        assertTrue("AND Search should find 0 results", CollectionUtils.isEmpty(result.getResults()));
        csqo.setOperator(Operator.OR);

        result = runQuery(null, csqo);
        for (ResourceCollection c : result.getResults()) {
            logger.debug("{} {}", c.getId(), ((VisibleCollection) c).getTitle());
            assertTrue("title contains kbp or kintigh", ((VisibleCollection) c).getTitle().contains("KBP") || ((VisibleCollection) c).getTitle().contains("Kintigh"));
        }
    }

    private SearchResult<ResourceCollection> runQuery(TdarUser user, CollectionSearchQueryObject csqo) throws SearchException, SearchIndexException, IOException {
        SearchResult<ResourceCollection> result = new SearchResult<>();
        result.setRecordsPerPage(100);
        collectionSearchService.buildResourceCollectionQuery(user, csqo, result, MessageHelper.getInstance());
        return result;
    }
}
