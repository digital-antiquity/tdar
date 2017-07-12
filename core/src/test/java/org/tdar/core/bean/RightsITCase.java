package org.tdar.core.bean;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
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
        TdarUser badUser = createAndSaveNewPerson();
        AuthorizedUser authorizedUser = new AuthorizedUser(getAdminUser(), badUser, GeneralPermissions.MODIFY_RECORD);
        Date soon = DateTime.now().plusDays(2).toDate();
        authorizedUser.setDateExpires(soon);
        dataset.getAuthorizedUsers().add(authorizedUser);
        genericService.saveOrUpdate(dataset);
        genericService.saveOrUpdate(authorizedUser);
        authorizedUser = null;
        Long id = dataset.getId();
        genericService.synchronize();

        
        List<UserRightsProxy> proxies = Arrays.asList(new UserRightsProxy(new AuthorizedUser(badUser, badUser, GeneralPermissions.MODIFY_RECORD)));
        
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
        TdarUser badUser = createAndSaveNewPerson();
        AuthorizedUser authorizedUser = new AuthorizedUser(getAdminUser(), badUser, GeneralPermissions.MODIFY_METADATA);
        Date soon = DateTime.now().plusDays(2).toDate();
        authorizedUser.setDateExpires(soon);
        dataset.getAuthorizedUsers().add(authorizedUser);
        genericService.saveOrUpdate(dataset);
        genericService.saveOrUpdate(authorizedUser);
        authorizedUser = null;
        Long id = dataset.getId();
        genericService.synchronize();

        
        AuthorizedUser au = new AuthorizedUser(badUser, badUser, GeneralPermissions.MODIFY_METADATA);
        au.setDateExpires(soon);
        List<UserRightsProxy> proxies = Arrays.asList(new UserRightsProxy(au));
        saveAndAssertException(dataset, badUser, proxies);
    }
    

    @Test
    @Rollback
    public void testRightsUpgrade() {
        // setup user that expires in two days, that has access to dataset
        Dataset dataset = createAndSaveNewDataset();
        TdarUser badUser = createAndSaveNewPerson();
        AuthorizedUser authorizedUser = new AuthorizedUser(getAdminUser(), badUser, GeneralPermissions.MODIFY_RECORD);
        Date soon = DateTime.now().plusDays(2).toDate();
        authorizedUser.setDateExpires(soon);
        dataset.getAuthorizedUsers().add(authorizedUser);
        genericService.saveOrUpdate(dataset);
        genericService.saveOrUpdate(authorizedUser);
        authorizedUser = null;
        Long id = dataset.getId();
        genericService.synchronize();

        
        AuthorizedUser au = new AuthorizedUser(badUser, badUser, GeneralPermissions.ADMINISTER_SHARE);
        au.setDateExpires(soon);
        List<UserRightsProxy> proxies = Arrays.asList(new UserRightsProxy(au));
        saveAndAssertException(dataset, badUser, proxies);
    }
    

    @Test
    @Rollback
    public void testRightsExtendNewUser() {
        // setup user that expires in two days, that has access to dataset
        Dataset dataset = createAndSaveNewDataset();
        TdarUser badUser = createAndSaveNewPerson();
        AuthorizedUser authorizedUser = new AuthorizedUser(getAdminUser(), badUser, GeneralPermissions.MODIFY_RECORD);
        Date soon = DateTime.now().plusDays(2).toDate();
        authorizedUser.setDateExpires(soon);
        dataset.getAuthorizedUsers().add(authorizedUser);
        genericService.saveOrUpdate(dataset);
        genericService.saveOrUpdate(authorizedUser);
        authorizedUser = null;
        Long id = dataset.getId();
        genericService.synchronize();

        
        AuthorizedUser au = new AuthorizedUser(badUser, getBasicUser(), GeneralPermissions.MODIFY_RECORD);
        au.setDateExpires(soon);
        List<UserRightsProxy> proxies = Arrays.asList(new UserRightsProxy(au));
        saveAndAssertException(dataset, badUser, proxies);
        
        
    }
}
