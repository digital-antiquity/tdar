package org.tdar.core.bean;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.UserRightsProxy;
import org.tdar.core.exception.TdarAuthorizationException;
import org.tdar.core.service.collection.ResourceCollectionService;

public class RightsITCase extends AbstractIntegrationTestCase {

    @Autowired
    private ResourceCollectionService resourceCollectionService;

    @Test
    @Rollback
    public void testRightsExtend() {
        // setup user that expires in two days, that has access to dataset
        Dataset dataset = createAndSaveNewDataset();
        TdarUser badUser = createAndSaveNewUser();
        AuthorizedUser authorizedUser = new AuthorizedUser(getAdminUser(), badUser, Permissions.MODIFY_RECORD);
        Date soon = DateTime.now().plusDays(2).toDate();
        authorizedUser.setDateExpires(soon);
        dataset.getAuthorizedUsers().add(authorizedUser);
        genericService.saveOrUpdate(dataset);
        genericService.saveOrUpdate(authorizedUser);
        authorizedUser = null;
        Long id = dataset.getId();
        genericService.synchronize();

        List<UserRightsProxy> proxies = Arrays.asList(new UserRightsProxy(new AuthorizedUser(badUser, badUser, Permissions.MODIFY_RECORD)));

        saveAndAssertException(dataset, badUser, proxies);
    }

    private void saveAndAssertException(Dataset dataset, TdarUser badUser, List<UserRightsProxy> proxies) {
        TdarAuthorizationException tau = null;
        try {
            resourceCollectionService.saveResourceRights(proxies, badUser, dataset);
        } catch (TdarAuthorizationException tau_) {
            tau = tau_;
        }
        assertNotNull("we should have an exception", tau);
    }

    @Test
    @Rollback
    public void testRightsModifyMetadata() {
        // setup user that expires in two days, that has access to dataset
        Dataset dataset = createAndSaveNewDataset();
        TdarUser badUser = createAndSaveNewUser();
        AuthorizedUser authorizedUser = new AuthorizedUser(getAdminUser(), badUser, Permissions.MODIFY_METADATA);
        Date soon = DateTime.now().plusDays(2).toDate();
        authorizedUser.setDateExpires(soon);
        dataset.getAuthorizedUsers().add(authorizedUser);
        genericService.saveOrUpdate(dataset);
        genericService.saveOrUpdate(authorizedUser);
        authorizedUser = null;
        Long id = dataset.getId();
        genericService.synchronize();

        AuthorizedUser au = new AuthorizedUser(badUser, badUser, Permissions.MODIFY_METADATA);
        au.setDateExpires(soon);
        List<UserRightsProxy> proxies = Arrays.asList(new UserRightsProxy(au));
        saveAndAssertException(dataset, badUser, proxies);
    }

    @Test
    @Rollback
    public void testRightsUpgrade() {
        // setup user that expires in two days, that has access to dataset
        Dataset dataset = createAndSaveNewDataset();
        TdarUser badUser = createAndSaveNewUser();
        AuthorizedUser authorizedUser = new AuthorizedUser(getAdminUser(), badUser, Permissions.MODIFY_RECORD);
        Date soon = DateTime.now().plusDays(2).toDate();
        authorizedUser.setDateExpires(soon);
        dataset.getAuthorizedUsers().add(authorizedUser);
        genericService.saveOrUpdate(dataset);
        genericService.saveOrUpdate(authorizedUser);
        authorizedUser = null;
        Long id = dataset.getId();
        genericService.synchronize();

        AuthorizedUser au = new AuthorizedUser(badUser, badUser, Permissions.ADMINISTER_COLLECTION);
        au.setDateExpires(soon);
        List<UserRightsProxy> proxies = Arrays.asList(new UserRightsProxy(au));
        saveAndAssertException(dataset, badUser, proxies);
    }

