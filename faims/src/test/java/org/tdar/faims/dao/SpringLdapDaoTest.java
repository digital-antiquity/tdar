package org.tdar.faims.dao;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import javax.naming.Name;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.support.LdapUtils;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.dao.external.auth.AuthenticationResultType;

/**
 * We know that the Spring LDAP code is tested by SpringSource. We simply need to test that our interface between tdar and that code works as we expect it to.
 * Hence we choose to mock the spring code and test to see if our codes public interface works as expected. We are only testing the methods in the
 * AuthenticationProvider interface that are actually supported/implemented by the SpringLdapDao written by Nuwan.
 * 
 * @author Martin Paulo
 */
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

    private Expectations getAuthenticationExpectation(final String password, final String ldapFilter, final Class<? extends Exception> clss) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        return new Expectations() {
            {
                LdapQueryBuilder query  = LdapQueryBuilder.query();
                query.base(LdapUtils.emptyLdapName());
                query.filter(ldapFilter);
                oneOf(template).authenticate(with(any(query.getClass())), with(same(password)));
                if (clss != null) {
                    will(throwException(clss.newInstance()));
                }
            }
        };
    }

    @Test
    public void willReturnValidResultOnSuccessfullAuthentication() throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final String password = "password";
        final String username = "name";
        final String ldapFilter = "(&(objectclass=inetOrgPerson)(uid=" + username + "))";
        context.checking(getAuthenticationExpectation(password, ldapFilter, null));
        assertTrue(AuthenticationResultType.VALID.equals(dao.authenticate(null, null, username, password).getType()));
        context.assertIsSatisfied();
    }

    @Test
    public void willReturnInvalidPasswordOnUnsuccessfullAuthentication() throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String password = "password";
        String username = "name";
        String ldapFilter = "(&(objectclass=inetOrgPerson)(uid=" + username + "))";
        context.checking(getAuthenticationExpectation(password, ldapFilter, AuthenticationException.class));
        assertTrue(AuthenticationResultType.INVALID_PASSWORD.equals(dao.authenticate(null, null, username, password).getType()));
        context.assertIsSatisfied();
    }

    @SuppressWarnings("static-method")
    private TdarUser getNewPerson() {
        TdarUser person = new TdarUser("first", "last", "first@last.com");
        person.setUsername("firstAndLast");
        return person;
    }

    @Test
    public void willDealWithExistingUserOnAddUser() {
        final TdarUser person = getNewPerson();
        final String password = "password";
        context.checking(new Expectations() {
            {
                oneOf(template).lookup(with(any(Name.class)), with(any(ContextMapper.class)));
                will(returnValue(person));
            }
        });
        assertEquals(AuthenticationResultType.ACCOUNT_EXISTS, dao.addUser(person, password, TdarGroup.TDAR_USERS).getType());
        context.assertIsSatisfied();
    }

    @Test
    public void willAddNewUserOnAddUser() {
        final TdarUser person = getNewPerson();
        final String password = "password";
        context.checking(new Expectations() {
            {
                // we first need to fail in finding the person
                oneOf(template).lookup(with(any(Name.class)), with(any(ContextMapper.class)));
                will(throwException(new NameNotFoundException("missing...")));
                oneOf(template).bind(with(any(DirContextAdapter.class))); // create them
                oneOf(template).lookupContext(with(any(Name.class))); // find them
                oneOf(template).modifyAttributes(with(any(DirContextOperations.class))); // add the group
            }
        });
        assertEquals(AuthenticationResultType.VALID, dao.addUser(person, password, TdarGroup.TDAR_USERS).getType());
        context.assertIsSatisfied();
    }

    @Test
    public void willDeleteUser() {
        final TdarUser person = getNewPerson();
        context.checking(new Expectations() {
            {
                oneOf(template).unbind(with(any(Name.class)));
            }
        });
        assertTrue(dao.deleteUser(person));
        context.assertIsSatisfied();
    }

    @Test
    public void willUpdateUserPassword() {
        final TdarUser person = getNewPerson();
        final String password = "password";
        context.checking(new Expectations() {
            {
                oneOf(template).lookupContext(with(any(Name.class))); // find them
                oneOf(template).modifyAttributes(with(any(DirContextOperations.class))); // add the group
            }
        });
        dao.updateUserPassword(person, password);
        context.assertIsSatisfied();
    }

    @Test
    public void willFindGroupMemberships() {
        final TdarUser person = getNewPerson();
        final String[] groups = { "agroup", "bgroup" };
        context.checking(new Expectations() {
            {
                oneOf(template).search(with(any(LdapQuery.class)), with(any(AttributesMapper.class)));
                will(returnValue(Arrays.asList(groups)));
            }
        });
        assertArrayEquals(groups, dao.findGroupMemberships(person));
        context.assertIsSatisfied();
    }
}
