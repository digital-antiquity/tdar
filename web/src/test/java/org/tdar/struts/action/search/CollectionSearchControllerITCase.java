package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Document;
import org.tdar.search.exception.SearchException;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.index.LookupSource;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.utils.MessageHelper;

@Transactional
public class CollectionSearchControllerITCase extends AbstractControllerITCase {

    @Autowired
    CollectionSearchAction controller;

    @Test
    @Rollback(true)
    public void testFindAllSearchPhrase() {
        controller = generateNewInitializedController(CollectionSearchAction.class);
        doSearch("");
        assertEquals(MessageHelper.getMessage("advancedSearchController.title_all_collections"), controller.getSearchSubtitle());
    }

    @Test
    @Rollback
    public void testSearchForPublicReosurceCollection() throws InstantiationException, IllegalAccessException, SearchIndexException, IOException {
        SharedCollection collection = setupCollection(false, null);
        assertTrue(controller.getResults().contains(collection));
    }

    @Test
    @Rollback
    public void testSearchForPrivateCollectionAnonymous() throws InstantiationException, IllegalAccessException, SearchIndexException, IOException {
        SharedCollection collection = setupCollection(true, null);
        assertFalse(controller.getResults().contains(collection));
    }

    @Test
    @Rollback
    public void testSearchForPrivateSharedCollectionAnonymous() throws InstantiationException, IllegalAccessException, SearchIndexException, IOException {
        SharedCollection collection = setupCollection(true, null, false, CollectionType.LIST);
        assertFalse(controller.getResults().contains(collection));
    }

    @Test
    @Rollback
    public void testSearchForPrivateCollectionAsBasicUserWithRights()
            throws InstantiationException, IllegalAccessException, SearchIndexException, SearchException, IOException {
        SharedCollection collection = setupCollection(true, getBasicUser(), true, CollectionType.LIST);
        searchIndexService.index(collection);
        assertTrue(controller.getResults().contains(collection));
    }

    @Test
    @Rollback
    public void testSearchForPrivateSharedCollectionAsBasicUserWithRights()
            throws InstantiationException, IllegalAccessException, SearchIndexException, IOException {
        SharedCollection collection = setupCollection(true, getBasicUser(), true, CollectionType.LIST);
        searchIndexService.index(collection);
        assertTrue(controller.getResults().contains(collection));
    }

    @Test
    @Rollback
    public void testSearchForPrivateCollectionAsBasicUserWithoutRights()
            throws InstantiationException, IllegalAccessException, SearchIndexException, IOException {
        SharedCollection collection = setupCollection(true, getBasicUser());
        assertFalse(controller.getResults().contains(collection));
    }

    @Test
    @Rollback
    public void testSearchForPrivateCollectionAsAdmin() throws InstantiationException, IllegalAccessException, SearchIndexException, IOException {
        // searchIndexService.purgeAll();
        SharedCollection collection = setupCollection(true, getAdminUser());
        assertTrue(controller.getResults().contains(collection));
    }

    private SharedCollection setupCollection(boolean visible, TdarUser user) throws SearchIndexException, IOException {
        return setupCollection(visible, user, false, CollectionType.LIST);
    }

    private SharedCollection setupCollection(boolean visible, TdarUser user, boolean createAuthUser, CollectionType type)
            throws SearchIndexException, IOException {
        assertEquals(getUser(), getAdminUser());
        SharedCollection collection = createAndSaveNewResourceCollection("Hohokam Archaeology along the Salt-Gila Aqueduct Central Arizona Project");
        Document doc = createAndSaveNewResource(Document.class);

        collection.setDescription("test");
        collection.setHidden(visible);
        if (type == CollectionType.LIST) {
            collection.getUnmanagedResources().add(doc);
        } else {
            collection.getResources().add(doc);
        }
        collection.markUpdated(getUser());
        genericService.saveOrUpdate(collection);
        if (createAuthUser) {
            AuthorizedUser authuser = new AuthorizedUser(getAdminUser(), user, GeneralPermissions.ADMINISTER_SHARE);
            collection.getAuthorizedUsers().add(authuser);
            genericService.saveOrUpdate(collection);
        }
        searchIndexService.index(collection);
        controller = generateNewController(CollectionSearchAction.class);
        init(controller, user);
        doSearch("Hohokam Archaeology");
        return collection;
    }

    protected void doSearch(String query) {
        controller.setQuery(query);
        AbstractSearchControllerITCase.doSearch(controller, LookupSource.COLLECTION);
        logger.info("search (" + controller.getQuery() + ") found: " + controller.getTotalRecords());
    }

    @Override
    protected void reindex() {
        searchIndexService.purgeAll();
        searchIndexService.indexAll(getAdminUser(), LookupSource.COLLECTION);
    }

    @Override
    public TdarUser getUser() {
        return getAdminUser();
    }
}
