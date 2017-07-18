package org.tdar.core.service;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.service.collection.ResourceCollectionService;

public class SharingITCase extends AbstractIntegrationTestCase {

    @Autowired
    private ResourceCollectionService resourceCollectionService;
    
    @Test
    @Rollback(true)
    public void testResource() {
        Dataset dataset = createAndSaveNewDataset();
        dataset.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(), getBillingUser(), GeneralPermissions.MODIFY_RECORD));
        genericService.saveOrUpdate(dataset);
        genericService.synchronize();
        List<TdarUser> findUsersSharedWith = resourceCollectionService.findUsersSharedWith(getUser());
        logger.debug("{}", findUsersSharedWith);
        assertTrue("should contain billing user", CollectionUtils.containsAll(findUsersSharedWith, Arrays.asList(getBillingUser())));
    }
}
