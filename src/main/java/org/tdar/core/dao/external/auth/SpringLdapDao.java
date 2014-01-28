package org.tdar.core.dao.external.auth;

import java.util.List;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.WhitespaceWildcardsFilter;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.entity.Person;
import org.tdar.utils.MessageHelper;

/**
 * $Id$
 * 
 * <p>
 * Provides authentication services and group management via LDAP
 * </p>
 * 
 * @author <a href='mailto:nuwan.goonasekera@versi.edu.au'>Nuwan Goonasekera</a>
 * @version $Revision$
 * @see <a href='http://static.springsource.org/spring-ldap/site/reference/html/index.html'>Spring LDAP documentation</a>
 * 
 */
@Service
public class SpringLdapDao extends BaseAuthenticationProvider {

    protected final LdapOperations ldapTemplate;
    private String passwordResetURL;
    private String baseDN;
    private String userRDN;
    private String groupDN;

    public SpringLdapDao() {
        ldapTemplate = null;
    }

    @Autowired(required = false)
    public SpringLdapDao(LdapOperations ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    @Override
    public boolean isConfigured() {
        if (ldapTemplate == null) {
            logger.debug("ldaptemplate is null");
            return false;
        }

        try {
            ldapTemplate.lookup("");
        } catch (Exception e) {
            logger.debug("Could not connect to ldap service", e);
            return false;
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.tdar.core.service.external.AuthenticationProvider#logout(javax.servlet
     * .http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.tdar.core.service.external.AuthenticationProvider#authenticate(javax
     * .servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse,
     * java.lang.String, java.lang.String)
     */
    @Override
    public AuthenticationResult authenticate(HttpServletRequest request,
            HttpServletResponse response, String name, String password) {
        try {
            PersonLdapDao ldapDAO = new PersonLdapDao();
            return ldapDAO.authenticate(name, password) == true ? AuthenticationResult.VALID : AuthenticationResult.INVALID_PASSWORD;
        } catch (AuthenticationException e) {
            logger.debug("Invalid authentication for " + name, e);
            return AuthenticationResult.INVALID_PASSWORD;
        } catch (Exception e) {
            logger.error("could not authenticate against ldap server", e);
            return AuthenticationResult.REMOTE_EXCEPTION.exception(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.tdar.core.service.external.AuthenticationProvider#isAuthenticated
     * (javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    public boolean isAuthenticated(HttpServletRequest request,
            HttpServletResponse response) {
        return false; // Unlike crowd, which sets an SSO cookie, we are not
                      // maintaining one. So just return false here. Tdar
                      // session management will have to handle it.
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.tdar.core.service.external.AuthenticationProvider#addUser(org.tdar
     * .core.bean.entity.Person, java.lang.String)
     */
    @Override
    public AuthenticationResult addUser(Person person, String password, TdarGroup... groups) {
        String username = person.getUsername();

        PersonLdapDao ldapDAO = new PersonLdapDao();

        try {
            @SuppressWarnings("unused")
            Person temp = ldapDAO.findByPrimaryKey(username);
            // if this succeeds, then this principal already exists.
            // FIXME: if they already exist in the system, we should let them
            // know that they already have an account in the system and that they can
            // just authenticate to edit their account / profile.
            logger.warn("XXX: Trying to add an LDAP user that already exists: ["
                    + person.toString()
                    + "]\n Returning and attempting to authenticate them.");
            // just check if authentication works then.
            return AuthenticationResult.ACCOUNT_EXISTS;
        } catch (NameNotFoundException e) {
            logger.debug("Object not found, as expected.");
        }

        logger.debug("Adding LDAP user : " + username);
        ldapDAO.create(person, password);
        return AuthenticationResult.VALID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.tdar.core.service.external.AuthenticationProvider#deleteUser(org.
     * tdar.core.bean.entity.Person)
     */
    @Override
    public boolean deleteUser(Person person) {
        PersonLdapDao ldapDAO = new PersonLdapDao();
        ldapDAO.delete(person);

        logger.debug("Removed LDAP user : " + person);
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.tdar.core.service.external.AuthenticationProvider#resetUserPassword
     * (org.tdar.core.bean.entity.Person)
     */
    @Override
    public void resetUserPassword(Person person) {
        // TODO: How does one implement password reset for LDAP? One possibility
        // is for tdar to handle password resets. Another is to redirect to some
        // predefined password reset url,
        // depending on the specific ldap implementation. Till then, just throw
        // an exception
        throw new UnsupportedOperationException(MessageHelper.getMessage("ldap.password_reset_disabled"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.tdar.core.service.external.AuthenticationProvider#updateUserPassword
     * (org.tdar.core.bean.entity.Person, java.lang.String)
     */
    @Override
    public void updateUserPassword(Person person, String password) {
        PersonLdapDao ldapDAO = new PersonLdapDao();
        ldapDAO.update(person, password);
    }

    @Override
    public String[] findGroupMemberships(Person person) {
        PersonLdapDao ldapDAO = new PersonLdapDao();
        return ldapDAO.getGroupMembership(person.getUsername());
    }

    @Override
    public String getPasswordResetURL() {
        return passwordResetURL;
    }

    @Value("${ldap.passwordResetURL}")
    public void setPasswordResetURL(String url) {
        this.passwordResetURL = url;
    }

    /*
     * Get LDAP base DN
     */
    public String getBaseDN() {
        return baseDN;
    }

    @Value("${ldap.baseDn}")
    public void setBaseDN(String baseDN) {
        this.baseDN = baseDN;
    }

    /*
     * Get Relative Distinguished Name (RDN) for users. Will be appended with Base DN during searches.
     */
    public String getUserRDN() {
        return userRDN;
    }

    @Value("${ldap.userRdn}")
    public void setUserRDN(String userRDN) {
        this.userRDN = userRDN;
    }

    /*
     * Get RDN for groups. Will be appended with Base DN during searches.
     */
    public String getGroupRDN() {
        return groupDN;
    }

    @Value("${ldap.groupRdn}")
    public void setGroupRDN(String groupDN) {
        this.groupDN = groupDN;
    }

    /*
     * This internal class encapsulates the actual DAO logic for Person info stored on an LDAP server.
     * Ideally, this should be a stand alone class with most, if not all, data stored on the LDAP server.
     */
    public class PersonLdapDao {
        
        private static final String CLASS_PERSON = "inetOrgPerson";
        private static final String CLASS_GROUP = "posixgroup";
        private static final String ATTR_MEMBER = "memberuid";
        private static final String ATTR_OBJECT_CLASS = "objectclass";
        private static final String ATTR_USER_ID = "uid";
        private static final String ATTR_COMMON_NAME = "cn";
        private static final String ATTR_FIRST_NAME = "givenName";
        private static final String ATTR_MAIL = "mail";
        private static final String ATTR_SURNAME = "sn";
        private static final String ATTR_PASSWORD = "userPassword";

        public boolean authenticate(String username, String password) {
            AndFilter filter = new AndFilter();
            filter.and(new EqualsFilter(ATTR_OBJECT_CLASS, CLASS_PERSON)).and(
                    new EqualsFilter(ATTR_USER_ID, username));
            return ldapTemplate.authenticate(DistinguishedName.EMPTY_PATH,
                    filter.toString(), password);
        }

        public void create(Person person, String password, TdarGroup... groups) {
            Name userdn = buildPersonRDN(person);
            DirContextAdapter context = new DirContextAdapter(userdn);
            mapToContext(person, context);
            setPassword(context, password);
            ldapTemplate.bind(context);

            // Update Groups
            if (ArrayUtils.isEmpty(groups)) {
                addUserToGroup(person, TdarGroup.TDAR_USERS.getGroupName());
            } else {
                for (TdarGroup group : groups) {
                    addUserToGroup(person, group.getGroupName());
                }
            }
        }

        private void addUserToGroup(Person person, String groupName) {
            try {
                Name groupDn = buildGroupRDN(groupName);
                DirContextOperations groupContext = ldapTemplate.lookupContext(groupDn);
                groupContext.addAttributeValue(ATTR_MEMBER, person.getUsername());
                ldapTemplate.modifyAttributes(groupContext);
            } catch (Exception e) {
                logger.debug("Could not add person: " + person + " to group: " + groupName);
                logger.debug("Exception: ", e);
            }
        }

        public void update(Person person, String password) {
            Name dn = buildPersonRDN(person);
            DirContextOperations context = ldapTemplate.lookupContext(dn);
            mapToContext(person, context);
            setPassword(context, password);
            ldapTemplate.modifyAttributes(context);
        }

        public void delete(Person person) {
            ldapTemplate.unbind(buildPersonRDN(person));
        }

        public Person findByPrimaryKey(String uid) {
            Name dn = buildPersonRDN(uid);
            return (Person) ldapTemplate.lookup(dn, getContextMapper());
        }

        public List<?> findByName(String name) {
            AndFilter filter = new AndFilter();
            filter.and(new EqualsFilter(ATTR_OBJECT_CLASS, CLASS_PERSON)).and(
                    new WhitespaceWildcardsFilter(ATTR_COMMON_NAME, name));
            return ldapTemplate.search(DistinguishedName.EMPTY_PATH,
                    filter.encode(), getContextMapper());
        }

        public List<?> findAll() {
            EqualsFilter filter = new EqualsFilter(ATTR_OBJECT_CLASS,
                    CLASS_PERSON);
            return ldapTemplate.search(DistinguishedName.EMPTY_PATH,
                    filter.encode(), getContextMapper());
        }

        public String[] getGroupMembership(String uid) {
            AndFilter filter = new AndFilter();
            try {
                filter.and(new EqualsFilter(ATTR_OBJECT_CLASS, CLASS_GROUP)).and(
                        new EqualsFilter(ATTR_MEMBER, uid));

                List<?> groups = ldapTemplate.search(DistinguishedName.EMPTY_PATH,
                        filter.encode(), new AttributesMapper() {
                            @Override
                            public Object mapFromAttributes(Attributes attrs) throws NamingException {
                                return attrs.get(ATTR_COMMON_NAME).get().toString();
                            }
                        });

                return groups.toArray(new String[0]);
            } catch (Exception e) {
                logger.debug("Could not find membership for person: " + uid, e);
            }

            return new String[0];
        }

        protected ContextMapper getContextMapper() {
            return new PersonContextMapper();
        }

        protected Name buildPersonRDN(Person person) {
            return buildPersonRDN(person.getUsername());
        }

        protected Name buildPersonRDN(String uid) {
            DistinguishedName dn = new DistinguishedName(getUserRDN());
            dn.add(ATTR_USER_ID, uid);
            return dn;
        }

        @SuppressWarnings("unused")
        private Name buildPersonDN(Person person) throws InvalidNameException {
            return buildPersonDN(person.getUsername());
        }

        private Name buildPersonDN(String uid) throws InvalidNameException {
            DistinguishedName dn = new DistinguishedName(getBaseDN());
            dn.addAll(buildPersonRDN(uid));
            return dn;
        }

        @SuppressWarnings("unused")
        private Name buildGroupDN(String groupName) throws InvalidNameException {
            DistinguishedName dn = new DistinguishedName(getBaseDN());
            dn.addAll(buildGroupRDN(groupName));
            return dn;
        }

        private Name buildGroupRDN(String groupName) {
            DistinguishedName dn = new DistinguishedName(getGroupRDN());
            dn.add(ATTR_COMMON_NAME, groupName);
            return dn;
        }

        protected void mapToContext(Person person, DirContextOperations context) {
            context.setAttributeValue(ATTR_OBJECT_CLASS, CLASS_PERSON);
            context.setAttributeValue(ATTR_COMMON_NAME, person.getName());
            context.setAttributeValue(ATTR_FIRST_NAME, person.getFirstName());
            context.setAttributeValue(ATTR_SURNAME, person.getLastName());
            context.setAttributeValue(ATTR_MAIL, person.getEmail());
        }

        protected void setPassword(DirContextOperations context, String password) {
            context.setAttributeValue(ATTR_PASSWORD, password);
        }

        private class PersonContextMapper extends AbstractContextMapper {

            public PersonContextMapper() {
                // TODO Auto-generated constructor stub
            }

            @Override
            public Object doMapFromContext(DirContextOperations context) {
                Person person = new Person();
                person.setEmail(context.getStringAttribute(ATTR_USER_ID));
                person.setFirstName(context.getStringAttribute(ATTR_FIRST_NAME));
                person.setLastName(context.getStringAttribute(ATTR_SURNAME));
                return person;
            }
        }
    }

}