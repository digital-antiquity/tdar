package org.tdar.core.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
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
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.BookmarkedResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.SimpleFileProcessingDao;
import org.tdar.core.dao.base.GenericDao.FindOptions;
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
public class EntityServiceImpl extends ServiceInterface.TypedDaoBase<Person, PersonDao> implements EntityService {

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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#findPerson(java.lang.Long)
     */
    @Override
    @Transactional(readOnly = true)
    public Person findPerson(Long id) {
        return find(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#findAllRegisteredUsers(int)
     */
    @Override
    @Transactional(readOnly = true)
    public List<TdarUser> findAllRegisteredUsers(int maxResults) {
        return getDao().findAllRegisteredUsers(maxResults);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#findAllRegisteredUsers()
     */
    @Override
    @Transactional(readOnly = true)
    public List<TdarUser> findAllRegisteredUsers() {
        return getDao().findAllRegisteredUsers(null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#findAllInstitutions()
     */
    @Override
    @Transactional(readOnly = true)
    public List<Institution> findAllInstitutions() {
        return institutionDao.findAll();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#findInstitutionByName(java.lang.String)
     */
    @Override
    @Transactional(readOnly = true)
    public Institution findInstitutionByName(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        return institutionDao.findByName(name.trim());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#findPersonsByInstitution(org.tdar.core.bean.entity.Institution)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Person> findPersonsByInstitution(Institution institution) {
        if (PersistableUtils.isNullOrTransient(institution)) {
            return null;
        }
        return getDao().findPeopleByInstituion(institution);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#findInstitution(long)
     */
    @Override
    @Transactional(readOnly = true)
    public Institution findInstitution(long id) {
        return institutionDao.find(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#findInstitutionLike(java.lang.String)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Institution> findInstitutionLike(String name) {
        return institutionDao.withNameLike(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#findByEmail(java.lang.String)
     */
    @Override
    @Transactional(readOnly = true)
    public Person findByEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return null;
        }
        return getDao().findByEmail(email.trim());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#findUserByEmail(java.lang.String)
     */
    @Override
    @Transactional(readOnly = true)
    public TdarUser findUserByEmail(String email) {
        if ((email == null) || email.isEmpty()) {
            return null;
        }
        return getDao().findUserByEmail(email);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#findByUsername(java.lang.String)
     */
    @Override
    @Transactional(readOnly = true)
    public TdarUser findByUsername(String username) {
        if (StringUtils.isBlank(username)) {
            return null;
        }
        return getDao().findByUsername(username.trim());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#findSimilarPeople(org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Person> findSimilarPeople(TdarUser person) {
        return getDao().findSimilarPeople(person);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#findByFullName(java.lang.String)
     */
    @Override
    public Set<Person> findByFullName(String fullName) {
        return getDao().findByFullName(fullName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#findFullUserResources(org.tdar.core.bean.entity.TdarUser, boolean)
     */
    @Override
    public Set<Resource> findFullUserResources(TdarUser person, boolean isAdmin) {
        return authorizedUserDao.findSparseEditableResources(person, isAdmin);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#findOrSaveCreator(C)
     */
    @Override
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
                transientPerson.setInstitution(findOrSaveInstitution);
                ;
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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#findPerson(org.tdar.core.bean.entity.Person)
     */
    @Override
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
        if (transientPerson instanceof TdarUser) {
            String username = StringUtils.trim(((TdarUser) transientPerson).getUsername());

            if (StringUtils.isNotBlank(username)) {
                blessedPerson = findByUsername(username);
            }
            logger.debug("find by username: {}, {}", username, blessedPerson);
        }

        String email = StringUtils.trim(transientPerson.getEmail());
        if (StringUtils.isNotBlank(email) && blessedPerson == null) {
            blessedPerson = findByEmail(email);
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
            // make sure we're trimmng
            transientPerson.setEmail(StringUtils.trim(transientPerson.getEmail()));
            transientPerson.setFirstName(StringUtils.trim(transientPerson.getFirstName()));
            transientPerson.setLastName(StringUtils.trim(transientPerson.getLastName()));
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
        transientInstitution.setName(StringUtils.trim(transientInstitution.getName()));
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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#findOrSaveResourceCreator(org.tdar.core.bean.entity.ResourceCreator)
     */
    @Override
    @Transactional(readOnly = false)
    public void findOrSaveResourceCreator(ResourceCreator resourceCreator) {
        resourceCreator.setCreator(findOrSaveCreator(resourceCreator.getCreator()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#findAccessibleResourceCollections(org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    public List<ResourceCollection> findAccessibleResourceCollections(TdarUser user, Permissions perm) {
        return authorizedUserDao.findAccessibleResourceCollections(user, perm);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#showRecentLogins()
     */
    @Override
    @Transactional(readOnly = true)
    public List<TdarUser> showRecentLogins() {
        return getDao().findRecentLogins();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#registerLogin(org.tdar.core.bean.entity.TdarUser, java.lang.String)
     */
    @Override
    @Transactional(readOnly = false)
    public void registerLogin(TdarUser authenticatedUser, String userAgent) {
        getDao().registerLogin(authenticatedUser, userAgent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#findNumberOfActualContributors()
     */
    @Override
    @Transactional(readOnly = true)
    public Long findNumberOfActualContributors() {
        return getDao().findNumberOfActualContributors();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#findAuthorityFromDuplicate(org.tdar.core.bean.entity.Creator)
     */
    @Override
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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#findAllContributorIds()
     */
    @Override
    @Transactional(readOnly = true)
    public Set<Long> findAllContributorIds() {
        return getDao().findAllContributorIds();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#updatePersonOcurrances()
     */
    @Override
    @Transactional
    public void updatePersonOcurrances() {
        getDao().updateOccuranceValues();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#getCreatorViewCount(org.tdar.core.bean.entity.Creator)
     */
    @Override
    @Transactional(readOnly = true)
    public Long getCreatorViewCount(Creator<?> creator) {
        if (PersistableUtils.isNullOrTransient(creator)) {
            return 0L;
        }
        return getDao().getCreatorViewCount(creator);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#findResourceCreator(org.tdar.core.bean.entity.ResourceCreator)
     */
    @Override
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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#deleteForController(org.tdar.core.bean.entity.Creator, java.lang.String, org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = false)
    public void deleteForController(Creator<?> creator, String deletionReason, TdarUser authenticatedUser) {
        creator.setStatus(Status.DELETED);
        getDao().saveOrUpdate(creator);
        publisher.publishEvent(new TdarEvent(creator, EventType.CREATE_OR_UPDATE));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#getDeletionIssues(com.opensymphony.xwork2.TextProvider, org.tdar.core.bean.entity.Creator)
     */
    @Override
    @Transactional(readOnly = true)
    public DeleteIssue getDeletionIssues(TextProvider textProvider, Creator<?> persistable) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#saveInstitutionForController(org.tdar.core.bean.entity.Institution, java.lang.String, java.lang.String,
     * org.tdar.core.bean.FileProxy)
     */
    @Override
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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#savePersonforController(org.tdar.core.bean.entity.Person, java.lang.String, java.lang.String,
     * org.tdar.core.bean.FileProxy)
     */
    @Override
    @Transactional(readOnly = false)
    public void savePersonforController(Person person, String email, String institutionName, FileProxy fileProxy) {
        if (!StringUtils.equals(email, person.getEmail())) {
            person.setEmail(email);
        }
        getLogger().debug("saving person: {} with institution {} ", person, institutionName);
        if (StringUtils.isBlank(institutionName)) {
            person.setInstitution(null);
        } else {
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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#getBookmarkedResourcesForUser(org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    public List<BookmarkedResource> getBookmarkedResourcesForUser(TdarUser user) {
        return bookmarkedResourceDao.findBookmarksResourcesByPerson(user);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#getAgreementCounts()
     */
    @Override
    @Transactional(readOnly = true)
    public Map<AgreementTypes, Long> getAgreementCounts() {
        return getDao().getAgreementCounts();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#getAffiliationCounts()
     */
    @Override
    @Transactional(readOnly = true)
    public Map<UserAffiliation, Long> getAffiliationCounts() {
        return getDao().getAffiliationCounts(false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#getAffiliationCounts(boolean)
     */
    @Override
    @Transactional(readOnly = true)
    public Map<UserAffiliation, Long> getAffiliationCounts(boolean b) {
        return getDao().getAffiliationCounts(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#getSchemaOrgJson(org.tdar.core.bean.entity.Creator, java.lang.String)
     */
    @Override
    @Transactional(readOnly = true)
    public String getSchemaOrgJson(Creator<?> creator, String logoUrl) throws IOException {
        SchemaOrgCreatorTransformer transformer = new SchemaOrgCreatorTransformer();
        return transformer.convert(serializationService, creator, logoUrl);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#saveAddress(org.tdar.core.bean.entity.Address, org.tdar.core.bean.entity.Creator)
     */
    @Override
    @Transactional(readOnly = false)
    public void saveAddress(Address address2, Creator<?> creator) {
        creator.getAddresses().add(address2);
        getDao().saveOrUpdate(creator);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#deleteAddressForCreator(org.tdar.core.bean.entity.Address, org.tdar.core.bean.entity.Creator)
     */
    @Override
    @Transactional(readOnly = false)
    public void deleteAddressForCreator(Address address, Creator<?> creator) {
        Address toDelete = address;
        getLogger().info("to delete: {} ", toDelete);
        boolean remove = creator.getAddresses().remove(toDelete);
        getLogger().info("did it work: {} ", remove);
        // this is likely superflouous, but I'm tired
        getDao().delete(toDelete);
        getDao().saveOrUpdate(creator);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#findChangesForUser(org.tdar.core.bean.entity.TdarUser, java.util.Date)
     */
    @Override
    @Transactional(readOnly = true)
    public List<ResourceRevisionLog> findChangesForUser(TdarUser user, Date date) {
        return getDao().findChangesForUser(user, date);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.EntityService#findUser(org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    public TdarUser findUser(TdarUser user) {
        TdarUser found = (TdarUser) findPerson(user);
        if (PersistableUtils.isNotNullOrTransient(found)) {
            return found;
        }
        return null;
    }

}
