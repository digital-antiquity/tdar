package org.tdar.core.dao.external.auth;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;
import org.tdar.core.bean.entity.Person;

/**
 * We know that the SpringLDAP code is tested by SpringSource. We simply need to test our interface to that code works as we expect it to. Hence the
 * way to do this is to mock the spring code and test to see if our code works as expected.
 * 
 * @author Martin Paulo
 */
@Ignore
public class SpringLdapDaoTest {

    Mockery context;
    SpringLdapDao dao;
    LdapOperations template;

    @Before
    public void setUp() throws Exception {
        context = new Mockery();
        template = context.mock(LdapOperations.class);
        dao = new SpringLdapDao(template);
    }

    @Test
    public void isConfiguredIsFalseIfNullTemplate() {
        dao = new SpringLdapDao(null);
        assertFalse(dao.isConfigured());
    }

    @Test
    public void isConfiguredTriesLookup() {
        context.checking(new Expectations() {
            {
                oneOf(template).lookup("");
            }
        });
        assertTrue(dao.isConfigured());
        context.assertIsSatisfied();
    }

    @Test
    public void willReturnValidResultOnSuccessfullAuthentication() {
        final String password = "password";
        final String username = "name";
        final String ldapFilter = "(&(objectclass=inetOrgPerson)(uid=" + username + "))";

        context.checking(new Expectations() {
            {
                oneOf(template).authenticate(DistinguishedName.EMPTY_PATH, ldapFilter, password);
                will(returnValue(true));
            }
        });
        assertTrue(AuthenticationResult.VALID.equals(dao.authenticate(null, null, "name", password)));
        context.assertIsSatisfied();
    }

    @Test
    public void willReturnInvalidPasswordOnUnsuccessfullAuthentication() {
        final String password = "password";
        final String username = "name";
        final String ldapFilter = "(&(objectclass=inetOrgPerson)(uid=" + username + "))";

        context.checking(new Expectations() {
            {
                oneOf(template).authenticate(DistinguishedName.EMPTY_PATH, ldapFilter, password);
                will(returnValue(false));
            }
        });
        assertTrue(AuthenticationResult.INVALID_PASSWORD.equals(dao.authenticate(null, null, "name", password)));
        context.assertIsSatisfied();
    }

    @Test
    public void willDealWithExistingUserOnAddUser() {
        final Person person = new Person("first", "last", "first@last.com");
        final String password = "password";
        context.checking(new Expectations() {
            {
                // oneOf(template).authenticate(DistinguishedName.EMPTY_PATH, ldapFilter, password); will(returnValue(false));
            }
        });
        assertFalse(dao.addUser(person, password, TdarGroup.TDAR_USERS));
        context.assertIsSatisfied();
    }

}
