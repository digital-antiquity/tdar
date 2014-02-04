package org.tdar.core.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthenticationToken;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.request.ContributorRequest;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.GenericDao.FindOptions;
import org.tdar.core.dao.entity.AuthorizedUserDao;
import org.tdar.core.dao.entity.InstitutionDao;
import org.tdar.core.dao.entity.PersonDao;
import org.tdar.core.dao.request.ContributorRequestDao;

/**
 * $Id$
 * <p>
 * Handles requests for all entities, including Persons, Institutions, etc.
 * </p>
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Transactional(readOnly = true)
@Service
public class EntityService extends ServiceInterface.TypedDaoBase<Person, PersonDao> {

    @Autowired
    private InstitutionDao institutionDao;
    @Autowired
    private AuthorizedUserDao authorizedUserDao;
    @Autowired
    private ContributorRequestDao contributorRequestDao;

    /**
     * Find a @link Person by ID
     * 
     * @param id
     * @return
     */
    public Person findPerson(Long id) {
        return find(id);
    }

    /**
     * Find all registered users (limited by most recent x)
     * 
     * @param maxResults
     * @return
     */
    public List<Person> findAllRegisteredUsers(int maxResults) {
        return getDao().findAllRegisteredUsers(maxResults);
    }

    /**
     * Find all registered users
     * @return
     */
    public List<Person> findAllRegisteredUsers() {
        return getDao().findAllRegisteredUsers(null);
    }

    /**
     * Find all @link Institution entities.
     * @return
     */
    public List<Institution> findAllInstitutions() {
        return institutionDao.findAll();
    }

    /**
     * Find an @link Institution by exact name
     * @param name
     * @return
     */
    public Institution findInstitutionByName(String name) {
        return institutionDao.findByName(name);
    }

    /**
     * Find a @link Institution by ID
     * @param id
     * @return
     */
    public Institution findInstitution(long id) {
        return institutionDao.find(id);
    }

    /**
     * Find an @link Institution based on name bounded by wildcards %name% 
     * @param name
     * @return
     */
    public List<Institution> findInstitutionLike(String name) {
        return institutionDao.withNameLike(name);
    }

    /**
     * List all @link ContributorRequest entries
     * @return
     */
    public List<ContributorRequest> findAllContributorRequests() {
        return contributorRequestDao.findAll();
    }

    /**
     * Find a @link ContributorRequest for a specific @link PErson
     * @param p
     * @return
     */
    public ContributorRequest findContributorRequest(Person p) {
        return contributorRequestDao.findByPerson(p);
    }

    /**
     * List all Pending @link ContributorRequest entries
     * @return
     */
    public List<ContributorRequest> findAllPendingContributorRequests() {
        return contributorRequestDao.findAllPending();
    }

    /**
     * Find a @link Person by their email
     * @param email
     * @return
     */
    public Person findByEmail(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        }
        return getDao().findByEmail(email);
    }

    /**
     * Find a @link Person by their Username
     * @param username
     * @return
     */
    public Person findByUsername(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }
        return getDao().findByUsername(username);
    }

    /**
     * Find a Person by their full name (guessing about how to split the name into parts)
     * 
     * @param fullName
     * @return
     */
    public Set<Person> findByFullName(String fullName) {
        return getDao().findByFullName(fullName);
    }

    /**
     * Find the @link AuthenticationToken by Id
     * @param id
     * @return
     */
    public AuthenticationToken findAuthenticationToken(Long id) {
        return getDao().find(AuthenticationToken.class, id);
    }

    /**
     * Find all resources to which the given user/Person has full access.
     * 
     * @param person
     * @return
     */
    public Set<Resource> findFullUserResources(Person person, boolean isAdmin) {
        return authorizedUserDao.findSparseEditableResources(person, isAdmin);
    }

    /**
     * Find or saveCreator based on type 
     * @see #findOrSaveInstitution(Institution)
     * @see #findOrSavePerson(Person)
     * 
     * @param transientCreator
     * @return
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = false)
    public <C extends Creator> C findOrSaveCreator(C transientCreator) {
        C creatorToReturn = null;

        if (transientCreator instanceof Person) {
            creatorToReturn = (C) findOrSavePerson((Person) transientCreator);
        }
        if (transientCreator instanceof Institution) {
            creatorToReturn = (C) findOrSaveInstitution((Institution) transientCreator);
        }
        if (creatorToReturn != null && creatorToReturn.isDeleted()) {
            creatorToReturn.setStatus(Status.ACTIVE);
        }

        return creatorToReturn;
    }

    /**
     * Find a @link Person by id, email or save a new one if one is not found
     * 
     * @param transientPerson
     * @return
     */
    @Transactional(readOnly = false)
    private Person findOrSavePerson(Person transientPerson) {
        // now find or save the person (if the person was found the institution field is ignored
        // entirely and replaced with the persisted person's institution
        if (transientPerson == null || transientPerson.hasNoPersistableValues()) {
            return null;
        }

        if (Persistable.Base.isNotNullOrTransient(transientPerson.getId())) {
            getDao().detachFromSession(transientPerson);
            return find(transientPerson.getId());
        }

        Person blessedPerson = null;
        if (StringUtils.isNotBlank(transientPerson.getEmail())) {
            blessedPerson = findByEmail(transientPerson.getEmail());
        } else {
            transientPerson.setEmail(null);// make sure it's null and not just blank or empty
        }

        // didn't find by email? cast the net a little wider...
        if (blessedPerson == null) {
            if (transientPerson.getInstitution() != null) {
                transientPerson.setInstitution(findOrSaveInstitution(transientPerson.getInstitution()));
            }
            Set<Person> people = getDao().findByPerson(transientPerson);
            /*
             * Perhaps this should match only if FirstName and LastName are not empty, but I can see cases
             * where LastName may not be empty but firstName is...
             */
            if (!people.isEmpty()) {
                blessedPerson = people.iterator().next();
            }
        }

        // still didn't find anything? Fair enough, let's save it
        if (blessedPerson == null) {
            getDao().save(transientPerson);
            blessedPerson = transientPerson;
        }
        return blessedPerson;
    }

    /**
     * Find the @link Institution by name, or save a new one if it does not exist.
     * 
     * @param transientInstitution
     * @return
     */
    @Transactional(readOnly = false)
    private Institution findOrSaveInstitution(Institution transientInstitution) {
        if (transientInstitution == null || StringUtils.isBlank(transientInstitution.getName()))
            return null;

        if (Persistable.Base.isNotNullOrTransient(transientInstitution.getId())) {
            return getDao().find(Institution.class, transientInstitution.getId());
        }

        Institution blessedInstitution = getDao().findByExample(Institution.class, transientInstitution,
                Arrays.asList(Institution.getIgnorePropertiesForUniqueness()), FindOptions.FIND_FIRST_OR_CREATE).get(0);
        if (!blessedInstitution.isDeleted()) {
            blessedInstitution.setStatus(Status.ACTIVE);
        }
        return blessedInstitution;
    }

    /**
     * Finds the matching @link ResourceCreator (based on name, id, unique value) or save a new one.
     * 
     * @param resourceCreator
     */
    @Transactional(readOnly = false)
    public void findOrSaveResourceCreator(ResourceCreator resourceCreator) {
        resourceCreator.setCreator(findOrSaveCreator(resourceCreator.getCreator()));
    }

    /**
     * List @link ResourceCollection entries that a @link Person (user) has access to
     * @param user
     * @return
     */
    @Transactional
    public List<ResourceCollection> findAccessibleResourceCollections(Person user) {
        return authorizedUserDao.findAccessibleResourceCollections(user);
    }

    /**
     * List recent logins to the system
     * 
     * @return
     */
    @Transactional
    public List<Person> showRecentLogins() {
        return getDao().findRecentLogins();
    }

    /**
     * Increment the login counter for a given user
     * 
     * @param authenticatedUser
     */
    @Transactional(readOnly = false)
    public void registerLogin(Person authenticatedUser) {
        getDao().registerLogin(authenticatedUser);
    }

    /**
     * Find the number of actual contributors to tDAR based on resource.submitter
     * 
     * @return
     */
    @Transactional(readOnly = true)
    public Long findNumberOfActualContributors() {
        return getDao().findNumberOfActualContributors();
    }

    /**
     * Given a @link Creator with a @link Status of Duplicate, return the Authority.
     * 
     * @param dup
     * @return
     */
    @Transactional(readOnly = true)
    public Creator findAuthorityFromDuplicate(Creator dup) {
        if (Persistable.Base.isNullOrTransient(dup) || !dup.isDuplicate()) {
            return null;
        }
        if (dup instanceof Person) {
            return getDao().findAuthorityFromDuplicate((Person) dup);
        } else {
            return institutionDao.findAuthorityFromDuplicate((Institution) dup);
        }
    }

    /**
     * Find all Ids of (actual) contributors within the system based on "submitter" id
     * @return
     */
    @Transactional(readOnly = true)
    public Set<Long> findAllContributorIds() {
        return getDao().findAllContributorIds();
    }

    /**
     * Update the Person occurrance count for each Person -- effectively how often they're used or referenced in the Database. This can be used for relevancy
     * ranking and visualization
     */
    @Transactional
    public void updatePersonOcurrances() {
        getDao().updateOccuranceValues();
    }

}
