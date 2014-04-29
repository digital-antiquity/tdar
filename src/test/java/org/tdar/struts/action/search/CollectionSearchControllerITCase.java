package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.search.index.LookupSource;
import org.tdar.utils.MessageHelper;

@Transactional
public class CollectionSearchControllerITCase extends AbstractSearchControllerITCase {

    @Test
    @Rollback(true)
    public void testFindAllSearchPhrase() {
        doSearch("");
        assertEquals(MessageHelper.getMessage("advancedSearchController.title_all_collections"), controller.getSearchSubtitle());
    }

    @Test
    @Rollback
    public void testSearchForPublicReosurceCollection() throws InstantiationException, IllegalAccessException {
        ResourceCollection collection = setupCollection(true, null);
        assertTrue(controller.getResults().contains(collection));
    }

    @Test
    @Rollback
    public void testSearchForPrivateCollectionAnonymous() throws InstantiationException, IllegalAccessException {
        ResourceCollection collection = setupCollection(false, null);
        assertFalse(controller.getResults().contains(collection));
    }

    @Test
    @Rollback
    public void testSearchForPrivateCollectionAsBasicUserWithRights() throws InstantiationException, IllegalAccessException {
        ResourceCollection collection = setupCollection(false, getBasicUser(), true);
        assertTrue(controller.getResults().contains(collection));
    }

    @Test
    @Rollback
    public void testSearchForPrivateCollectionAsBasicUserWithoutRights() throws InstantiationException, IllegalAccessException {
        ResourceCollection collection = setupCollection(false, getBasicUser());
        assertFalse(controller.getResults().contains(collection));
    }

    @Test
    @Rollback
    public void testSearchForPrivateCollectionAsAdmin() throws InstantiationException, IllegalAccessException {
        ResourceCollection collection = setupCollection(false, getAdminUser());
        assertTrue(controller.getResults().contains(collection));
    }

    private ResourceCollection setupCollection(boolean visible, TdarUser user) {
        return setupCollection(visible, user, false);
    }

    private ResourceCollection setupCollection(boolean visible, TdarUser user, boolean createAuthUser) {
        assertEquals(getUser(), getAdminUser());
        ResourceCollection collection = createAndSaveNewResourceCollection("Hohokam Archaeology along the Salt-Gila Aqueduct Central Arizona Project");
        collection.setDescription("test");
        collection.setVisible(visible);
        genericService.saveOrUpdate(collection);
        if (createAuthUser) {
            AuthorizedUser authuser = new AuthorizedUser(user, GeneralPermissions.ADMINISTER_GROUP);
            collection.getAuthorizedUsers().add(authuser);
            genericService.saveOrUpdate(collection);
        }
        searchIndexService.index(collection);
        controller = generateNewController(AdvancedSearchController.class);
        init(controller, user);
        doSearch("Hohokam Archaeology");
        return collection;
    }

    @Override
    protected void doSearch(String query) {
        controller.setQuery(query);
        AbstractSearchControllerITCase.doSearch(controller, LookupSource.COLLECTION);
        logger.info("search (" + controller.getQuery() + ") found: " + controller.getTotalRecords());
    }

    @Override
    protected void reindex() {
        searchIndexService.purgeAll();
        searchIndexService.indexAll(getAdminUser(), ResourceCollection.class);
    }

    @Override
    protected TdarUser getUser() {
        return getAdminUser();
    }
}
