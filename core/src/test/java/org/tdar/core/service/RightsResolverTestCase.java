package org.tdar.core.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.Document;

public class RightsResolverTestCase {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testRightsResolverInitOneUser() {
        List<AuthorizedUser> users = new ArrayList<>();

        TdarUser basic = new TdarUser("basic", "user", "a@b.com");
        Date date = new Date(1000L);
        AuthorizedUser user = new AuthorizedUser(basic, basic, Permissions.MODIFY_METADATA, date);
        users.add(user);
        RightsResolver rr = RightsResolver.evaluate(users);

        assertFalse(rr.canModifyUsersOn(new Document()));
        assertFalse(rr.canModifyUsersOn(new ResourceCollection()));
        assertFalse(rr.hasPermissionsEscalation(new AuthorizedUser(basic, basic, Permissions.MODIFY_METADATA, date)));
        assertTrue(rr.hasPermissionsEscalation(new AuthorizedUser(basic, basic, Permissions.MODIFY_METADATA)));
        assertTrue(rr.hasPermissionsEscalation(new AuthorizedUser(basic, basic, Permissions.MODIFY_RECORD, date)));

    }

    @Test
    public void testRightsResolverInitTwoUsesr() {
        List<AuthorizedUser> users = new ArrayList<>();

        TdarUser basic = new TdarUser("basic", "user", "a@b.com");
        Date date = new Date(1000L);
        AuthorizedUser user = new AuthorizedUser(basic, basic, Permissions.MODIFY_METADATA, date);
        AuthorizedUser user2 = new AuthorizedUser(basic, basic, Permissions.ADD_TO_COLLECTION);
        users.add(user);
        users.add(user2);
        RightsResolver rr = RightsResolver.evaluate(users);
        assertTrue(rr.canModifyUsersOn(new Document()));
        assertFalse(rr.canModifyUsersOn(new ResourceCollection()));
        assertFalse(rr.hasPermissionsEscalation(new AuthorizedUser(basic, basic, Permissions.MODIFY_METADATA, date)));
        assertFalse(rr.hasPermissionsEscalation(new AuthorizedUser(basic, basic, Permissions.MODIFY_METADATA)));
        assertFalse(rr.hasPermissionsEscalation(new AuthorizedUser(basic, basic, Permissions.MODIFY_RECORD, date)));
        assertTrue(rr.hasPermissionsEscalation(new AuthorizedUser(basic, basic, Permissions.ADMINISTER_COLLECTION, date)));
        assertTrue(rr.hasPermissionsEscalation(new AuthorizedUser(basic, basic, Permissions.ADMINISTER_COLLECTION)));
    }

    @Test
    public void testRightsResolverInitTwoDates() {
        List<AuthorizedUser> users = new ArrayList<>();
        TdarUser basic = new TdarUser("basic", "user", "a@b.com");
        Date date = new Date(1500L);
        AuthorizedUser user = new AuthorizedUser(basic, basic, Permissions.MODIFY_METADATA);
        AuthorizedUser user2 = new AuthorizedUser(basic, basic, Permissions.ADD_TO_COLLECTION, date);
        users.add(user);
        users.add(user2);
        RightsResolver rr = RightsResolver.evaluate(users);

        assertTrue(rr.canModifyUsersOn(new Document()));
        assertFalse(rr.canModifyUsersOn(new ResourceCollection()));
        assertFalse(rr.hasPermissionsEscalation(new AuthorizedUser(basic, basic, Permissions.MODIFY_METADATA, date)));
        assertFalse(rr.hasPermissionsEscalation(new AuthorizedUser(basic, basic, Permissions.MODIFY_METADATA)));
        assertFalse(rr.hasPermissionsEscalation(new AuthorizedUser(basic, basic, Permissions.MODIFY_RECORD, date)));
        assertTrue(rr.hasPermissionsEscalation(new AuthorizedUser(basic, basic, Permissions.ADMINISTER_COLLECTION, date)));
        assertTrue(rr.hasPermissionsEscalation(new AuthorizedUser(basic, basic, Permissions.ADMINISTER_COLLECTION)));
        assertFalse(rr.hasPermissionsEscalation(new AuthorizedUser(basic, basic, Permissions.ADD_TO_COLLECTION, date)));
        assertTrue(rr.hasPermissionsEscalation(new AuthorizedUser(basic, basic, Permissions.ADD_TO_COLLECTION)));

    }

    @Test
    public void testRightsResolverOver2() {
        // make sure that once a date is set, it's applied
        List<AuthorizedUser> users = new ArrayList<>();
        TdarUser basic = new TdarUser("basic", "user", "a@b.com");
        Date date = new Date(1500L);
        AuthorizedUser user = new AuthorizedUser(basic, basic, Permissions.MODIFY_RECORD, date);
        AuthorizedUser user2 = new AuthorizedUser(basic, basic, Permissions.MODIFY_RECORD);
        users.add(user);
        users.add(user2);
        RightsResolver rr = RightsResolver.evaluate(users);
        assertTrue(rr.canModifyUsersOn(new Document()));
        assertFalse(rr.canModifyUsersOn(new ResourceCollection()));
        assertFalse(rr.hasPermissionsEscalation(new AuthorizedUser(basic, basic, Permissions.MODIFY_METADATA, date)));
        assertFalse(rr.hasPermissionsEscalation(new AuthorizedUser(basic, basic, Permissions.MODIFY_METADATA)));
        assertFalse(rr.hasPermissionsEscalation(new AuthorizedUser(basic, basic, Permissions.MODIFY_RECORD, date)));
        assertFalse(rr.hasPermissionsEscalation(new AuthorizedUser(basic, basic, Permissions.MODIFY_RECORD)));
        assertTrue(rr.hasPermissionsEscalation(new AuthorizedUser(basic, basic, Permissions.ADMINISTER_COLLECTION, date)));
        assertTrue(rr.hasPermissionsEscalation(new AuthorizedUser(basic, basic, Permissions.ADMINISTER_COLLECTION)));
        assertTrue(rr.hasPermissionsEscalation(new AuthorizedUser(basic, basic, Permissions.ADD_TO_COLLECTION, date)));
        assertTrue(rr.hasPermissionsEscalation(new AuthorizedUser(basic, basic, Permissions.ADD_TO_COLLECTION)));

    }
}
