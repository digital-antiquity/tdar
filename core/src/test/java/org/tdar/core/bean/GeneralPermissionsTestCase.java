package org.tdar.core.bean;

import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;

public class GeneralPermissionsTestCase {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    
    @Test
    public void getAvailableForResource() {
        List<GeneralPermissions> availablePermissionsFor = GeneralPermissions.getAvailablePermissionsFor(Resource.class);
        logger.debug("rperm: {}", availablePermissionsFor);
    }

    @Test
    public void getAvailableForSharedCollection() {
        List<GeneralPermissions> availablePermissionsFor = GeneralPermissions.getAvailablePermissionsFor(SharedCollection.class);
        logger.debug("sharedperm: {}", availablePermissionsFor);
    }
}
