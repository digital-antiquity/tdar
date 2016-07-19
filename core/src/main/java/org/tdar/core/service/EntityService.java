package org.tdar.core.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Address;
import org.tdar.core.bean.entity.AgreementTypes;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserAffiliation;
import org.tdar.core.bean.resource.BookmarkedResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.GenericDao.FindOptions;
import org.tdar.core.dao.SimpleFileProcessingDao;
import org.tdar.core.dao.entity.AuthorizedUserDao;
import org.tdar.core.dao.entity.InstitutionDao;
import org.tdar.core.dao.entity.PersonDao;
import org.tdar.core.dao.resource.BookmarkedResourceDao;
import org.tdar.core.event.EventType;
import org.tdar.core.event.TdarEvent;
import org.tdar.transform.jsonld.SchemaOrgCreatorTransformer;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.TextProvider;

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
    private transient InstitutionDao institutionDao;
    @Autowired
    private transient AuthorizedUserDao authorizedUserDao;
    @Autowired
    private transient SerializationService serializationService;
    @Autowired
    private transient SimpleFileProcessingDao simpleFileProcessingDao;
    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private transient BookmarkedResourceDao bookmarkedResourceDao;

    /**
     * Find a @link Person by ID
     * 
     * @param id
     * @return
     */
    @Transactional(readOnly = true)
    public Person findPerson(Long id) {
        return find(id);
    }

    /**
     * Find all registered users (limited by most recent x)
     * 
     * @param maxResults
     * @return
     */
    @Transactional(readOnly = true)
    public List<TdarUser> findAllRegisteredUsers(int maxResults) {
        return getDao().findAllRegisteredUsers(maxResults);
    }

    /**
     * Find all registered users
     * 
     * @return
     */
    @Transactional(readOnly = true)
    public List<TdarUser> findAllRegisteredUsers() {
        return getDao().findAllRegisteredUsers(null);
    }

    /**
     * Find all @link Institution entities.
     * 
     * @return
     */
    @Transactional(readOnly = true)
    public List<Institution> findAllInstitutions() {
        return institutionDao.findAll();
    }

    /**
     * Find an @link Institution by exact name
     * 
     * @param name
     * @return
     */
    @Transactional(readOnly = true)
    public Institution findInstitutionByName(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        return institutionDao.findByName(name.trim());
    }

    /**
     * Find a @link Institution by ID
     * 
     * @param id
     * @return
     */
    @Transactional(readOnly = true)
    public Institution findInstitution(long id) {
        return institutionDao.find(id);
    }

    /**
     * Find an @link Institution based on name bounded by wildcards %name%
     * 
     * @param name
     * @return
     */
    @Transactional(readOnly = true)
    public List<Institution> findInstitutionLike(String name) {
        return institutionDao.withNameLike(name);
    }

    /**
     * Find a @link Person by their email
     * 
     * @param email
     * @return
     */
    @Transactional(readOnly = true)
    public Person findByEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return null;
        }
        return getDao().findByEmail(email.trim());
    }

    @Transactional(readOnly = true)
    public TdarUser findUserByEmail(String email) {
        if ((email == null) || email.isEmpty()) {
            return null;
        }
        return getDao().findUserByEmail(email);
    }

    /**
     * Find a @link Person by their Username
     * 
     * @param username
     * @return
     */
    @Transactional(readOnly = true)
    public TdarUser findByUsername(String username) {
        if (StringUtils.isBlank(username)) {
            return null;
        }
        return getDao().findByUsername(username.trim());
    }

    /**
     * Find people with a similar name (for suggesting we merge)
     * 
     * @param person
     * @return
     */
    @Transactional(readOnly=true)
    public List<Person> findSimilarPeople(TdarUser person) {
        return getDao().findSimilarPeople(person);
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
     * Find all resources to which the given user/Person has full access.
     * 
     * @param person
     * @return
     */
    public Set<Resource> findFullUserResources(TdarUser person, boolean isAdmin) {
        return authorizedUserDao.findSparseEditableResources(person, isAdmin);
    }

    /**
     * Find or saveCreator based on type
     * 
     * @see #findOrSaveInstitution(Institution)
     * @see #findOrSavePerson(Person)
     * 
     * @param transientCreator
     * @return
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = false)
    public <C extends Creator<?>> C findOrSaveCreator(C transientCreator) {
        C creatorToReturn = null;

        if (transientCreator instanceof Person) {
            creatorToReturn = (C) findOrSavePerson((Person) transientCreator);
        }
        if (transientCreator instanceof Institution) {
            creatorToReturn = (C) findOrSaveInstitution((Institution) transientCreator);
        }
        if ((creatorToReturn != null) && creatorToReturn.isDeleted()) {
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
            if (transientPerson.getInstitution() != null) {
                Institution findOrSaveInstitution = findOrSaveInstitution(transientPerson.getInstitution());
                transientPerson.setInstitution(findOrSaveInstitution);;
            }
            if (transientPerson instanceof TdarUser && ((TdarUser) transientPerson).getProxyInstitution() != null) {
                TdarUser transientUser = ((TdarUser) transientPerson);
                Institution findOrSaveInstitution = findOrSaveInstitution(transientUser.getProxyInstitution());
                transientUser.setProxyInstitution(findOrSaveInstitution);
            }
            getDao().save(transientPerson);
            blessedPerson = transientPerson;
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
        if ((transientPerson == null) || transientPerson.hasNoPersistableValues()) {
            return null;
        }

        if (PersistableUtils.isNotNullOrTransient(transientPerson.getId())) {
            if (getDao().sessionContains(transientPerson)) {
                return transientPerson;
            }
            return find(transientPerson.getId());
        }

        Person blessedPerson = null;
        String email = transientPerson.getEmail();
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
        transientPerson.setEmail(email);
        return blessedPerson;
    }

    /**
     * Find the @link Institution by name
     * 
     * @param transientInstitution
     * @return
     */
    @Transactional(readOnly = true)
    private Institution findInstitution(Institution transientInstitution) {
        if ((transientInstitution == null) || StringUtils.isBlank(transientInstitution.getName())) {
            return null;
        }

        if (PersistableUtils.isNotNullOrTransient(transientInstitution.getId())) {
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
            if ((transientInstitution == null) || transientInstitution.hasNoPersistableValues()) {
                return null;
            }
            institutionDao.save(transientInstitution);
            blessedInstitution = transientInstitution;
        } else if (!blessedInstitution.isDeleted()) {
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
     * 
     * @param user
     * @return
     */
    @Transactional(readOnly = true)
    public List<ResourceCollection> findAccessibleResourceCollections(TdarUser user) {
        return authorizedUserDao.findAccessibleResourceCollections(user);
    }

    /**
     * List recent logins to the system
     * 
     * @return
     */
    @Transactional(readOnly = true)
    public List<TdarUser> showRecentLogins() {
        return getDao().findRecentLogins();
    }

    /**
     * Increment the login counter for a given user
     * 
     * @param authenticatedUser
     */
    @Transactional(readOnly = false)
    public void registerLogin(TdarUser authenticatedUser, String userAgent) {
        getDao().registerLogin(authenticatedUser, userAgent);
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
    public Creator<?> findAuthorityFromDuplicate(Creator<?> dup) {
        if (PersistableUtils.isNullOrTransient(dup) || !dup.isDuplicate()) {
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
     * 
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
     * 
     * @param creator
     * @return
     */
    @Transactional(readOnly = true)
    public Long getCreatorViewCount(Creator<?> creator) {
        if (PersistableUtils.isNullOrTransient(creator)) {
            return 0L;
        }
        return getDao().getCreatorViewCount(creator);
    }

    @Transactional(readOnly = true)
    public void findResourceCreator(ResourceCreator creator) {
        Creator<?> incomingCreator = creator.getCreator();
        if (incomingCreator instanceof Person) {
            incomingCreator = findPerson((Person) incomingCreator);
        }
        if (incomingCreator instanceof Institution) {
            incomingCreator = findInstitution((Institution) incomingCreator);
        }
        if ((incomingCreator != null) && incomingCreator.isDeleted()) {
            incomingCreator.setStatus(Status.ACTIVE);
        }
        if (incomingCreator != null) {
            creator.setCreator(incomingCreator);
        }
    }

    @Transactional(readOnly = false)
    public void deleteForController(Creator<?> creator, String deletionReason, TdarUser authenticatedUser) {
    	creator.setStatus(Status.DELETED);
    	getDao().saveOrUpdate(creator);
        publisher.publishEvent(new TdarEvent(creator, EventType.CREATE_OR_UPDATE));
    }

    @Transactional(readOnly = true)
    public DeleteIssue getDeletionIssues(TextProvider textProvider, Creator<?> persistable) {
        return null;
    }

    @Transactional(readOnly = false)
    public void saveInstitutionForController(Institution persistable, String name, String email, FileProxy fileProxy) {
        // name has a unique key; so we need to be careful with it
        persistable.setName(name);
        persistable.setEmail(email);
        getDao().saveOrUpdate(persistable);
        if (fileProxy != null) {
            simpleFileProcessingDao.processFileProxyForCreatorOrCollection(persistable, fileProxy);
        }
    }

    @Transactional(readOnly = false)
    public void savePersonforController(Person person, String email, String institutionName, FileProxy fileProxy) {
        if (!StringUtils.equals(email, person.getEmail())) {
            person.setEmail(email);
        }
        getLogger().debug("saving person: {} with institution {} ", person, institutionName);
        if (StringUtils.isBlank(institutionName)) {
            person.setInstitution(null);
        }
        else {
            // if the user changed the person's institution, find or create it
            Institution persistentInstitution = findOrSaveCreator(new Institution(institutionName));
            getLogger().debug("setting institution to persistent: " + persistentInstitution);
            person.setInstitution(persistentInstitution);
        }

        saveOrUpdate(person);
        if (fileProxy != null) {
            simpleFileProcessingDao.processFileProxyForCreatorOrCollection(person, fileProxy);
        }
    }

    @Transactional(readOnly = true)
    public List<BookmarkedResource> getBookmarkedResourcesForUser(TdarUser user) {
        return bookmarkedResourceDao.findBookmarksResourcesByPerson(user);
    }

    @Transactional(readOnly = true)
    public Map<AgreementTypes, Long> getAgreementCounts() {
        return getDao().getAgreementCounts();
    }

    @Transactional(readOnly = true)
    public Map<UserAffiliation, Long> getAffiliationCounts() {
        return getDao().getAffiliationCounts(false);
    }

    @Transactional(readOnly=true)
    public Map<UserAffiliation, Long> getAffiliationCounts(boolean b) {
        return getDao().getAffiliationCounts(true);
    }

    @Transactional(readOnly=true)
    public String createSchemaOrgJson(Creator<?> creator, String logoUrl) throws IOException {
        SchemaOrgCreatorTransformer transformer = new SchemaOrgCreatorTransformer();
        return transformer.convert(serializationService, creator, logoUrl);
    }

    @Transactional(readOnly=false)
    public void saveAddress(Address address2, Creator<?> creator) {
        creator.getAddresses().add(address2);
        getDao().saveOrUpdate(creator);
    }

    @Transactional(readOnly=false)
    public void deleteAddressForCreator(Address address, Creator<?> creator) {
        Address toDelete = address;
        getLogger().info("to delete: {} ", toDelete);
        boolean remove = creator.getAddresses().remove(toDelete);
        getLogger().info("did it work: {} ", remove);
        // this is likely superflouous, but I'm tired
        getDao().delete(toDelete);
        getDao().saveOrUpdate(creator);
        
    }

}
