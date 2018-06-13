package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.collection.CollectionResourceSection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Status;
import org.tdar.search.exception.SearchException;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.index.LookupSource;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.utils.MessageHelper;

import com.amazonaws.services.ec2.model.EgressOnlyInternetGateway;

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
        ResourceCollection collection = setupCollection(false, null);
        assertTrue(controller.getResults().contains(collection));
    }

    @Test
    @Rollback
    public void testSearchForPrivateCollectionAnonymous() throws InstantiationException, IllegalAccessException, SearchIndexException, IOException {
        ResourceCollection collection = setupCollection(true, null);
        assertFalse(controller.getResults().contains(collection));
    }

    @Test
    @Rollback
    public void testSearchForPrivateSharedCollectionAnonymous() throws InstantiationException, IllegalAccessException, SearchIndexException, IOException {
        ResourceCollection collection = setupCollection(true, null, false, CollectionResourceSection.UNMANAGED);
        assertFalse(controller.getResults().contains(collection));
    }

    @Test
    @Rollback
    public void testSearchForPrivateCollectionAsBasicUserWithRights()
            throws InstantiationException, IllegalAccessException, SearchIndexException, SearchException, IOException {
        ResourceCollection collection = setupCollection(true, getBasicUser(), true, CollectionResourceSection.UNMANAGED);
        searchIndexService.index(collection);
        assertTrue(controller.getResults().contains(collection));
    }

    @Test
    @Rollback
    public void testSearchForPrivateSharedCollectionAsBasicUserWithRights()
            throws InstantiationException, IllegalAccessException, SearchIndexException, IOException {
        ResourceCollection collection = setupCollection(true, getBasicUser(), true, CollectionResourceSection.UNMANAGED);
        searchIndexService.index(collection);
        assertTrue(controller.getResults().contains(collection));
    }

    
    @Test
    @Rollback
    public void testSearchForDeletedCollectionAsBasicUser()
            throws InstantiationException, IllegalAccessException, SearchIndexException, IOException {
        TdarUser user = getBasicUser();
        ResourceCollection collection = createAndSaveNewResourceCollection("Hohokam Archaeology along the Salt-Gila Aqueduct Central Arizona Project");
        collection.setDescription("test");
        collection.setHidden(false);
        collection.setStatus(Status.DELETED);
        collection.markUpdated(getUser());
        genericService.saveOrUpdate(collection);
        collection.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(), user, Permissions.ADMINISTER_COLLECTION));
        collection.getAuthorizedUsers().add(new AuthorizedUser(getBasicUser(), user, Permissions.ADMINISTER_COLLECTION));
        genericService.saveOrUpdate(collection);
        searchIndexService.index(collection);
        controller = generateNewController(CollectionSearchAction.class);
        init(controller, user);
        doSearch("Hohokam Archaeology");
        assertFalse(controller.getResults().contains(collection));
        controller = generateNewController(CollectionSearchAction.class);
        init(controller, getAdminUser());
        doSearch("Hohokam Archaeology");
        assertFalse(controller.getResults().contains(collection));
    }

    @Test
    @Rollback
    public void testSearchForPrivateCollectionAsBasicUserWithoutRights()
            throws InstantiationException, IllegalAccessException, SearchIndexException, IOException {
        ResourceCollection collection = setupCollection(true, getBasicUser());
        assertFalse(controller.getResults().contains(collection));
    }

    @Test
    @Rollback
    public void testSearchForPrivateCollectionAsAdmin() throws InstantiationException, IllegalAccessException, SearchIndexException, IOException {
        // searchIndexService.purgeAll();
        ResourceCollection collection = setupCollection(true, getAdminUser());
        assertTrue(controller.getResults().contains(collection));
    }

    private ResourceCollection setupCollection(boolean visible, TdarUser user) throws SearchIndexException, IOException {
        return setupCollection(visible, user, false, CollectionResourceSection.UNMANAGED);
    }

    private ResourceCollection setupCollection(boolean visible, TdarUser user, boolean createAuthUser, CollectionResourceSection type)
            throws SearchIndexException, IOException {
        assertEquals(getUser(), getAdminUser());
        ResourceCollection collection = createAndSaveNewResourceCollection("Hohokam Archaeology along the Salt-Gila Aqueduct Central Arizona Project");
        Document doc = createAndSaveNewResource(Document.class);

        collection.setDescription("test");
        collection.setHidden(visible);
        if (type == CollectionResourceSection.UNMANAGED) {
            collection.getUnmanagedResources().add(doc);
        } else {
            collection.getManagedResources().add(doc);
        }
        collection.markUpdated(getUser());
        genericService.saveOrUpdate(collection);
        if (createAuthUser) {
            AuthorizedUser authuser = new AuthorizedUser(getAdminUser(), user, Permissions.ADMINISTER_COLLECTION);
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
