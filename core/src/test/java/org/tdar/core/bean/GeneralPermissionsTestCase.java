package org.tdar.core.bean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;

public class GeneralPermissionsTestCase {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    
    @Test
    public void getAvailableForResource() {
        List<GeneralPermissions> availablePermissionsFor = GeneralPermissions.getAvailablePermissionsFor(Resource.class);
        logger.debug("rperm: {}", availablePermissionsFor);
        assertTrue(availablePermissionsFor.contains(GeneralPermissions.MODIFY_METADATA));
        assertFalse(availablePermissionsFor.contains(GeneralPermissions.ADMINISTER_SHARE));
    }

    @Test
    public void getAvailableForSharedCollection() {
        List<GeneralPermissions> availablePermissionsFor = GeneralPermissions.getAvailablePermissionsFor(ResourceCollection.class);
        logger.debug("sharedperm: {}", availablePermissionsFor);
        assertTrue(availablePermissionsFor.contains(GeneralPermissions.MODIFY_METADATA));
        assertTrue(availablePermissionsFor.contains(GeneralPermissions.ADMINISTER_SHARE));
    }

}
