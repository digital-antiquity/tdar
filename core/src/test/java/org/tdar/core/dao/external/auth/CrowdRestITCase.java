package org.tdar.core.dao.external.auth;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.dao.external.auth.AuthenticationResult;
import org.tdar.core.dao.external.auth.AuthenticationResult.AuthenticationResultType;
import org.tdar.core.dao.external.auth.CrowdRestDao;

import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.user.User;

public class CrowdRestITCase extends AbstractIntegrationTestCase {

    private Properties getCrowdProperties() {
        Properties crowdProperties = new Properties();
        // crowdProperties.put("application.name", "tdar.test");
        // crowdProperties.put("application.password", "tdar.test");
        // crowdProperties.put("application.login.url", "http://localhost/crowd");
        // crowdProperties.put("crowd.server.url", "http://localhost/crowd");
        try {
            crowdProperties.load(new FileReader(new File("src/test/resources/crowd.properties")));
        } catch (IOException e) {
            logger.error("couldn't load properties", e);
        }

        return crowdProperties;
    }

    @Test
    @Rollback
    public void testCrowdRestUserUpdate2() {
        CrowdRestDao dao = new CrowdRestDao(getCrowdProperties());
        TdarUser user = createAndSaveNewPerson("authtestuser@tdar.org", "update-email");
        deleteUser(dao, user);
        AuthenticationResult addUser = dao.addUser(user, "changeme", TdarGroup.CONFLUENCE_USERS, TdarGroup.TDAR_EDITOR);
        assertEquals(AuthenticationResultType.VALID, addUser.getType());
        String originalEmail = user.getEmail();
        user.setEmail("testcrowduserupdate@example.com"); // original email for test user should be test@tdar.org
        user.setFirstName("testing");
        user.setLastName("changes");
        logger.debug("original email address was:{}", originalEmail);
        dao.updateBasicUserInformation(user);
        User cu = null;
        try {
            cu = dao.getUser(user);
        } catch (UserNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OperationFailedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ApplicationPermissionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidAuthenticationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertEquals(user.getEmail(), cu.getEmailAddress());
        assertNotEquals(originalEmail, cu.getEmailAddress());
        assertEquals(user.getProperName(), cu.getDisplayName());
        assertEquals(user.getFirstName(), cu.getFirstName());
        assertEquals(user.getLastName(), cu.getLastName());
//        assertEquals(user.getEmail(), cu.getExternalId());
        String[] findGroupMemberships = dao.findGroupMemberships(user);
        logger.debug("groups {}", findGroupMemberships);
        assertTrue(ArrayUtils.contains(findGroupMemberships, TdarGroup.CONFLUENCE_USERS.toString()));
        assertTrue(ArrayUtils.contains(findGroupMemberships, TdarGroup.TDAR_EDITOR.toString()));
        deleteUser(dao, user);
        
    }

    private void deleteUser(CrowdRestDao dao, TdarUser user) {
        try {
            dao.deleteUser(user);
        } catch (Throwable t) {
            logger.error("{}", t, t);
        }
    }

}
