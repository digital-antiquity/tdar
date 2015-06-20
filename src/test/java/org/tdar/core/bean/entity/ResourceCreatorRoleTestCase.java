package org.tdar.core.bean.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceCreatorRoleTestCase {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private static final int ROLE_COUNT_ALL = 17; // counting deprecated
    private static final int ROLE_COUNT_PERSON = 12; // not counting deprecated
    private static final int ROLE_COUNT_INSTITUTION = 11; // NOT counting deprecated

    @Test
    public void testGetAllRoles() {
        List<ResourceCreatorRole> roles = ResourceCreatorRole.getAll();
        assertNotNull(ResourceCreatorRole.getAll());
        assertEquals("getAll should return 20 roles", ROLE_COUNT_ALL, roles.size());
        logger.debug("all roles: " + roles);
    }

    @Test
    public void testGetPersonRoles() {
        List<ResourceCreatorRole> roles = ResourceCreatorRole.getPersonRoles();
        assertNotNull(roles);
        assertEquals("there are currently 14 person roles", ROLE_COUNT_PERSON, roles.size());
        assertFalse(CollectionUtils.containsAny(roles, ResourceCreatorRole.getOtherRoles()));
        logger.debug("person roles: " + roles);
    }

    @Test
    public void testGetInstitutionRoles() {
        List<ResourceCreatorRole> roles = ResourceCreatorRole.getInstitutionRoles();
        assertNotNull(roles);
        assertEquals("there are currently 11 institution roles", ROLE_COUNT_INSTITUTION, roles.size());
        assertFalse(CollectionUtils.containsAny(roles, ResourceCreatorRole.getOtherRoles()));
        logger.debug("institutional roles: " + roles);
    }

}
