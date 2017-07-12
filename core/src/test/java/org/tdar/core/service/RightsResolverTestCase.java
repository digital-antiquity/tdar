package org.tdar.core.service;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;

public class RightsResolverTestCase {
    protected final Logger logger = LoggerFactory.getLogger(getClass());


    @Test
    public void testRightsResolverInitOneUser() {
        List<AuthorizedUser> users = new ArrayList<>();
        
        TdarUser basic = new TdarUser("basic", "user", "a@b.com");
        AuthorizedUser user = new AuthorizedUser(basic, basic, GeneralPermissions.MODIFY_METADATA);
        Date date = new Date(1000L);
        user.setDateExpires(date);
        users.add(user);
        RightsResolver rr = RightsResolver.evaluate(users);
        assertEquals(date, rr.getMinDate());
        assertEquals(GeneralPermissions.MODIFY_METADATA, rr.getMinPerm());

        user = new AuthorizedUser(basic, basic, GeneralPermissions.MODIFY_METADATA);
        users = Arrays.asList(user);
        rr = RightsResolver.evaluate(users);
        assertEquals(null, rr.getMinDate());
        assertEquals(GeneralPermissions.MODIFY_METADATA, rr.getMinPerm());

    
    }



    @Test
    public void testRightsResolverInitTwoUsesr() {
        List<AuthorizedUser> users = new ArrayList<>();
        
        TdarUser basic = new TdarUser("basic", "user", "a@b.com");
        AuthorizedUser user = new AuthorizedUser(basic, basic, GeneralPermissions.MODIFY_METADATA);
        AuthorizedUser user2 = new AuthorizedUser(basic, basic, GeneralPermissions.ADD_TO_SHARE);
        Date date = new Date(1000L);
        user.setDateExpires(date);
        users.add(user);
        users.add(user2);
        RightsResolver rr = RightsResolver.evaluate(users);
        assertEquals(null, rr.getMinDate());
        assertEquals(GeneralPermissions.ADD_TO_SHARE, rr.getMinPerm());
    }
    
    
    @Test
    public void testRightsResolverInitTwoDates() {
        List<AuthorizedUser> users = new ArrayList<>();
        TdarUser basic = new TdarUser("basic", "user", "a@b.com");
        AuthorizedUser user = new AuthorizedUser(basic, basic, GeneralPermissions.MODIFY_METADATA);
        AuthorizedUser user2 = new AuthorizedUser(basic, basic, GeneralPermissions.ADD_TO_SHARE);
        Date date = new Date(1000L);
        Date date2 = new Date(1500L);
        user.setDateExpires(date);
        user2.setDateExpires(date2);
        users.add(user);
        users.add(user2);
        RightsResolver rr = RightsResolver.evaluate(users);
        assertEquals(date2, rr.getMinDate());
        assertEquals(GeneralPermissions.ADD_TO_SHARE, rr.getMinPerm());
    }
}
