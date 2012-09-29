package org.tdar.core.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthenticationToken;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.request.ContributorRequest;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.GenericDao.FindOptions;
import org.tdar.core.dao.entity.AuthorizedUserDao;
import org.tdar.core.dao.entity.InstitutionDao;
import org.tdar.core.dao.entity.PersonDao;
import org.tdar.core.dao.request.ContributorRequestDao;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
import org.tdar.core.service.external.auth.InternalTdarRights;

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

    public final static String BEAN_ID = "personService";

    @Autowired
    public void setDao(PersonDao dao) {
        super.setDao(dao);
    }

    @Autowired
    private InstitutionDao institutionDao;
    @Autowired
    private AuthorizedUserDao authorizedUserDao;
    @Autowired
    private ContributorRequestDao contributorRequestDao;
    @Autowired
    private GenericService genericService;

    @Autowired
    private AuthenticationAndAuthorizationService authenticationService;

    public Person findPerson(Long id) {
        return find(id);
    }

    public List<Person> findAllRegisteredUsers(Integer num) {
        return getDao().findAllRegisteredUsers(num);
    }

    public List<Institution> findAllInstitutions() {
        return institutionDao.findAll();
    }

    public Institution findInstitutionByName(String name) {
        return institutionDao.findByName(name);
    }

    public Institution findInstitution(long id) {
        return institutionDao.find(id);
    }

    public List<Institution> findInstitutionLike(String name) {
        return institutionDao.withNameLike(name);
    }

    public List<ContributorRequest> findAllContributorRequests() {
        return contributorRequestDao.findAll();
    }

    public List<ContributorRequest> findAllPendingContributorRequests() {
        return contributorRequestDao.findAllPending();
    }

    /**
     * Returns true if the person is privileged or is a member of the full team read access.
     * 
     * @param person
     * @return
     */
    public boolean canViewConfidentialInformation(Person person, Resource resource) {
        // This function used to pre-test on the resource, but it doesn't have to and is now more granular
        if (resource == null)
            return false;

        if (person == null) {
            logger.trace("person is null");
            return false;
        }

        if (resource.getSubmitter().equals(person)) {
            logger.debug("person was submitter");
            return true;
        }

        if (authenticationService.can(InternalTdarRights.VIEW_AND_DOWNLOAD_CONFIDENTIAL_INFO, person)) {
            return true;
        }

        if (authorizedUserDao.isAllowedTo(person, resource, GeneralPermissions.VIEW_ALL)) {
            logger.debug("person is an authorized user");
            return true;
        }

        logger.debug("returning false... access denied");
        return false;
    }

    /**
     * This is a fairly expensive operation.
     * 
     * Returns true iff
     * <ol>
     * <li>the person and resource parameters are not null
     * <li>resource.submitter is the same as the person parameter
     * <li>the person has curator privileges (signified in crowd)
     * <li>the person has full user privileges on the resource
     * </ol>
     * 
     * @param person
     * @param resource
     * @return true if person has write permissions on resource according to the above policies, false otherwise.
     */