    @Test
    @Rollback
    public void testRightsExtendNewUser() {
        // setup user that expires in two days, that has access to dataset
        Dataset dataset = createAndSaveNewDataset();
        TdarUser badUser = createAndSaveNewUser();
        AuthorizedUser authorizedUser = new AuthorizedUser(getAdminUser(), badUser, Permissions.MODIFY_RECORD);
        Date soon = DateTime.now().plusDays(2).toDate();
        authorizedUser.setDateExpires(soon);
        dataset.getAuthorizedUsers().add(authorizedUser);
        genericService.saveOrUpdate(dataset);
        genericService.saveOrUpdate(authorizedUser);
        // authorizedUser = null;
        genericService.synchronize();

        AuthorizedUser au = new AuthorizedUser(badUser, getEditorUser(), Permissions.MODIFY_RECORD);
        // au.setDateExpires(soon);
        List<UserRightsProxy> proxies = Arrays.asList(new UserRightsProxy(au), new UserRightsProxy(authorizedUser));
        saveAndAssertException(dataset, badUser, proxies);
    }

    @Test
    @Rollback(true)
    public void testRightsRemoveInherit() {
        // setup user that expires in two days, that has access to dataset
        Dataset dataset = createAndSaveNewDataset();
        TdarUser removeUser = createAndSaveNewUser();
        TdarUser ownerUser = createAndSaveNewUser();
        AuthorizedUser authorizedUser = new AuthorizedUser(getAdminUser(), removeUser, Permissions.VIEW_ALL);
        dataset.getAuthorizedUsers().add(authorizedUser);
        genericService.saveOrUpdate(dataset);
        genericService.saveOrUpdate(authorizedUser);

        // setup collection and parent, grant ownership rights to parent
        ResourceCollection grandParent = new ResourceCollection("rights parent", "rights parent", getBasicUser());
        grandParent.markUpdated(getBasicUser());
        grandParent.getAuthorizedUsers().add(new AuthorizedUser(getBasicUser(), ownerUser, Permissions.ADMINISTER_COLLECTION));
        ResourceCollection parent = new ResourceCollection("rights inherit", "rights inherit", getBasicUser());
        parent.markUpdated(getBasicUser());
        parent.getAuthorizedUsers().add(new AuthorizedUser(getBasicUser(), getEditorUser(), Permissions.ADMINISTER_COLLECTION));
        ResourceCollection collection = new ResourceCollection("rights inherit", "rights inherit", getBasicUser());
        collection.markUpdated(getBasicUser());
        collection.getAuthorizedUsers().add(new AuthorizedUser(getBasicUser(), getAdminUser(), Permissions.ADMINISTER_COLLECTION));

        // setup a completely different inheritance tree
        ResourceCollection collection2 = new ResourceCollection("rights2", "rights2", getBasicUser());
        collection2.markUpdated(getBasicUser());
        collection2.getAuthorizedUsers().add(new AuthorizedUser(getBasicUser(), getAdminUser(), Permissions.ADMINISTER_COLLECTION));
        genericService.saveOrUpdate(grandParent);
        genericService.saveOrUpdate(collection);
        genericService.saveOrUpdate(collection2);
        genericService.saveOrUpdate(parent);
        resourceCollectionService.updateCollectionParentTo(getAdminUser(), parent, grandParent);
        resourceCollectionService.updateCollectionParentTo(getAdminUser(), collection, parent);
        genericService.saveOrUpdate(grandParent);
        genericService.saveOrUpdate(parent);
        genericService.saveOrUpdate(collection);
        dataset.getManagedResourceCollections().add(collection);
        genericService.saveOrUpdate(collection);
        collection.getManagedResources().add(dataset);
        genericService.saveOrUpdate(dataset);
        dataset.getManagedResourceCollections().add(collection2);
        genericService.saveOrUpdate(collection2);
        collection2.getManagedResources().add(dataset);
        genericService.saveOrUpdate(dataset);
        // authorizedUser = null;
        Long datasetId = dataset.getId();
        Long collectionId = collection.getId();
        Long parentId = grandParent.getId();
        Long userId = removeUser.getId();
        Long ownerId = ownerUser.getId();
        removeUser = null;
        dataset = null;
        collection = null;
        grandParent = null;
        genericService.synchronize();

        List<UserRightsProxy> proxies = new ArrayList<>();
        resourceCollectionService.saveResourceRights(proxies, ownerUser, genericService.find(Dataset.class, datasetId));

    }

}
