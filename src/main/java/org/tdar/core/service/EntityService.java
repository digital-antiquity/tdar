package org.tdar.core.service;

import java.util.Arrays;
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
import org.tdar.core.service.external.CrowdService;

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
    private CrowdService crowdService;

    public Person findPerson(Long id) {
        return find(id);
    }

    public List<Person> findAllRegisteredUsers() {
        return getDao().findAllRegisteredUsers();
    }

    public List<Person> findAllOtherRegisteredUsers(Person person) {
        return getDao().findAllOtherRegisteredUsers(person);
    }

    public List<Institution> findAllInstitutions() {
        return institutionDao.findAll();
    }

    public Institution findInstitutionByName(String name) {
        return institutionDao.findByName(name);
    }

    public List<Institution> findInstitutionLike(String name) {
        return institutionDao.withNameLike(name);
    }

    public List<Person> findAllRegisteredUsersSorted() {
        return getDao().findAllRegisteredUsersSorted();
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

        if (isSpecialUser(person)) {
            return true;
        }

        if (resource.getSubmitter().equals(person)) {
            logger.debug("person was submitter");
            return true;
        }

        if (authorizedUserDao.isAllowedTo(person, resource, GeneralPermissions.VIEW_ALL)) {
            logger.debug("person is an authorized user");
            return true;
        }

        logger.debug("returning false... access denied");
        return false;
    }

    public boolean isSpecialUser(Person person) {
        if (person.isPrivileged()) {
            logger.debug("person is privleged");
            return true;
        }

        if (crowdService.isAdministrator(person)) {
            logger.debug("person is admin(crowd)");
            return true;
        }

        return false;
    }

    /**
     * This is a fairly expensive operation.
     * 
     * Returns true iff
     * <ol>
     * <li>the person and resource parameters are not null
     * <li>resource.submitter is the same as the person parameter
     * <li>the person has admin privileges (signified in crowd)
     * <li>the person has full user privileges on the resource
     * </ol>
     * 
     * @param person
     * @param resource
     * @return true if person has write permissions on resource according to the above policies, false otherwise.
     */
    public boolean canEditResource(Person person, Resource resource) {
        return person != null
                && resource != null
                && (resource.getSubmitter().equals(person) || crowdService.isAdministrator(person) || authorizedUserDao.isAllowedTo(person, resource,
                        GeneralPermissions.MODIFY_RECORD));
    }

    public boolean canDownload(InformationResourceFileVersion irFileVersion, Person person) {
        boolean fileRestricted = (irFileVersion.getInformationResourceFile().isConfidential() ||
                !irFileVersion.getInformationResourceFile().getInformationResource().isAvailableToPublic());
        if (fileRestricted && !canViewConfidentialInformation(person, irFileVersion.getInformationResourceFile().getInformationResource())) {
            return false;
        } else {
            return true;
        }
    }

    public boolean canViewCollection(ResourceCollection collection, Person person) {
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

    public AuthenticationToken findAuthenticationToken(Number id) {
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

    @Transactional(readOnly = false)
    public Creator findOrSaveCreator(Creator transientCreator) {
        if (transientCreator instanceof Person) {
            return findOrSavePerson((Person) transientCreator);
        }
        if (transientCreator instanceof Institution) {
            return findOrSaveInstitution((Institution) transientCreator);
        }
        return null;
    }

    @Transactional(readOnly = false)
    private Person findOrSavePerson(Person transientPerson) {
        // now find or save the person (if the person was found the institution field is ignored entirely and replaced with the persisted person's institution
        Person blessedPerson = null;
        if (StringUtils.isNotBlank(transientPerson.getEmail())) {
            blessedPerson = findByEmail(transientPerson.getEmail());
        }

        // didn't find by email? cast the net a little wider...
        if (blessedPerson == null) {
            if (transientPerson.getInstitution() != null) {
                transientPerson.setInstitution(findOrSaveInstitution(transientPerson.getInstitution()));
            }
            List<String> ignoredProps = Arrays.asList("id", "institution", "dateCreated", "registered", "privileged", "rpa", "contributor");
            blessedPerson = genericService.findByExample(Person.class, transientPerson, ignoredProps, FindOptions.FIND_FIRST_OR_CREATE).get(0);
        }
        return blessedPerson;
    }

    @Transactional(readOnly = false)
    private Institution findOrSaveInstitution(Institution transientInstitution) {
        Institution blessedInstitution = genericService.findByExample(Institution.class, transientInstitution, Arrays.asList("id", "dateCreated"),
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

        if (isSpecialUser(authenticatedUser) || authenticatedUser.equals(persistable.getOwner())) {
            return true;
        }

        return authorizedUserDao.isAllowedTo(authenticatedUser, GeneralPermissions.ADMINISTER_GROUP, persistable);
    }

    @Transactional 
    public List<ResourceCollection> findAccessibleResourceCollections(Person user) {
        return authorizedUserDao.findAccessibleResourceCollections(user);
    }
    

}