//    @Deprecated
//    public boolean canEditResource(Person person, Resource resource) {
//        return person != null
//                && resource != null
//                && (resource.getSubmitter().equals(person) || authenticationService.can(InternalTdarRights.EDIT_RESOURCES, person) || authorizedUserDao
//                        .isAllowedTo(person, resource,
//                                GeneralPermissions.MODIFY_RECORD));
//    }

    public boolean canDownload(InformationResourceFileVersion irFileVersion, Person person) {
        if (irFileVersion == null)
            return false;
        boolean fileRestricted = (irFileVersion.getInformationResourceFile().isConfidential() ||
                !irFileVersion.getInformationResourceFile().getInformationResource().isAvailableToPublic());
        if (fileRestricted && !canViewConfidentialInformation(person, irFileVersion.getInformationResourceFile().getInformationResource())) {
            return false;
        } else {
            return true;
        }
    }

    public boolean canViewCollection(ResourceCollection collection, Person person) {
        if (collection.isShared() && collection.isVisible())
            return true;
        return authorizedUserDao.isAllowedTo(person, GeneralPermissions.VIEW_ALL, collection);
    }

    public Person findByEmail(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        }
        return getDao().findByEmail(email);
    }

    public Set<Person> findByFullName(String fullName) {
        return getDao().findByFullName(fullName);
    }

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
        return authorizedUserDao.findEditableResources(person, isAdmin);
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = false)
    public <C extends Creator> C findOrSaveCreator(C transientCreator) {
        if (transientCreator instanceof Person) {
            return (C) findOrSavePerson((Person) transientCreator);
        }
        if (transientCreator instanceof Institution) {
            return (C) findOrSaveInstitution((Institution) transientCreator);
        }
        return null;
    }

    @Transactional(readOnly = false)
    private Person findOrSavePerson(Person transientPerson) {
        // now find or save the person (if the person was found the institution field is ignored
        // entirely and replaced with the persisted person's institution
        Person blessedPerson = null;
        if (StringUtils.isNotBlank(transientPerson.getEmail())) {
            blessedPerson = findByEmail(transientPerson.getEmail());
        } else {
            transientPerson.setEmail(null);//make sure it's null and not just blank or empty
        }

        // didn't find by email? cast the net a little wider...
        if (blessedPerson == null) {
            if (transientPerson.getInstitution() != null) {
                transientPerson.setInstitution(findOrSaveInstitution(transientPerson.getInstitution()));
            }
            Set<Person> people = getDao().findByPerson(transientPerson);
            if(!people.isEmpty()) {
                blessedPerson = people.iterator().next();
            }
        }
        
        //still didn't find anything?  Fair enough, let's save it
        if(blessedPerson == null) {
            genericService.save(transientPerson);
            blessedPerson = transientPerson;
        }
        return blessedPerson;
    }

    @Transactional(readOnly = false)
    private Institution findOrSaveInstitution(Institution transientInstitution) {
        Institution blessedInstitution = genericService.findByExample(Institution.class, transientInstitution,
                Arrays.asList(Institution.getIgnorePropertiesForUniqueness()),
                FindOptions.FIND_FIRST_OR_CREATE).get(0);
        return blessedInstitution;
    }

    /**
     * @param resourceCreator
     */
    @Transactional(readOnly = false)
    public void findOrSaveResourceCreator(ResourceCreator resourceCreator) {
        resourceCreator.setCreator(findOrSaveCreator(resourceCreator.getCreator()));
    }

    /**
     * @param authenticatedUser
     * @param persistable
     * @return
     */
    @Transactional(readOnly = false)
    public boolean canEditCollection(Person authenticatedUser, ResourceCollection persistable) {
        if (authenticatedUser == null) {
            logger.debug("person is null");
            return false;
        }

        if (authenticationService.can(InternalTdarRights.EDIT_RESOURCE_COLLECTIONS, authenticatedUser) || authenticatedUser.equals(persistable.getOwner())) {
            return true;
        }

        return authorizedUserDao.isAllowedTo(authenticatedUser, GeneralPermissions.ADMINISTER_GROUP, persistable);
    }

    @Transactional
    public List<ResourceCollection> findAccessibleResourceCollections(Person user) {
        return authorizedUserDao.findAccessibleResourceCollections(user);
    }

    public List<Person> showRecentLogins() {
        return getDao().findRecentLogins();
    }

    @Transactional(readOnly = false)
    public void registerLogin(Person authenticatedUser) {
        authenticatedUser.setLastLogin(new Date());
        authenticatedUser.incrementLoginCount();
        logger.trace("login {} {}", authenticatedUser.getLastLogin(), authenticatedUser.getTotalLogins());
        saveOrUpdate(authenticatedUser);
    }

    @Transactional(readOnly = true)
    public Long findNumberOfActualContributors() {
        return getDao().findNumberOfActualContributors();
    }
}