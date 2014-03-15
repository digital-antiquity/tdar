package org.tdar.core.service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
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
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.GenericDao.FindOptions;
import org.tdar.core.dao.entity.AuthorizedUserDao;
import org.tdar.core.dao.entity.InstitutionDao;
import org.tdar.core.dao.entity.PersonDao;

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
    private XmlService xmlService;

    /**
     * Find a @link Person by ID
     * 
     * @param id
     * @return
     */
    @Transactional(readOnly=true)
    public Person findPerson(Long id) {
        return find(id);
    }

    /**
     * Find all registered users (limited by most recent x)
     * 
     * @param maxResults
     * @return
     */
    @Transactional(readOnly=true)
    public List<Person> findAllRegisteredUsers(int maxResults) {
        return getDao().findAllRegisteredUsers(maxResults);
    }

    /**
     * Find all registered users
     * @return
     */
    @Transactional(readOnly=true)
    public List<Person> findAllRegisteredUsers() {
        return getDao().findAllRegisteredUsers(null);
    }

    /**
     * Find all @link Institution entities.
     * @return
     */
    @Transactional(readOnly=true)
    public List<Institution> findAllInstitutions() {
        return institutionDao.findAll();
    }

    /**
     * Find an @link Institution by exact name
     * @param name
     * @return
     */
    @Transactional(readOnly=true)
    public Institution findInstitutionByName(String name) {
        return institutionDao.findByName(name);
    }

    /**
     * Find a @link Institution by ID
     * @param id
     * @return
     */
    @Transactional(readOnly=true)
    public Institution findInstitution(long id) {
        return institutionDao.find(id);
    }

    /**
     * Find an @link Institution based on name bounded by wildcards %name% 
     * @param name
     * @return
     */
    @Transactional(readOnly=true)
    public List<Institution> findInstitutionLike(String name) {
        return institutionDao.withNameLike(name);
    }


    /**
     * Find a @link Person by their email
     * @param email
     * @return
     */
    @Transactional(readOnly=true)
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
    @Transactional(readOnly=true)
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
        Person blessedPerson = findPerson(transientPerson);
        // still didn't find anything? Fair enough, let's save it
        if (blessedPerson == null) {
            getDao().save(transientPerson);
            if (transientPerson.getInstitution() != null) {
                getDao().saveOrUpdate(transientPerson.getInstitution());
            }
            blessedPerson = transientPerson;
            xmlService.logRecordXmlToFilestore(transientPerson);
        }
        return blessedPerson;
    }

    /**
     * Find a @link Person by id, email 
     * 
     * @param transientPerson
     * @return
     */
    @Transactional(readOnly = true)
    public Person findPerson(Person transientPerson) {
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
            Institution transientInstitution = transientPerson.getInstitution();
            if (transientInstitution != null) {
                Institution foundInstitution = findInstitution(transientInstitution);
                if (foundInstitution != null) {
                    transientPerson.setInstitution(foundInstitution);
                }
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
        return blessedPerson;
    }
    

    /**
     * Find the @link Institution by name
     * 
     * @param transientInstitution
     * @return
     */
    @Transactional(readOnly=true)
    private Institution findInstitution(Institution transientInstitution) {
        if (transientInstitution == null || StringUtils.isBlank(transientInstitution.getName()))
            return null;

        if (Persistable.Base.isNotNullOrTransient(transientInstitution.getId())) {
            return getDao().find(Institution.class, transientInstitution.getId());
        }

        List<Institution> examples = getDao().findByExample(Institution.class, transientInstitution,
                Arrays.asList(Institution.getIgnorePropertiesForUniqueness()), FindOptions.FIND_FIRST);
        if (CollectionUtils.isNotEmpty(examples)) {
            return examples.get(0);
        } 
        return null;
        
    }
    /**
     * Find the @link Institution by name, or save a new one if it does not exist.
     * 
     * @param transientInstitution
     * @return
     */
    @Transactional(readOnly = false)
    private Institution findOrSaveInstitution(Institution transientInstitution) {
        Institution blessedInstitution = findInstitution(transientInstitution);
        if (blessedInstitution == null) {
            if (transientInstitution == null || transientInstitution.hasNoPersistableValues()) {
                return null;
            }
            institutionDao.save(transientInstitution);
            blessedInstitution = transientInstitution;
            xmlService.logRecordXmlToFilestore(transientInstitution);
        } else if (!blessedInstitution.isDeleted()) {
            blessedInstitution.setStatus(Status.ACTIVE);
            xmlService.logRecordXmlToFilestore(blessedInstitution);
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
    @Transactional(readOnly=true)
    public List<ResourceCollection> findAccessibleResourceCollections(Person user) {
        return authorizedUserDao.findAccessibleResourceCollections(user);
    }

    /**
     * List recent logins to the system
     * 
     * @return
     */
    @Transactional(readOnly=true)
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

    /**
     * get aggregate view counts for creators
     * @param creator
     * @return
     */
    @Transactional(readOnly=true)
    public Long getCreatorViewCount(Creator creator) {
        if (Persistable.Base.isNullOrTransient(creator))
            return 0L;
        return getDao().getCreatorViewCount(creator);
    }

    @Transactional(readOnly=true)
    public void findResourceCreator(ResourceCreator creator) {
        Creator incomingCreator = creator.getCreator();
        if (incomingCreator instanceof Person) {
            incomingCreator = findPerson((Person) incomingCreator);
        }
        if (incomingCreator instanceof Institution) {
            incomingCreator = findInstitution((Institution) incomingCreator);
        }
        if (incomingCreator != null && incomingCreator.isDeleted()) {
            incomingCreator.setStatus(Status.ACTIVE);
        }
        if (incomingCreator != null) {
            creator.setCreator(incomingCreator);
        }
    }
}
